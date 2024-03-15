import argparse
import datetime
import logging
import os
import uuid

from devkit_utils import file_utils
from devkit_utils.devkit_client import DevKitClient
from devkit_utils.error_coce import ErrorCodeEnum, ErrorCodeMsg
from devkit_utils.log_config import config_log_ini
from devkit_utils.pyinstaller_utils import obtain_root_path
from devkit_utils.transport_utils import SSHClientFactory
from report.report import Report

ROOT_PATH = os.path.dirname(os.path.dirname(__file__))


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
        self.report = Report("./")
        file_utils.create_dir(self.data_path)

    def distribute(self):
        task_id = str(uuid.uuid4())
        # 分发采集任务
        self.distribute_to_sample_task(task_id)
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

    def __print_result(self, jfr_names):
        print("=============================================================")
        print("The following files have been uploaded to the DevKit server:")
        for jfr_name in jfr_names:
            print(jfr_name)
        print(f"Please open the following address to view：\n"
              f"https://{self.devkit_ip}:{self.devkit_port}")
        print(f"user :{self.devkit_user}, password: ${self.devkit_password}")

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
                stdin, stdout, stderr = ssh_client.exec_command(
                    f"sh {task_id}/devkit_pipeline_agent/bin/devkit_agent_start.sh "
                    f"-a {self.apps} -d {self.duration} -t {task_id}")
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
