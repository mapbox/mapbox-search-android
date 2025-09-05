#!/usr/bin/env bash

set -eo pipefail

VERSION_NAME=`cat MapboxSearch/gradle.properties | grep VERSION_NAME`
if [[ "$VERSION_NAME" == *-SNAPSHOT* ]]
  then
    echo "Publishing snapshots to the SDK registry..."
    source scripts/ci-publish.sh
  else
    echo "Current version is not SNAPSHOT. Skip publishing."
fi
