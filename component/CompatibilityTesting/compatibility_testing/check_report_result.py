import os
import json

COMPATIBILITY_REPORT_ENTRIES = ["id", "result", "reason", "evidence"]
COMPATIBILITY_REPORT_TEMPLATE_HOLDER = "/** compatibility-test-report **/"
COMPATIBILITY_JSON_NAME = "log.json"

flag = 0
with open(os.path.join(os.getcwd(), "Chinese", COMPATIBILITY_JSON_NAME), "r") as file:
    data = json.load(file)
    for each in data:
        for each_key in COMPATIBILITY_REPORT_ENTRIES:
            if each_key == "result":
                if each.get(each_key) and each.get(each_key) == "failed":
                    flag = -1
                    break

print(flag)