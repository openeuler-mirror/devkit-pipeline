from handler.handler_and_node import Handler
from pipeline_script_generator.jenkins_generator import JenkinsScript
from pipeline_script_generator.gitlab_generator import GitlabScript

generator_class = (JenkinsScript, GitlabScript)

class GeneratePipelineScript(Handler):
    def handle(self, data) -> bool:
        for klass in generator_class:
            o = klass(data)
            with open("./script." + o.name, "w+", encoding="utf8") as f:
                f.write(o.generate())
        return True
