import os
import zipfile
import tarfile
import argparse


class AcceptanceTool(object):
    def devkit_acceptance_report(self, compressed_report_package):
        if not os.path.exists(compressed_report_package):
            print("请输入正确的报告压缩包")
            return
        file_ext = os.path.splitext(compressed_report_package)[1].lower()
        if file_ext not in (".zip", ".gz", ".bz2"):
            print("请使用以下格式的压缩包：zip、gz、bz2")
            return
        report_name = compressed_report_package.split("/")[-1].split("_")
        devkit_pipeline_name = ""
        devkit_pipeline_id = ""
        if len(report_name) == 3:
            devkit_pipeline_name = report_name[0]
            devkit_pipeline_id = report_name[1]
        decompress = {".zip": decompress_zip, ".gz": decompress_gz_bz, ".bz2": decompress_gz_bz}
        current_path = os.getcwd()
        print("开始解压")
        file_names = decompress.get(file_ext)(compressed_report_package)
        command_line_html = {"64-bit-running-mode-check.html": "64位运行模式检查",
                             "memory-consistency-check.html": "内存一致性检查",
                             "SoftwareMigrationAssessment.html": "软件迁移评估",
                             "byte-alignment-check.html": "字节对齐检查", "SourceCodeScanningReport.html": "源码迁移",
                             "compatibility_report.html": "云测工具"}
        print("解压完成。")
        print("流水线{}构建{}devkit-pipeline相关工具报告扫描中...").format(devkit_pipeline_name, devkit_pipeline_id)
        html_line = ""
        contents = ""
        for file in file_names:
            if file.split("/")[-1] in command_line_html.keys():
                try:
                    with open(os.path.join(current_path, file), encoding="utf-8") as f:
                        content = f.readlines()
                except UnicodeDecodeError:
                    with open(os.path.join(current_path, file), encoding="gbk") as f:
                        content = f.readlines()
                if file.split("/")[-1] != "compatibility_report.html":
                    flag = 0
                    for html_line in content:
                        if "Source File Path" in html_line and file != "SoftwareMigrationAssessment.html":
                            flag += 1
                            continue
                        elif "Software Package Path or Name" in html_line:
                            flag += 1
                            continue
                if flag == 1:
                    html_line = \
                        html_line.replace("""<span class="info">""", "").replace("""</span>""", "").strip().split("/")[
                            -1]
                    break
                else:
                    for html_line in content:
                        if "Compatibility_Application_Start" in html_line:
                            str1 = html_line.find("7528")
                            str2 = html_line.find("542f")
                            html_line = html_line[str1 + 3 + 1: str2 - 2]
                            break
                output_content = """{}:
                        报告路径：{}
                        被扫描软件名称：{}""".format(command_line_html.get(file.split("/")[-1]),
                                                    os.path.join(current_path, file), html_line)
                print(output_content)
                contents += "<li>{}</li>".format(output_content)

        if not html_line:
            print("""\033[31m未发现的devkit-pipeline相关工具报告、033[0m""")

        html_contents = '<html><body><div style="display:flex;border-bottom: 1px dashed;justify-content:center;margin-bottom:10px;"><h1>Acceptance report</h1></div><ul>{}</ul></body></html>'.format(
            contents)
        with open('./{}_{}_htmlreports.html'.format(devkit_pipeline_name, devkit_pipeline_id), 'w') as f:
            f.write(html_contents)


def decompress_zip(compressed_report_package):
    with zipfile.ZipFile(compressed_report_package) as zip:
        zip.extractall("./")
        file_names = zip.namelist()
        return file_names


def decompress_gz_bz(compressed_report_package):
    with tarfile.open(compressed_report_package, "r") as tar:
        tar.extractall(path="./")
        file_names = tar.getnames()
        return file_names


if __name__ == "__main__":
    try:
        parser = argparse.ArgumentParser()
        parser.add_argument('-tool', help='DevKit or BoostKit')
        parser.add_argument('-package', help='Compressed package')
        args = parser.parse_args()
        acceptance_tool = AcceptanceTool()
        if args.tool == "DevKit":
            acceptance_tool.devkit_acceptance_report(args.package)
        elif args.tool == "BoostKit":
            pass
        else:
            print("请输入正确的参数，如-tool Devkit 或 -tool BoostKit")
    except Exception as err:
        print(err)
        print("请输入正确的参数")