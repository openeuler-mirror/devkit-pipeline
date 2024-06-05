from handler.handler_and_node import Handler
from pipeline_script_generator.jenkins_generator import JenkinsScript


class GeneratePipelineScript(Handler):
    def handle(self, data) -> bool:
        j = JenkinsScript(data)
        with open("./xxx.jenkins", "w+", encoding="utf8") as f:
            f.write(j.generate())
        return True
