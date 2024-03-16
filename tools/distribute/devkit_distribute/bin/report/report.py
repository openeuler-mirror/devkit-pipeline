import csv
import json
import os
import subprocess
import time
import re


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
JMETER_SUMMARY_TEMPLATE_HOLDER = "/** jmeter-trend-data **/"
GIT_TEMPLATE_HOLDER = "/** git-history **/"

class Report:
    def __init__(self, report_path):
        if not os.path.isdir(report_path):
            raise Exception(f"Report path:{report_path} illegal.")
        self.report_dir = os.path.join(report_path, str(time.time()))
        os.mkdir(self.report_dir)
        self.main_page = ""
        self.summary_page = ""
        self.git_log_page = ""
        self.chart_page = ""

    def generate_git_log(self, repo_path):
        full_cmd = GIT_LOG_RECORD_COMMAND.format(repo_path)
        parent_url_cmd = GIT_REMOTE_URL_COMMAND.format(repo_path)
        parent_url = subprocess.Popen(parent_url_cmd, shell=True, stdout=subprocess.PIPE, encoding="utf-8").stdout.read()
        git_url = re.sub(PORT_SUB_PATTERN, '', parent_url.replace("ssh://git@", "https://").replace(".git", "")).strip("\n") + "/commit/"
        git_data = subprocess.Popen(full_cmd, shell=True, stdout=subprocess.PIPE, encoding='utf-8').stdout.readlines()
        data = ["commit", "author", "author_email", "date", "message"]
        for x in git_data:
            data.extend([value.strip("'").rstrip("'") if key != "commit" else git_url + value.strip("'").rstrip("'") for key, value in json.loads(x).items()])
        git_log = json.dumps(data)

        with open(os.path.join(HTML_TEMPLATE_NAME), "r") as f:
            html_lines = f.readlines()
            res = [sub.replace(GIT_TEMPLATE_HOLDER, git_log) for sub in html_lines]
            with open(os.path.join(HTML_TEMPLATE_NAME), "w") as file:
                file.writelines(res)
                
    def jmeter_report_to_html(self):
        all_data= []
        with open(JMETER_REPORT_NAME, newline='') as csvfile:
            reader = csv.reader(csvfile)
            for row in reader:
                all_data.append(row)

        all_data_json = json.dumps(all_data)
        with open(HTML_TEMPLATE_NAME, "r") as file:
            html_lines = file.readlines()
            res = [sub.replace(JMETER_SUMMARY_TEMPLATE_HOLDER, all_data_json) for sub in html_lines]
            with open(HTML_TEMPLATE_NAME, "w") as new_file:
                new_file.writelines(res)

a = Report()
a.generate_git_log()