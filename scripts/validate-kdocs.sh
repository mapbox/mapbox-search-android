#!/usr/bin/env bash

set -eo pipefail

echo -e "\nDocs validation..."
./gradlew dokkaHtml --rerun-tasks
