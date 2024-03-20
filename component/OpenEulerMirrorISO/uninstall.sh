#!/bin/bash

function resume_original_mirror() {
    rm -rf /etc/yum.repos.d/local.repo
    mv -rf /etc/yum.repos.d/yum.repos.backup/* /etc/yum.repos.d/
    rm -rf /etc/yum.repos.d/yum.repos.backup

    yum clean all
    echo "yum makecache: "
    yum makecache
}

function main() {
    umount /devkitmirror
    rm -rf /devkitmirror

    resume_original_mirror
}

main "$@"