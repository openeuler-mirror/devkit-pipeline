import argparse
import datetime
import logging
import os
import threading
import typing
import uuid

from devkit_utils import file_utils
from devkit_utils import shell_tools
from devkit_utils.devkit_client import DevKitClient
from devkit_utils.error_coce import ErrorCodeEnum, ErrorCodeMsg
from devkit_utils.log_config import config_log_ini
from devkit_utils.pyinstaller_utils import obtain_root_path
from devkit_utils.transport_utils import SSHClientFactory
from report.report import Report

ROOT_PATH = os.path.dirname(os.path.dirname(__file__))


class JmeterCommand:
    def __init__(self, origin_command):
        self.origin_command: str = origin_command
        self.csv_file = None
        self.result_dir = None
        self.jmx_file = None

    def check_and_init_jmeter_cmd(self):
        if not self.origin_command:
            return
        jmeter_commands: typing.List[str] = self.origin_command.split()
        command_length = len(jmeter_commands)
        index = 0
        while index < command_length:
            if jmeter_commands[index] in ["sh", "bash", "/bin/bash", "/bin/sh"]:
                index = index + 1
                continue
            elif jmeter_commands[index].endswith("jmeter.sh"):
                index = index + 1
                continue
            elif jmeter_commands[index] == "-n":
                index = index + 1
                continue
            elif jmeter_commands[index] == "-e":
                index = index + 1
                continue
            elif jmeter_commands[index].endswith("t") and index + 1 < command_length:
                self.jmx_file = jmeter_commands[index + 1]
                index = index + 2
                continue
            elif jmeter_commands[index].endswith("l") and index + 1 < command_length:
                self.csv_file = jmeter_commands[index + 1]
                index = index + 2
                continue
            elif jmeter_commands[index].endswith("o") and index + 1 < command_length:
                self.result_dir = jmeter_commands[index + 1]
                index = index + 2
                continue
            else:
                break
        else:
            return self.__check_param_resource()
        raise Exception(
            "The command line is not supported。example: sh jmeter.sh -n -t /home/demo.jmt "
            "-l /home/jmeter/result.csv -e -o /home/jmeter/empty_dir/")

    def __check_param_resource(self):
        if os.path.exists(self.csv_file):
            raise Exception(f"the file {self.csv_file} is exist")
        if os.path.exists(self.result_dir) and os.path.getsize(self.result_dir) > 0:
            raise Exception(f"the directory {self.result_dir} is exist or not empty")
        if not os.path.exists(self.jmx_file):
            raise Exception(f"the jmx file {self.jmx_file} is not exist")


class Distributor:
    def __init__(self, args):
        self.ips_list = args.ips_list.split(",")
        self.port = args.port
        self.user = args.user
        self.password = args.password
        self.pkey_file = args.pkey_file
        self.pkey_content = args.pkey_content
        self.pkey_password = args.pkey_password
        self.duration = args.duration
        self.root_path = args.root_path
        self.apps = args.applications
        self.data_path = os.path.join(self.root_path, "data")
        self.log_path = os.path.join(self.root_path, "log")
        self.devkit_ip = args.devkit_ip
        self.devkit_port = args.devkit_port
        self.devkit_user = args.devkit_user
        self.devkit_password = args.devkit_password
        file_utils.create_dir(self.data_path)
        self.template_path = os.path.join(self.root_path, "config")
        self.git_path = args.git_path
        self.jmeter_command: JmeterCommand = JmeterCommand(args.jmeter_command)
        self.jmeter_thread: typing.Optional[threading.Thread] = None
        self.enable_jmeter_command = True if args.jmeter_command else False

    def distribute(self):
        # jmeter 命令校验
        self.jmeter_command.check_and_init_jmeter_cmd()
        # 校验联通性
        self.__check_ips_connected()
        # 启动jmeter
        if self.enable_jmeter_command:
            self.__start_jmeter_thread()
        task_id = str(uuid.uuid4())
        # 分发采集任务
        self.distribute_to_sample_task(task_id)
        if self.enable_jmeter_command:
            self.jmeter_thread.join()
            self.send_jmeter_has_stopped(task_id)
        # 获取jfr文件，删除任务文件
        local_jfrs = list()
        self.obtain_jfrs(local_jfrs, task_id)
        # 发送至 Devkit
        client = DevKitClient(self.devkit_ip, self.devkit_port, self.devkit_user, self.devkit_password)
        jfr_names = list()
        for jfr in local_jfrs:
            jfr_names.append(os.path.basename(jfr))
            client.upload_report_by_force(jfr)
        client.logout()
        # 清空本地jfr文件
        file_utils.clear_dir(self.data_path)
        # 等待jmeter完成
        if self.enable_jmeter_command:
            report = Report(report_path=self.data_path, template_path=self.template_path,
                            jmeter_report_path=self.jmeter_command.csv_file,
                            git_path=self.git_path, devkit_tool_ip=self.devkit_ip,
                            devkit_tool_port=self.devkit_port, devkit_user_name=self.devkit_user)
        else:
            report = Report(report_path=self.data_path, template_path=self.template_path,
                            git_path=self.git_path, devkit_tool_ip=self.devkit_ip,
                            devkit_tool_port=self.devkit_port, devkit_user_name=self.devkit_user)
        report.report()
        self.__print_result(jfr_names)

    def __start_jmeter_thread(self):
        self.jmeter_command.check_and_init_jmeter_cmd()
        self.jmeter_thread = threading.Thread(target=self.__jmeter_start, args=(self.jmeter_command.origin_command,))
        self.jmeter_thread.start()

    @staticmethod
    def __jmeter_start(command):
        outcome = shell_tools.exec_shell(command, is_shell=True, timeout=None)
        logging.info("return_code: %s", outcome.return_code)
        logging.info("error: %s", outcome.err)
        logging.info("out: %s", outcome.out)

    def __print_result(self, jfr_names):
        print("=============================================================")
        print("The following files have been uploaded to the DevKit server:")
        for jfr_name in jfr_names:
            print(jfr_name)
        print(f"Please open the following address to view：\n"
              f"https://{self.devkit_ip}:{self.devkit_port}")
        print(f"user :{self.devkit_user}, password: {self.devkit_password}")

    def __check_ips_connected(self):
        for ip in self.ips_list:
            factory = SSHClientFactory(ip=ip, user=self.user, port=self.port, password=self.password,
                                       pkey_file=self.pkey_file, pkey_content=self.pkey_content,
                                       pkey_password=self.pkey_password)
            ssh_client = factory.create_ssh_client()
            ssh_client.close()
        client = DevKitClient(self.devkit_ip, self.devkit_port, self.devkit_user, self.devkit_password)
        client.logout()

    def send_jmeter_has_stopped(self, task_id):
        # 顺序获取
        for ip in self.ips_list:
            factory = SSHClientFactory(ip=ip, user=self.user, port=self.port, password=self.password,
                                       pkey_file=self.pkey_file, pkey_content=self.pkey_content,
                                       pkey_password=self.pkey_password)
            ssh_client = factory.create_ssh_client()
            try:
                logging.info("Wait for the server[%s]  to finish uploading the jfr file", ip)
                ssh_client.exec_command(f"echo 1 > {task_id}/devkit_pipeline_agent/config/jmeter_has_stopped.ini")
            except Exception as ex:
                logging.exception(ex)

    def obtain_jfrs(self, local_jfrs, task_id):
        # 顺序获取
        for ip in self.ips_list:
            factory = SSHClientFactory(ip=ip, user=self.user, port=self.port, password=self.password,
                                       pkey_file=self.pkey_file, pkey_content=self.pkey_content,
                                       pkey_password=self.pkey_password)
            ssh_client = factory.create_ssh_client()
            try:
                logging.info("Wait for the server[%s]  to finish uploading the jfr file", ip)
                self.__blocking_util_upload_success(ssh_client,
                                                    f"{task_id}/devkit_pipeline_agent/config/complete_the_upload.ini",
                                                    ip)

                logging.info("obtain the jfr file name from ip:%s", ip)
                stdin, stdout, stderr = ssh_client.exec_command(
                    f"cat {task_id}/devkit_pipeline_agent/config/upload_sample.ini")
                jfr_paths_all = stdout.read().decode("utf-8")
                jfr_paths = jfr_paths_all.split(os.linesep) if jfr_paths_all else []
                logging.info("jfr path:%s", jfr_paths)
                self.__close_pipeline(stdin, stdout, stderr)
                sftp_client = ssh_client.open_sftp()
                log_ip_name = ip.replace(".", "_")
                sftp_client.get(f"{task_id}/devkit_pipeline_agent/log/devkit_pipeline_agent.log",
                                f"{self.log_path}/devkit_pipeline_agent_{log_ip_name}.log")
                self.print_agent_log_file(log_ip_name)
                logging.info("download the jfr file from ip:%s", ip)
                if not jfr_paths:
                    raise Exception(f"The jfr file {self.apps} cannot be generated")
                for jfr_path in jfr_paths:
                    local_path = os.path.join(self.data_path, os.path.basename(jfr_path))
                    sftp_client.get(jfr_path, local_path)
                    local_jfrs.append(local_path)
                sftp_client.close()
                logging.info("the server[%s] has finish uploading the jfr file or has timeout 6000", ip)
            except Exception as ex:
                logging.exception(ex)
            finally:
                self.__delete_agent(ssh_client, task_id)
                ssh_client.close()

    def print_agent_log_file(self, ip):
        with open(file=f"{self.log_path}/devkit_pipeline_agent_{ip}.log", mode="r", encoding="utf-8") as file:
            all_content = file.read()
            logging.info("============agent [%s] log===start=============\n%s", ip, all_content)
            logging.info("============agent [%s] log===end=============", ip)

    def distribute_to_sample_task(self, task_id):
        # 分发采集任务
        for ip in self.ips_list:
            factory = SSHClientFactory(ip=ip, user=self.user, port=self.port, password=self.password,
                                       pkey_file=self.pkey_file, pkey_content=self.pkey_content,
                                       pkey_password=self.pkey_password)
            ssh_client = factory.create_ssh_client()
            try:
                logging.info("ip:%s create %s directory ", ip, task_id)
                stdin, stdout, stderr = ssh_client.exec_command(f"mkdir {task_id}")
                self.__close_pipeline(stdin, stdout, stderr)
                agent_package = os.path.join(self.root_path, "config/devkit_pipeline_agent.tar.gz")
                logging.info("ip:%s upload devkit_pipeline_agent.tar.gz", ip)
                sftp_client = ssh_client.open_sftp()
                sftp_client.put(agent_package, f"{task_id}/devkit_pipeline_agent.tar.gz")
                sftp_client.close()
                logging.info("ip:%s unpack devkit_pipeline_agent.tar.gz", ip)
                stdin, stdout, stderr = ssh_client.exec_command(
                    f"cd {task_id} && tar -xvzf devkit_pipeline_agent.tar.gz --no-same-owner")
                logging.info("upack tar.gz %s", stderr.readlines())
                self.__close_pipeline(stdin, stdout, stderr)
                logging.info("ip:%s start devkit pipeline agent", ip)
                if self.enable_jmeter_command:
                    start_command = (f"bash {task_id}/devkit_pipeline_agent/bin/devkit_agent_start.sh -a {self.apps} "
                                     f"-d {self.duration} -t {task_id} -w")
                else:
                    start_command = (f"bash {task_id}/devkit_pipeline_agent/bin/devkit_agent_start.sh -a {self.apps} "
                                     f"-d {self.duration} -t {task_id}")
                stdin, stdout, stderr = ssh_client.exec_command(start_command)
                logging.info("start the sampling process on server %s:%s", ip, stderr.readlines())
                self.__close_pipeline(stdin, stdout, stderr)
                logging.info("ip:%s The devkit pipeline agent was successfully launched", ip)
            except Exception as ex:
                self.__delete_agent(ssh_client, task_id)
                raise ex
            finally:
                ssh_client.close()

    @staticmethod
    def __delete_agent(ssh_client, task_id):
        ssh_client.exec_command(f"rm -rf {task_id}")
        ssh_client.exec_command(f"rm -rf /tmp/{task_id}")

    def __blocking_util_upload_success(self, ssh_client, transport_file, ip):
        before = datetime.datetime.now()
        while (datetime.datetime.now() - before).seconds < 6000 + self.duration:
            stdin, stdout, stderr = ssh_client.exec_command(f"cat {transport_file}")
            file_content = stdout.read().decode("utf-8").strip()
            self.__close_pipeline(stdin, stdout, stderr)
            if file_content == str(ErrorCodeEnum.FINISHED):
                return True
            elif file_content == str(ErrorCodeEnum.NOT_FOUND_JCMD):
                raise Exception(ErrorCodeMsg.LANGUAGE_EN.get(ErrorCodeEnum.NOT_FOUND_JCMD).format(ip))
            elif file_content == str(ErrorCodeEnum.NOT_FOUND_APPS):
                raise Exception(ErrorCodeMsg.LANGUAGE_EN.get(ErrorCodeEnum.NOT_FOUND_APPS).format(self.apps, ip))
        return False

    @staticmethod
    def __close_pipeline(stdin, stdout, stderr):
        stdin.close()
        stdout.close()
        stderr.close()


def main():
    parser = argparse.ArgumentParser(description="Capture the flight records of the target program")
    parser.add_argument("-i", "--ips", required=True, dest="ips_list",
                        help="the machine ips on which the java application is running ")
    parser.add_argument("-u", "--user", required=True, dest="user", default="root",
                        help="the user of the ips")
    parser.add_argument("-P", "--port", dest="port", type=int, default=22,
                        help="the ssh port of the ips")
    parser.add_argument("-f", "--pkey-file", dest="pkey_file",
                        help="the file path of the private key")
    parser.add_argument("-c", "--pkey-content", dest="pkey_content",
                        help="the content of the private key")
    parser.add_argument("-w", "--pkey-password", dest="pkey_password",
                        help="the private key password")
    parser.add_argument("--devkit-ip", dest="devkit_ip", required=True,
                        help="the ip of the kunpeng DevKit server")
    parser.add_argument("--devkit-port", dest="devkit_port", default="8086",
                        help="the port of the kunpeng DevKit server")
    parser.add_argument("--devkit-user", dest="devkit_user", default="devadmin",
                        help="the user of the kunpeng DevKit server")
    parser.add_argument("--devkit-password", dest="devkit_password", default="admin100",
                        help="the password of the user of the kunpeng DevKit server")
    parser.add_argument("-a", "--app", required=True, dest="applications",
                        help="the process names that can be multiple, each separated by a comma")
    parser.add_argument("-d", "--duration", required=True, dest="duration", type=int,
                        help="the time of the sample")
    parser.add_argument("--git-path", required=True, dest="git_path", type=str,
                        help="git path")
    parser.add_argument("--jmeter-command", dest="jmeter_command", type=str,
                        help="the command that start jmeter tests")
    parser.set_defaults(root_path=obtain_root_path(ROOT_PATH))
    parser.set_defaults(password="")
    args = parser.parse_args()
    config_log_ini(args.root_path, "devkit_distribute")
    logging.info("devkit_distribute start")
    logging.info(args)
    distributor = Distributor(args)
    distributor.distribute()


if __name__ == "__main__":
    try:
        main()
    except Exception as err:
        logging.exception(err)
        raise err
