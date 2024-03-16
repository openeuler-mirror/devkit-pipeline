import os.path

import constant
from utils import base_path

lkp_collection_map = {
    "LkpTests": {
        "download file": {
            "save_path": f"{os.path.join('./', constant.DEPENDENCY_DIR, 'lkp-tests.tar.gz')}",
        },
        "download gem dependency": {
            "save_path": f"{os.path.join(base_path('component'), 'LkpTests', 'gem_dependencies.zip')}",
        },
    },
    "CompatibilityTesting": {
        "download file": {
            "save_path": f"{os.path.join('./', constant.DEPENDENCY_DIR, 'compatibility_testing.tar.gz')}",
        }
    },
    "DevkitDistribute": {
        "download file": {
            "save_path": f"{os.path.join(base_path('component'), 'devkit_distribute.tar.gz')}",
        }
    }
}
