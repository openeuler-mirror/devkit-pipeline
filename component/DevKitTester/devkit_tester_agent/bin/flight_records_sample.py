import argparse
import datetime
import logging.config
import os.path
import shutil
import time

import psutil

from devkit_utils import shell_tools, file_utils
from devkit_utils.error_coce import ErrorCodeEnum
from devkit_utils.log_config import config_log_ini
from devkit_utils.pyinstaller_utils import PyInstallerUtils

ROOT_PATH = os.path.dirname(os.path.dirname(__file__))


class TargetProcess:
    def __init__(self, pid, name):
        self.pid = pid
        self.name = name
        self.jfr_name = None


class FlightRecordsFactory:
    RECORD_NAME = "devkit_recorder_per_day"
    STOP_SAMPLE = "1"

    def __init__(self, apps, duration, root_path, wait_for_jmeter_stop, task_id):
        self.apps = apps
        self.duration = duration
        self.root_path = root_path
        self.wait_for_jmeter_stop = wait_for_jmeter_stop
        self.pids: list[TargetProcess] = list()
        self.pids_to_start_recording = list()
        self.pids_to_stop_recording = list()
        self.jfr_paths: list[str] = []
        self.return_code = ErrorCodeEnum.FINISHED
        self.dir_to_storage_jfr = os.path.join(root_path, "data")
        self.tmp_dir = os.path.join("/tmp", task_id)
        self.tmp_data_dir = os.path.join(self.tmp_dir, "data")
        self.tmp_config_dir = os.path.join(self.tmp_dir, "config")
        self.temporary_settings_path = os.path.join(self.tmp_config_dir, "settings.jfc")
        self.settings_path = os.path.join(root_path, "config/settings.jfc")
        self.response_file = os.path.join(root_path, "config/complete_the_upload.ini")
        self.now_date = datetime.datetime.now().strftime("%Y%m%d%H%M%S")
        file_utils.create_dir(self.dir_to_storage_jfr)
        self.jcmd_path = None
        self.user_is_root = False

    def start_sample(self):
        try:
            logging.info("start_sample")
            self.__create_tmp()
            self.__init_pids()
            if len(self.pids) == 0:
                self.return_code = ErrorCodeEnum.NOT_FOUND_APPS
                return
            elif not self.__check_jcmd():
                self.return_code = ErrorCodeEnum.NOT_FOUND_JCMD
                return
            self.__init_user_is_root()
            logging.info("start_recorder")
            if self.user_is_root:
                self.__start_sample_by_root()
            else:
                self.__start_sample_by_common_user()
        finally:
            shell_tools.exec_shell(f"echo {self.return_code} >{self.response_file}", is_shell=True)
            logging.info("the current agent has been executed")

    def __start_sample_by_root(self):
        self.__start_recorder_by_root()
        if self.jfr_paths:
            if self.wait_for_jmeter_stop:
                self.__wait_for_jmeter_has_stopping()
                self.__stop_recorder_by_root()
            else:
                time.sleep(self.duration)
            before = datetime.datetime.now()
            # 停止采集
            logging.info("check has stopped recorder")
            while not self.__check_has_stopped_recorder_by_root() and (datetime.datetime.now() - before).seconds < 30:
                time.sleep(1)
        else:
            logging.exception(f"The target application {self.apps}  cannot be found or Operation not permitted")

    def __start_sample_by_common_user(self):
        self.__start_recorder()
        if self.jfr_paths:
            if self.wait_for_jmeter_stop:
                self.__wait_for_jmeter_has_stopping()
                self.__stop_recorder()
            else:
                time.sleep(self.duration)
            before = datetime.datetime.now()
            # 停止采集
            logging.info("check has stopped recorder")
            while not self.__check_has_stopped_recorder() and (datetime.datetime.now() - before).seconds < 30:
                time.sleep(1)
        else:
            logging.exception(f"The target application {self.apps}  cannot be found or Operation not permitted")

    def __wait_for_jmeter_has_stopping(self):
        before = datetime.datetime.now()
        while (datetime.datetime.now() - before).seconds < self.duration:
            with open(file=os.path.join(self.root_path, "config/jmeter_has_stopped.ini"), mode="r",
                      encoding="utf-8") as file:
                file_content = file.read().strip()
                if file_content == "1":
                    logging.info("stop signal received")
                    return
                else:
                    time.sleep(2)

    def __create_tmp(self):
        file_utils.create_dir(self.tmp_data_dir, mode=0o777)
        file_utils.create_dir(self.tmp_config_dir, mode=0o777)
        shutil.copy(self.settings_path, self.temporary_settings_path, follow_symlinks=False)
        os.chmod(self.temporary_settings_path, mode=0o644)

    def __init_pids(self):
        for app in self.apps.split(","):
            commander_to_view_pid = "ps -ef|grep java|grep -v grep|grep {}|awk '{{print $2}}'".format(app)
            outcome = shell_tools.exec_shell(commander_to_view_pid, is_shell=True)
            logging.info("app:%s to pid %s", app, outcome)
            pids = outcome.out.split()
            for pid in pids:
                self.pids.append(TargetProcess(pid, app))

    def __check_jcmd(self):
        commander_to_check = "which jcmd"
        outcome = shell_tools.exec_shell(commander_to_check, is_shell=True)
        logging.info("check jcmd :%s", outcome)
        if outcome.return_code == 0:
            self.jcmd_path = outcome.out.strip()
            return True
        else:
            return False

    def __init_user_is_root(self):
        if os.geteuid() == 0:
            self.user_is_root = True
        else:
            self.user_is_root = False

    def __start_recorder_by_root(self):
        for target in self.pids:
            jfr_path = self.__jfr_name(target.name, target.pid)
            username = psutil.Process(int(target.pid)).username()
            command = (f"su - {username} -c '"
                       f"{self.jcmd_path} {target.pid} JFR.start  settings={self.temporary_settings_path} "
                       f"duration={self.duration}s  name={self.RECORD_NAME} filename={jfr_path}'")
            logging.info(command)
            outcome = shell_tools.exec_shell(command, is_shell=True)
            logging.info(outcome)
            if outcome.return_code == 0:
                self.jfr_paths.append(jfr_path)
                self.pids_to_start_recording.append(target)
        # 移动到data目录下
        with open(file=os.path.join(self.root_path, "config/upload_sample.ini"), mode="w", encoding="utf-8") as file:
            file.write(os.linesep.join(self.jfr_paths))

    def __start_recorder(self):
        for target in self.pids:
            jfr_path = self.__jfr_name(target.name, target.pid)
            command = (f"jcmd {target.pid} JFR.start settings={self.temporary_settings_path} duration={self.duration}s"
                       f" name={self.RECORD_NAME} filename={jfr_path}")
            logging.info(command)
            outcome = shell_tools.exec_shell(command, is_shell=True)
            logging.info(outcome)
            if outcome.return_code == 0:
                self.jfr_paths.append(jfr_path)
                self.pids_to_start_recording.append(target)
        # 移动到data目录下
        with open(file=os.path.join(self.root_path, "config/upload_sample.ini"), mode="w", encoding="utf-8") as file:
            file.write(os.linesep.join(self.jfr_paths))

    def __stop_recorder_by_root(self):
        for target in self.pids_to_start_recording:
            username = psutil.Process(int(target.pid)).username()
            command = f"su - {username} -c '{self.jcmd_path} {target.pid} JFR.stop name={self.RECORD_NAME}'"
            outcome = shell_tools.exec_shell(command, is_shell=True)
            logging.info(outcome)

    def __stop_recorder(self):
        for target in self.pids_to_start_recording:
            outcome = shell_tools.exec_shell("jcmd {} JFR.stop name={}".format(target.pid, self.RECORD_NAME),
                                             is_shell=True)
            logging.info(outcome)

    def __check_has_stopped_recorder_by_root(self):
        for target in self.pids_to_start_recording:
            username = psutil.Process(int(target.pid)).username()
            command = f"su - {username} -c '{self.jcmd_path}  {target.pid} JFR.check name={self.RECORD_NAME}'"
            outcome = shell_tools.exec_shell(command, is_shell=True)
            logging.info(outcome)
            if outcome.out.find("Could not find"):
                self.pids_to_stop_recording.append(target)
        if len(self.pids_to_stop_recording) == len(self.pids_to_start_recording):
            return True
        else:
            self.pids_to_stop_recording.clear()
            return False

    def __check_has_stopped_recorder(self):
        for target in self.pids_to_start_recording:
            outcome = shell_tools.exec_shell("jcmd {} JFR.check name={}".format(target.pid, self.RECORD_NAME),
                                             is_shell=True)
            logging.info(outcome)
            if outcome.out.find("Could not find"):
                self.pids_to_stop_recording.append(target)
        if len(self.pids_to_stop_recording) == len(self.pids_to_start_recording):
            return True
        else:
            self.pids_to_stop_recording.clear()
            return False

    def __jfr_name(self, app, pid):
        return os.path.join(self.tmp_data_dir, f"{app}_PID_{pid}_Time_{self.now_date}.jfr")

    def __del_dir(self, target):
        if not os.path.exists(target):
            return
        if os.path.isfile(target):
            return
        for file in os.listdir(target):
            sub = os.path.join(target, file)
            if os.path.isfile(sub):
                os.remove(sub)
            if os.path.islink(sub):
                os.unlink(sub)
            if os.path.isdir(sub):
                self.__del_dir(sub)
                os.rmdir(sub)


def main():
    parser = argparse.ArgumentParser(description="Capture the flight records of the target program")
    parser.add_argument("-a", "--app", required=True, dest="applications",
                        help="the process names that can be multiple, each separated by a comma")
    parser.add_argument("-d", "--duration", required=True, dest="duration", type=int, default=43200,
                        help="the time of the sample")
    parser.add_argument("-t", "--task-id", dest="task_id", default="devkit-pipeline-tmp",
                        help="the task id of the sample")
    parser.add_argument("-w", "--wait-for-jmeter-stop", dest="waiting", action="store_true",
                        help="the sample stop when the jmeter stop")
    parser.set_defaults(root_path=PyInstallerUtils.obtain_root_path(ROOT_PATH))
    args = parser.parse_args()
    config_log_ini(args.root_path, "devkit_tester_agent")
    logging.info("start")
    factory = FlightRecordsFactory(args.applications, args.duration, args.root_path, args.waiting, args.task_id)
    factory.start_sample()


if __name__ == "__main__":
    try:
        main()
    except Exception as err:
        logging.exception(err)
        raise err
