import json
import os
import re
import subprocess

PORT_SUB_PATTERN = re.compile(r':[0-9]+')
GIT_REMOTE_URL_COMMAND = """
git --git-dir={}/.git config --get remote.origin.url
"""

GIT_LOG_RECORD_COMMAND = """
git --git-dir={}/.git log --since=1.day \
--pretty=format:'{{"commit": "%H","author": "%an","author_email": "%ae","date": "%ad","message": "%f"}}'
"""
JMETER_REPORT_NAME = "result.csv"
HTML_TEMPLATE_NAME = "perf_report.html"
DEVKIT_REPORT_DATA_LINE_NUM = 33
GIT_REPORT_DATA_LINE_NUM = 43
REPORT_VALID_LINE = 29
JMETER_REPORT_DATA_LINE_NUM = 37


class Report:
    def __init__(self, report_path="./", template_path="./", git_path="./",
                 jmeter_report_path=None, devkit_tool_ip="",
                 devkit_tool_port="8086", devkit_user_name="devadmin"):
        if not os.path.isdir(report_path):
            raise Exception(f"Report path:{report_path} illegal.")
        self.report_dir = report_path
        self.git_path = git_path
        self.template_path = template_path
        self.jmeter_report_path = jmeter_report_path
        self.devkit_tool_ip = devkit_tool_ip
        self.devkit_tool_port = devkit_tool_port
        self.devkit_user_name = devkit_user_name

    def report(self):
        html_lines = self.read_template()
        devkit_report_json = self.generate_devkit_html()
        valid_page = list()
        valid_page.append("'report'")
        html_lines[DEVKIT_REPORT_DATA_LINE_NUM] = "report_tb_data: {}".format(devkit_report_json)
        if self.jmeter_report_path:
            valid_page.append("'perf'")
            jmeter_report = os.path.join(self.report_dir, "result.json")
            with open(jmeter_report, 'r') as file:
                html_lines[JMETER_REPORT_DATA_LINE_NUM] = "perf: {},\n".format(file.read())
        if self.git_path:
            valid_page.append("'git'")
            git_log = self.generate_git_log()
            html_lines[GIT_REPORT_DATA_LINE_NUM] = "git_tb_data: {},".format(git_log)
        html_lines[REPORT_VALID_LINE] = "const valid_pages = [{}];\n".format(",".join(valid_page))
        final_report = os.path.join(self.report_dir, "devkit_performance_report.html")
        with open(final_report, "w") as file:
            file.writelines(html_lines)
        return final_report

    def read_template(self):
        html_lines = []
        with open(os.path.join(self.template_path, HTML_TEMPLATE_NAME), "r") as file:
            html_lines = file.readlines()
        return html_lines
    
    def generate_devkit_html(self):
        return json.dumps(["Devkit URL", "user name", "https://{}:{}/#login".format(self.devkit_tool_ip, self.devkit_tool_port), self.devkit_user_name])

    def generate_git_log(self):
        full_cmd = GIT_LOG_RECORD_COMMAND.format(self.git_path)
        parent_url_cmd = GIT_REMOTE_URL_COMMAND.format(self.git_path)
        parent_url = subprocess.Popen(parent_url_cmd, shell=True, stdout=subprocess.PIPE, encoding="utf-8").stdout.read()
        git_url = re.sub(PORT_SUB_PATTERN, '', parent_url.replace("ssh://git@", "https://").replace(".git", "")).strip("\n") + "/commit/"
        git_data = subprocess.Popen(full_cmd, shell=True, stdout=subprocess.PIPE, encoding='utf-8').stdout.readlines()
        data = ["commit", "author", "author_email", "date", "message"]
        for x in git_data:
            data.extend([value.strip("'").rstrip("'") if key != "commit" else git_url + value.strip("'").rstrip("'") for key, value in json.loads(x).items()])
        git_log = json.dumps(data)
        return git_log
