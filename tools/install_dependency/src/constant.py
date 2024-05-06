USER = "user"
PKEY = "pkey"
PASSWORD = "password"
SCANNER = "scanner"
JAVA_BUILDER_JDK8 = "java_builder_jdk8"
JAVA_BUILDER_JDK17 = "java_builder_jdk17"
C_BUIDLER = "c_cpp_builder"
C_BUIDLER_GCC = "c_cpp_builder_gcc"
C_BUIDLER_BISHENG_COMPILER = "c_cpp_builder_bisheng_compiler"
EXECUTOR = "executor"
DEVKIT = "devkit"
MACHINE = "machine"
DEPENDENCY_FILE = "devkitdependencies.tar.gz"
DEPENDENCY_DIR = "devkitdependencies"

INSTRUCTION = "instruction"

ROLE_COMPONENT = {
    SCANNER: ["BiShengJDK17", "DevKitCLI"],
    C_BUIDLER: ["GCCforOpenEuler", "BiShengCompiler", "BiShengJDK17", "A-FOT", "NonInvasiveSwitching"],
    C_BUIDLER_GCC: ["GCCforOpenEuler",  "BiShengJDK17", "A-FOT"],
    C_BUIDLER_BISHENG_COMPILER: ["BiShengCompiler", "BiShengJDK17", "NonInvasiveSwitching"],
    JAVA_BUILDER_JDK8: ["BiShengJDK8", "BiShengJDK17"],
    JAVA_BUILDER_JDK17: ["BiShengJDK17"],
    EXECUTOR: ["BiShengJDK17", "LkpTests", "CompatibilityTesting", "DevkitDistribute"],
    DEVKIT: ["DevKitWeb"]
}

ROLE_LIST = [SCANNER, C_BUIDLER, C_BUIDLER_GCC, C_BUIDLER_BISHENG_COMPILER, JAVA_BUILDER_JDK8, JAVA_BUILDER_JDK17, EXECUTOR, DEVKIT]

FILE = "file"
SHA256 = "sha256"
URL = "url"
SAVE_PATH = "save_path"
FILE_SIZE = "file_size"
DEFAULT_PATH = "./devkitdependencies"
