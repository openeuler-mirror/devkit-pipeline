USER = "user"
PKEY = "pkey"
PASSWORD = "password"
SCANNER = "scanner"
BUILDER = "builder"
EXECUTOR = "executor"
DEVKIT = "devkit"
MACHINE = "machine"
DEPENDENCY_FILE = "devkitdependencies.tar.gz"
DEPENDENCY_DIR = "devkitdependencies"

INSTRUCTION = "instruction"

ROLE_COMPONENT = {
    SCANNER: ["BiShengJDK17"],
    BUILDER: ["GCCforOpenEuler", "BiShengCompiler", "BiShengJDK17", "BiShengJDK8"],
    EXECUTOR: ["BiShengJDK17", "LkpTests"]
}

ROLE_LIST = [SCANNER, BUILDER, EXECUTOR]

FILE = "file"
SHA256 = "sha256"
URL = "url"
SAVE_PATH = "save_path"
DEFAULT_PATH = "./devkitdependencies"
