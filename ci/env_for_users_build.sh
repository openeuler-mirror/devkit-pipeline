#!/bin/bash

# shellcheck disable=SC2129
# shellcheck disable=SC2016
echo 'export PATH=/opt/apache-maven-3.9.6/bin:$PATH' >>~/.bashrc
echo 'export PATH=/opt/python3/bin:/opt/local/bin:$PATH' >>~/.bashrc
echo 'export LD_LIBRARY_PATH=/opt/python3/lib:/opt/local/lib:$LD_LIBRARY_PATH' >>~/.bashrc

mkdir "${HOME}/.m2"
cp maven3/settings.xml "${HOME}/.m2/settings.xml"