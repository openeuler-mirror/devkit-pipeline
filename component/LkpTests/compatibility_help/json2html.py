import json
import re
import os


def dict2table(ks, vs):
    th = ''
    for each in ks:
        th = th + '<th>' + each + '</th>'
    trth = '<tr>' + th + '</tr>'
    trtd = ''
    for tds in vs:
        tdss = ''
        for td in tds:
            tdss = tdss + '<td>' + str(td) + '</td>'
        tdss = '<tr>' + tdss + '</tr>'
        trtd = trtd + tdss
    return '<table>' + trth + trtd + '</table>'


colunm_content = []
homepath = os.path.expanduser('~')
json_filepath = str(homepath) + "/.local/compatibility_testing/Chinese/log.json"
html_filepath = str(homepath) + "/.local/compatibility_testing/template.html"
with open(json_filepath, 'r') as file:
    data_dict = json.load(file)
colunm_name = ["id", "evidence", "reason", "result"]
flag = 0
for each in data_dict:
    for each_key in colunm_name:
        if each_key == "result":
            if each.get(each_key) and each.get(each_key) == "failed":
                flag = -1
        if each_key not in each.keys():
            each[each_key] = ""
for each in data_dict:
    each_line = []
    for each_key in colunm_name:
        if each_key in each.keys():
            each_line.append(each.get(each_key))
        else:
            each_line.append("")
html_table = dict2table(colunm_name, colunm_content)

with open(html_filepath, 'r') as f:
    html_content = f.read()

new_html_content = re.sub("need_to_be_replaced", html_table, html_content)
with open(html_filepath, "w") as f:
    f.write(new_html_content)
print(flag)