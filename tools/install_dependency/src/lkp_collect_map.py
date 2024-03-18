import os.path

from utils import base_path

lkp_collection_map = {
    "LkpTests": {
        "download file": {
            "save_path": f"{os.path.join(base_path('component'), 'LkpTests', 'lkp-tests.tar.gz')}",
        },
        "download gem dependency": {
            "save_path": f"{os.path.join(base_path('component'), 'LkpTests', 'gem_dependencies.zip')}",
        },
    },
    "CompatibilityTesting": {
        "download file": {
            "save_path": f"{os.path.join(base_path('component'), 'LkpTests', 'compatibility_testing.tar.gz')}",
        }
    },
    "DevkitDistribute": {
        "download file": {
            "save_path": f"{os.path.join(base_path('component'), 'DevkitDistribute', 'devkit_distribute.tar.gz')}",
        }
    }
}
