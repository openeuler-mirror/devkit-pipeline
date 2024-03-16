import os
import json

COMPATIBILITY_REPORT_ENTRIES = ["id", "result", "reason", "evidence"]
COMPATIBILITY_REPORT_TEMPLATE_HOLDER = "/** compatibility-test-report **/"
COMPATIBILITY_JSON_NAME = "log.json"

def compatibility_result_to_html(compatibility_json_path):
    compatibility_result = []
    compatibility_result.extend(COMPATIBILITY_REPORT_ENTRIES)
    with open(os.path.join(compatibility_json_path, COMPATIBILITY_JSON_NAME), "r") as file:
        data = json.load(file)
        for item in data:
            compatibility_result.extend([item.get(entry) for entry in COMPATIBILITY_REPORT_ENTRIES])
    
    compatibility_report = json.dumps(compatibility_result)
    with open("template.html", "r") as file:
        html_lines = file.readlines()
        res = [sub.replace(COMPATIBILITY_REPORT_TEMPLATE_HOLDER, compatibility_report) for sub in html_lines]
        with open(os.path.join(compatibility_json_path, "compatibility_report.html"), "w") as f:
            f.writelines(res)

compatibility_result_to_html("./")