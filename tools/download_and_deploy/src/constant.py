USER = "user"
PKEY = "pkey"
PASSWORD = "password"

SCANNER = "scanner"

C_BUILDER = "c_builder"
C_BUILDER_GCC = "c_builder_gcc"
C_BUILDER_BISHENG_COMPILER = "c_builder_bisheng_compiler"

JAVA_BUILDER = "java_builder"
JAVA_BUILDER_JDK8 = "java_builder_jdk8"
JAVA_BUILDER_JDK17 = "java_builder_jdk17"

COMPATIBILITY = "compatibility"
TESTER = "tester"

DEVKIT = "devkit"
CLAMAV = "clamav"

MACHINE = "machine"
DEPENDENCY_FILE = "devkitdependencies.tar.gz"
DEPENDENCY_DIR = "devkitdependencies"


ROLE_LIST = [
    SCANNER,
    C_BUILDER,     C_BUILDER_GCC,      C_BUILDER_BISHENG_COMPILER,
    JAVA_BUILDER,  JAVA_BUILDER_JDK8,  JAVA_BUILDER_JDK17,
    COMPATIBILITY, TESTER,
    DEVKIT,
    CLAMAV,
]

ROLE_COMPONENT = {
    SCANNER: ["DevKitCLI"],
    C_BUILDER: ["GCCforOpenEuler", "BiShengCompiler", "A-FOT", "NonInvasiveSwitching"],
    C_BUILDER_GCC: ["GCCforOpenEuler", "A-FOT"],
    C_BUILDER_BISHENG_COMPILER: ["BiShengCompiler", "NonInvasiveSwitching"],
    JAVA_BUILDER: ["BiShengJDK8", "BiShengJDK17"],
    JAVA_BUILDER_JDK8: ["BiShengJDK8"],
    JAVA_BUILDER_JDK17: ["BiShengJDK17"],
    COMPATIBILITY: ["CompatibilityTesting"],
    TESTER: ["DevKitTester"],
    DEVKIT: ["DevKitWeb"],
    CLAMAV: ["ClamAV"],
}


FILE = "file"
SHA256 = "sha256"
URL = "url"
SAVE_PATH = "save_path"
FILE_SIZE = "file_size"
DEFAULT_PATH = "./devkitdependencies"
