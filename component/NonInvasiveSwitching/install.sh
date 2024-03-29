#!/bin/bash
set -e
function main() {
    cat > "${HOME}"/.local/wrap-bin/clang <<'EOF'
set -x
"${HOME}"/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang "$@" -mcpu=tsv110 --O2 -g
EOF
      cat > "${HOME}"/.local/wrap-bin/clang++ <<'EOF'
set -x
"${HOME}"/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang++ "$@" -mcpu=tsv110 --O2 -g
EOF
      cat > "${HOME}"/.local/wrap-bin/gcc <<'EOF'
set -x
"${HOME}"/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang "$@" -mcpu=tsv110 --O2 -g
EOF
      cat > "${HOME}"/.local/wrap-bin/g++ <<'EOF'
set -x
"${HOME}"/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang++ "$@" -mcpu=tsv110 --O2 -g
EOF
chmod +x "${HOME}"/.local/wrap-bin/clang
chmod +x "${HOME}"/.local/wrap-bin/clang++
chmod +x "${HOME}"/.local/wrap-bin/gcc
chmod +x "${HOME}"/.local/wrap-bin/g++

  cat > "${HOME}"/.local/wrap-bin/devkit_pipeline.sh <<'EOF'

export PATH="${HOME}"/.local/wrap-bin:/usr/local/bin:$PATH
export LD_LIBRARY_PATH="${HOME}"/.local/lib:$LD_LIBRARY_PATH
EOF
chmod 755 "${HOME}"/.local/wrap-bin/devkit_pipeline.sh
}

if [ -d "${HOME}"/.local/wrap-bin ]; then
  main
else
  mkdir -p "${HOME}"/.local/wrap-bin
  main
fi

