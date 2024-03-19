#!/bin/bash

function configure_local_mirror() {
    local iso_file_path=$1
    mkdir -p /devkitmirror
    mount ${iso_file_path} /devkitmirror -o loop
    if [[ -d /etc/yum.repos.d/yum.repos.backup ]]; then
        mv -f /etc/yum.repos.d/yum.repos.backup /etc/yum.repos.backup
    else
        mkdir -p /etc/yum.repos.backup
    fi

    ls /etc/yum.repos.d/*
    if [[ "$?" == "0" ]]; then
        mv -f /etc/yum.repos.d/* /etc/yum.repos.backup
    fi

    mv -f /etc/yum.repos.backup /etc/yum.repos.d/
    cat > /etc/yum.repos.d/local.repo <<'EOF'
[local]
name=local.repo
baseurl=file:///devkitmirror
enabled=1
gpgcheck=0
EOF
    yum clean all
    echo "yum makecache: "
    yum makecache
}

function main() {
    iso_file_path=$1
    if [[ -f ${iso_file_path} ]]; then
        configure_local_mirror ${iso_file_path}
    else
        echo "Failed to find iso file."
    fi
}

main "$@"
