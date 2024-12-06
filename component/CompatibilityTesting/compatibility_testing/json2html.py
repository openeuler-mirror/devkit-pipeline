import os
import json

COMPATIBILITY_REPORT_ENTRIES = ["id", "result", "reason", "evidence"]
COMPATIBILITY_REPORT_TEMPLATE_HOLDER = "/** compatibility-test-report **/"
COMPATIBILITY_JSON_NAME = "log.json"


def compatibility_result_to_html():
    compatibility_result = []
    compatibility_result.extend(COMPATIBILITY_REPORT_ENTRIES)
    with open(os.path.join(os.getcwd(), COMPATIBILITY_JSON_NAME), "r") as file:
        data = json.load(file)

        for item in data:
            compatibility_result.extend([item.get(entry) for entry in COMPATIBILITY_REPORT_ENTRIES])

    compatibility_report = json.dumps(compatibility_result)
    with open("template.html", "r") as input_file, open("compatibility_report.html", "w") as output_file:
        html_lines = input_file.readlines()
        temp = html_lines[21]
        html_lines[21] = temp.replace(COMPATIBILITY_REPORT_TEMPLATE_HOLDER, compatibility_report)
        output_file.writelines(html_lines)
    
        

compatibility_result_to_html()
