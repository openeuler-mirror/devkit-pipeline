import csv
import json
import os
import subprocess
import time

import pandas as pd
from jinja2 import Template

from template.git_log_template import GIT_LOG_TEMPLATE
from template.performance_template import PERFORMANCE_TEMPLATE
from template.summary_template import SUMMARY_TEMPLATE
from template.chart_template import CHART_TEMPLATE

GIT_LOG_RECORD_COMMAND = """
git --git-dir={}/.git log --since=1.day \
--pretty=format:'{{"commit": "%H","author": "%an","author_email": "%ae","date": "%ad","message": "%f"}}'
"""

MAIN_PAGE_FILE = "performance_report.html"
GIT_LOG_FILE = "git_record.html"
SUMMARY_FILE = "performance_summary.html"
CHAT_FILE = "performance_chart.html"


class Report:
    def __init__(self, report_path):
        if not os.path.isdir(report_path):
            raise Exception(f"Report path:{report_path} illegal.")
        self.report_dir = os.path.join(report_path, str(time.time()))
        os.mkdir(self.report_dir)
        self.main_template = Template(PERFORMANCE_TEMPLATE)
        self.summary_template = Template(SUMMARY_TEMPLATE)
        self.git_log_template = Template(GIT_LOG_TEMPLATE)
        self.chart_template = Template(CHART_TEMPLATE)
        self.main_page = ""
        self.summary_page = ""
        self.git_log_page = ""
        self.chart_page = ""

    def generate_git_log(self, repo_path):
        full_command = GIT_LOG_RECORD_COMMAND.format(repo_path)
        data = subprocess.Popen(full_command, shell=True, stdout=subprocess.PIPE, encoding="utf-8").stdout.readlines()
        data = [json.loads(x) for x in data]
        git_log = {"git_log_list": data}
        self.git_log_page = self.git_log_template.render(git_log)
        with open(os.path.join(self.report_dir, GIT_LOG_FILE), "w") as f:
            f.write(self.git_log_page)

    def generate_summary(self, csv_file):
        data = []
        with open(csv_file, "r") as f:
            reader = csv.reader(f)
            headers = next(reader)
            for row in reader:
                o = dict()
                for i in range(len(headers)):
                    o.update({headers[i]: row[i]})
                data.append(o)
        summary_list = {"summary_list": data}
        self.summary_page = self.summary_template.render(summary_list)
        with open(os.path.join(self.report_dir, SUMMARY_FILE), "w+") as f:
            f.write(self.summary_page)

    def generate_index(self, ip, id_dict, port="8086", username="devadmin"):
        url = f"https://{ip}:{port}"
        info = {"url": url, "username": username}
        info.update(id_dict)
        info_dict = {"info_dict": info}
        self.main_page = self.main_template.render(info_dict)
        with open(os.path.join(self.report_dir, MAIN_PAGE_FILE), "w") as f:
            f.write(self.main_page)

    def generate_chars(self, csv_file):
        df = pd.read_csv(csv_file, index_col=False)
        df_filterd = df[["timeStamp", "elapsed", "IdleTime", "Latency", "label"]]
        df_filterd.columns = ["time", "elapsed", "idle", "latency", "label"]
        csv_string = df_filterd.to_csv(index=False)
        data = {"csv_content": csv_string}
        self.chart_page = self.chart_template.render(data)
        with open(os.path.join(self.report_dir, CHAT_FILE), "w") as f:
            f.write(self.chart_page)
