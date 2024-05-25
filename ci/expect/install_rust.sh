#!/bin/expect

set timeout 30

# shellcheck disable=SC2121
set RUSTUP_DIST_SERVER https://mirrors.ustc.edu.cn/rust-static
# shellcheck disable=SC2121
set RUSTUP_UPDATE_ROOT https://mirrors.ustc.edu.cn/rust-static/rustup

spawn bash rust.sh
expect "Cancel installation"
send "1"

expect "Cancel installation"
send "Y"

expect eof
exit