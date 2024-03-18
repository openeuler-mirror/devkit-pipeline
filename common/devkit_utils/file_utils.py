import os


def create_dir(target, mode=0o700):
    if os.path.exists(target):
        if os.path.isdir(target):
            return True
        else:
            return False
    old_mask = os.umask(0o000)
    os.makedirs(target, mode=mode)
    os.umask(old_mask)
    return True


def clear_dir(target):
    """
    删除目录下，所有文件，保留目录
    """
    if not os.path.exists(target):
        return
    for file in os.listdir(target):
        sub = os.path.join(target, file)
        if os.path.isfile(sub):
            os.remove(sub)
        if os.path.islink(sub):
            os.unlink(sub)
        if os.path.isdir(sub):
            clear_dir(sub)
            os.rmdir(sub)
