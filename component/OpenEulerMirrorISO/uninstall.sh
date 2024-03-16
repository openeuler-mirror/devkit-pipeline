#!/bin/bash

function resume_original_mirror() {
    rm -rf /etc/yum.repos.d/local.repo
    mv -rf /etc/yum.repos.d/yum.repos.backup/* /etc/yum.repos.d/
    rm -rf /etc/yum.repos.d/yum.repos.backup

    yum clean all
    yum makecache
}

function main() {
    iso_file_path=$1
    rm -rf "${iso_file_path}"
    umount /devkitmirror
    rm -rf /devkitmirror

    resume_original_mirror
}

main "$@"