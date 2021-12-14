#!/usr/bin/env bash

set -eo pipefail

VERSION_NAME=`cat MapboxSearch/gradle.properties | grep versionName`
if [[ "$VERSION_NAME" == *-SNAPSHOT ]]
  then
    echo "Publishing snapshots to the SDK registry..."
    source scripts/publish.sh
  else
    echo "Current version is not SNAPSHOT. Skip publishing."
fi
