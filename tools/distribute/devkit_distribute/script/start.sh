#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

# shellcheck disable=SC2154
"${root_path}/bin/entrance" -i "${ips_list}" -u "${user}" -P "${port}" -f "${pkey_file}" --duration "${duration}" --app "${applications}" \
  --devkit-ip "${devkit_ip}" --devkit-port "${devkit_port}" --devkit-password "${devkit_password}" --devkit-user "${devkit_user}" \
  --pkey-password "${pkey_password}" --git-path "${git_path}"