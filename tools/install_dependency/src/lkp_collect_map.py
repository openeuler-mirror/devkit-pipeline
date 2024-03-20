import os.path

from utils import base_path

CURRENT_DEFAULT_PATH = "./devkitdependencies"

lkp_collection_map = {
    "LkpTests": {
        "download file": {
            "save_path": f"{os.path.join(CURRENT_DEFAULT_PATH, 'lkp-tests.tar.gz')}",
        },
        "download gem dependency": {
            "save_path": f"{os.path.join(CURRENT_DEFAULT_PATH, 'gem_dependencies.zip')}",
        },
    },
    "CompatibilityTesting": {
        "download file": {
            "save_path": f"{os.path.join(CURRENT_DEFAULT_PATH, 'compatibility_testing.tar.gz')}",
        }
    },
    "DevkitDistribute": {
        "download file": {
            "save_path": f"{os.path.join(base_path('component'), 'DevkitDistribute', 'devkit_distribute.tar.gz')}",
        }
    }
}
