#!/usr/bin/env bash

set -xeo pipefail

SCRIPT_PATH=$(dirname "$0")
CMAKE_ROOT="${SCRIPT_PATH}/../"
INITIAL_PATH="$PWD"

DECLARED_VERSION=`cat MapboxSearch/gradle.properties | grep versionName`
VERSION=${DECLARED_VERSION#versionName=}

if [[ $DECLARED_VERSION == *-SNAPSHOT ]]
then
  echo "Version $DECLARED_VERSION is a SNAPSHOT. Skip publishing."
  exit 0
fi

source scripts/generate_docs.sh

GITHUB_TOKEN=$(./mbx-ci github writer public token 2>/dev/null)
export GITHUB_TOKEN

TMPDIR=$(mktemp -d)
trap 'rm -rf "${TMPDIR}"' INT TERM HUP EXIT # Remove folder on exit or error

BRANCH_NAME="publisher-production"

git clone "https://x-access-token:${GITHUB_TOKEN}@github.com/mapbox/mapbox-search-android.git" "${TMPDIR}"
pushd "${TMPDIR}"
echo "Checking out $BRANCH_NAME to ${TMPDIR}"
git checkout "$BRANCH_NAME"

#
# Copy API reference
#
cp -r "${INITIAL_PATH}/MapboxSearch/sdk/build/dokka" "${TMPDIR}/core/${VERSION}"
cp -r "${INITIAL_PATH}/MapboxSearch/ui/build/dokka" "${TMPDIR}/ui/${VERSION}"

git config user.email "release-bot@mapbox.com"
git config user.name "Release SDK bot"

#
# Commit to branch
#
git add -A
git commit -m "[mapbox-search-android] API reference for v${VERSION}"
git push origin "${BRANCH_NAME}"
popd > /dev/null
