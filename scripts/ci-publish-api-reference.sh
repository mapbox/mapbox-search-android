#!/usr/bin/env bash

set -xeo pipefail

SCRIPT_PATH=$(dirname "$0")
CMAKE_ROOT="${SCRIPT_PATH}/../"
INITIAL_PATH="$PWD"

DECLARED_VERSION=`cat MapboxSearch/gradle.properties | grep VERSION_NAME`
VERSION=${DECLARED_VERSION#VERSION_NAME=}

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
mkdir -p "${TMPDIR}/sdk-common"
cp -r "${INITIAL_PATH}/MapboxSearch/sdk-common/build/dokka" "${TMPDIR}/sdk-common/${VERSION}"

mkdir -p "${TMPDIR}/core"
cp -r "${INITIAL_PATH}/MapboxSearch/sdk/build/dokka" "${TMPDIR}/core/${VERSION}"

mkdir -p "${TMPDIR}/ui"
cp -r "${INITIAL_PATH}/MapboxSearch/ui/build/dokka" "${TMPDIR}/ui/${VERSION}"

mkdir -p "${TMPDIR}/offline"
cp -r "${INITIAL_PATH}/MapboxSearch/offline/build/dokka" "${TMPDIR}/offline/${VERSION}"

mkdir -p "${TMPDIR}/autofill"
cp -r "${INITIAL_PATH}/MapboxSearch/autofill/build/dokka" "${TMPDIR}/autofill/${VERSION}"

mkdir -p "${TMPDIR}/discover"
cp -r "${INITIAL_PATH}/MapboxSearch/discover/build/dokka" "${TMPDIR}/discover/${VERSION}"

mkdir -p "${TMPDIR}/place-autocomplete"
cp -r "${INITIAL_PATH}/MapboxSearch/place-autocomplete/build/dokka" "${TMPDIR}/place-autocomplete/${VERSION}"

git config user.email "release-bot@mapbox.com"
git config user.name "Release SDK bot"

#
# Commit to branch
#
git add -A
git commit -m "[mapbox-search-android] API reference for v${VERSION}"
git push origin "${BRANCH_NAME}"
popd > /dev/null
