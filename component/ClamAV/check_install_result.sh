#!/bin/bash

if which clamscan >/dev/null; then
    echo "true"
else
    echo "false"
fi