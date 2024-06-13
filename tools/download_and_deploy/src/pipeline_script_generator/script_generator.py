from constant import *


class ScriptGenerator:
    name = ""
    seprator = "/"
    base_template = ""
    source_migration_template = ""
    package_migration_template = ""
    mode_check_template = ""
    byte_alignment_template = ""
    memory_consistency_template = ""
    vector_check_template = ""
    gcc_template = ""
    bisheng_compiler_template = ""
    java8_build_template = ""
    java17_build_template = ""
    a_fot_template = ""
    java_perf_template = ""
    compatibility_test_template = ""
    clamav_template = ""

    def __init__(self, data):
        self.data = data
        self.ROLE_FUNCTION = {
            SCANNER: [
                self.source_migration_template,
                self.package_migration_template,
                self.mode_check_template,
                self.byte_alignment_template,
                self.memory_consistency_template,
                self.vector_check_template
            ],
            C_BUILDER_GCC: [self.gcc_template, self.a_fot_template],
            C_BUILDER_BISHENG_COMPILER: [self.bisheng_compiler_template],
            JAVA_BUILDER_JDK8: [self.java8_build_template],
            JAVA_BUILDER_JDK17: [self.java17_build_template],
            COMPATIBILITY: [self.compatibility_test_template],
            CLAMAV: [self.clamav_template]
        }

    def generate(self):
        separator = self.seprator * 120 + "\n"
        conf = separator
        for role in ROLE_LIST:
            if role not in self.data:
                continue
            for temp in self.ROLE_FUNCTION.get(role, []):
                conf += temp + separator
        return self.base_template.replace("##STAGES##", conf)
