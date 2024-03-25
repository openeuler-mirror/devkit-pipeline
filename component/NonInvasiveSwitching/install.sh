#!/bin/bash
set -e
function main() {
    cat > /usr/local/wrap-bin/clang <<'EOF'
set -x
"${HOME}"/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang "$@" -mcpu=tsv110 --O2 -g
EOF
      cat > /usr/local/wrap-bin/clang++ <<'EOF'
set -x
"${HOME}"/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang++ "$@" -mcpu=tsv110 --O2 -g
EOF
      cat > /usr/local/wrap-bin/gcc <<'EOF'
set -x
"${HOME}"/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang "$@" -mcpu=tsv110 --O2 -g
EOF
      cat > /usr/local/wrap-bin/g++ <<'EOF'
set -x
"${HOME}"/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang++ "$@" -mcpu=tsv110 --O2 -g
EOF
chmod +x /usr/local/wrap-bin/clang
chmod +x /usr/local/wrap-bin/clang++
chmod +x /usr/local/wrap-bin/gcc
chmod +x /usr/local/wrap-bin/g++

  cat > /usr/local/wrap-bin/devkit_pipeline.sh <<'EOF'

export PATH=/usr/local/wrap-bin:/usr/local/bin:$PATH
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
EOF
chmod 755 /usr/local/wrap-bin/devkit_pipeline.sh
}

if [ -d "/usr/local/wrap-bin" ]; then
  main
else
  mkdir -p /usr/local/wrap-bin
  main
fi

