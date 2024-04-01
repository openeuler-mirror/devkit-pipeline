#!/bin/bash

set -e


gems_name=(zeitwerk-2.6.5.gem unicode-display_width-2.5.0.gem tzinfo-2.0.5.gem tins-1.31.1.gem term-ansicolor-1.7.1.gem sync-0.5.0.gem
simplecov-rcov-0.3.1.gem simplecov-html-0.12.3.gem simplecov-0.21.2.gem simplecov_json_formatter-0.1.4.gem ruby-progressbar-1.11.0.gem
rubocop-ast-1.17.0.gem rubocop-1.12.1.gem rspec-support-3.12.0.gem rspec-mocks-3.12.0.gem rspec-expectations-3.12.0.gem
rspec-core-3.12.0.gem rspec-3.12.0.gem rexml-3.2.5.gem regexp_parser-2.6.0.gem rchardet-1.8.0.gem rainbow-3.1.1.gem public_suffix-4.0.7.gem
parser-3.1.2.1.gem parallel-1.22.1.gem minitest-5.15.0.gem i18n-1.12.0.gem gnuplot-2.6.2.gem git-1.7.0.gem docile-1.4.0.gem diff-lcs-1.5.0.gem
concurrent-ruby-1.1.10.gem ci_reporter-2.0.0.gem bundler-2.2.33.gem builder-3.2.4.gem ast-2.4.2.gem activesupport-6.1.7.gem)

function main() {
  lkp_tar=/tmp/devkitdependencies/lkp-tests.tar.gz
  gem_zip=/tmp/devkitdependencies/gem_dependencies.zip
  if [[ ! -d /usr/share/gems/gems/gem_dependencies ]]; then
    unzip -d /usr/share/gems/gems ${gem_zip}
  fi
  cd /usr/share/gems/gems/gem_dependencies
  for each in "${gems_name[@]}"; do
    gem install --local ${each}
  done
  mkdir -p "${HOME}"/.local/
  tar --no-same-owner -zxf ${lkp_tar} -C "${HOME}"/.local/
  cd "${HOME}"/.local/lkp-tests/
  chmod +x "${HOME}"/.local/lkp-tests/bin/lkp
  make

  chmod 777 "${HOME}"/.local/lkp-tests/programs/compatibility-test/run
  ln -s "${HOME}"/.local/lkp-tests/programs/compatibility-test/run "${HOME}"/.local/lkp-tests/tests/compatibility-test

  cd "${HOME}"/.local/lkp-tests/programs/compatibility-test/
  lkp split "${HOME}"/.local/lkp-tests/programs/compatibility-test/jobs/compatibility-test.yaml

}
main "$@"