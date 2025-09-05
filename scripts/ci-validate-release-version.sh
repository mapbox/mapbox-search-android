#!/usr/bin/env bash

set -eo pipefail

DECLARED_VERSION_NAME=`cat MapboxSearch/gradle.properties | grep VERSION_NAME`

extract_version_name() {
  [[ $1 =~ (VERSION_NAME=)?v?(.*) ]]
  local VERSION=${BASH_REMATCH[2]}
  echo "$VERSION"
}

declaredVersionName=`extract_version_name "$DECLARED_VERSION_NAME"`
tagVersionName=`extract_version_name $CIRCLE_TAG`

if [ "$declaredVersionName" != "$tagVersionName" ]; then
  echo "Declared SDK version name is not equal to the version specified in git tag. Declared: $DECLARED_VERSION_NAME, tag: $CIRCLE_TAG"
  exit 1
fi

if [[ $declaredVersionName == *-SNAPSHOT* ]]
then
  echo "Version $declaredVersionName is a SNAPSHOT. Skip publishing."
  exit 1
fi
