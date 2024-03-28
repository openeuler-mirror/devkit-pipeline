USER = "user"
PKEY = "pkey"
PASSWORD = "password"
SCANNER = "scanner"
JAVA_BUILDER = "java_builder"
C_BUIDLER = "c_cpp_builder"
EXECUTOR = "executor"
DEVKIT = "devkit"
MACHINE = "machine"
DEPENDENCY_FILE = "devkitdependencies.tar.gz"
DEPENDENCY_DIR = "devkitdependencies"

INSTRUCTION = "instruction"

ROLE_COMPONENT = {
    SCANNER: ["BiShengJDK17"],
    C_BUIDLER: ["GCCforOpenEuler", "BiShengCompiler", "BiShengJDK17", "A-FOT", "NonInvasiveSwitching"],
    JAVA_BUILDER: ["BiShengJDK17", "BiShengJDK8"],
    EXECUTOR: ["BiShengJDK17", "LkpTests"]
}

ROLE_LIST = [SCANNER, C_BUIDLER, JAVA_BUILDER, EXECUTOR]

FILE = "file"
SHA256 = "sha256"
URL = "url"
SAVE_PATH = "save_path"
DEFAULT_PATH = "./devkitdependencies"
