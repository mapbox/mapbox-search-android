#!/usr/bin/env bash

set -eoux pipefail

# upload report to codecov.io
pip3 install --user codecov

# use `env -i` to prevent leakage of any environment variables to codecov's script
env -i python3 -m \
    codecov \
    --root "$(pwd)" \
    --token ${CODECOV_TOKEN} \
    --file "$1" \
    --required || echo 'Codecov failed to upload'
