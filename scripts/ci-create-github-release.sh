#!/usr/bin/env bash

set -euo pipefail

DECLARED_VERSION=`cat MapboxSearch/gradle.properties | grep versionName`
VERSION=${DECLARED_VERSION#versionName=}

if [[ $DECLARED_VERSION == *-SNAPSHOT ]]
then
  echo "Version $DECLARED_VERSION is a SNAPSHOT. Skip publishing."
  exit 0
fi

GITHUB_TOKEN=$(./mbx-ci github writer public token 2>/dev/null)
export GITHUB_TOKEN

git config user.email "release-bot@mapbox.com"
git config user.name "Release SDK bot"

# TODO need to make sure that available on the CI executor gh version is up-to-date
gh api --silent -X POST "/repos/{owner}/{repo}/releases" -F tag_name="v${VERSION}" -F name="Release v${VERSION}"
# TODO add version info from CHANGELOG

