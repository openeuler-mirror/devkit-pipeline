from constant import *


class ScriptGenerator:
    base_template = ""
    source_migration_template = ""
    package_migration_template = ""
    mode_check_template = ""
    byte_alignment_template = ""
    memory_consistency_template = ""
    vector_check_template = ""
    build_template = ""
    bisheng_compiler_template = ""
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
            C_BUIDLER: [self.build_template, self.a_fot_template, self.bisheng_compiler_template],
            C_BUIDLER_GCC: [self.build_template, self.a_fot_template],
            C_BUIDLER_BISHENG_COMPILER: [self.bisheng_compiler_template],
            EXECUTOR: [self.compatibility_test_template, self.java_perf_template]
        }

    def generate(self):
        separator = "#"*120 + "\n"
        conf = separator
        for role in ROLE_LIST:
            if role not in self.data:
                continue
            if C_BUIDLER in self.data and (role == C_BUIDLER_GCC or role == C_BUIDLER_BISHENG_COMPILER):
                continue
            for temp in self.ROLE_FUNCTION.get(role, []):
                conf += temp + separator
        return self.base_template.replace("##STAGES##", conf)
