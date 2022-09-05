#!/usr/bin/env bash

set -euo pipefail

SCRIPT_PATH=$(dirname "$0")
CMAKE_ROOT="${SCRIPT_PATH}/../"
INITIAL_PATH="$PWD/scripts"

DECLARED_VERSION=`cat MapboxSearch/gradle.properties | grep VERSION_NAME`
VERSION=${DECLARED_VERSION#VERSION_NAME=}

if [[ $DECLARED_VERSION == *-SNAPSHOT ]]
then
  echo "Version $DECLARED_VERSION is a SNAPSHOT. Skip publishing."
  exit 0
fi

GITHUB_TOKEN=$(./mbx-ci github writer token 2>/dev/null)
export GITHUB_TOKEN

BRANCH_NAME="mapbox-search-android/${VERSION}"

TMPDIR=$(mktemp -d)
trap 'rm -rf "${TMPDIR}"' INT TERM HUP EXIT # Remove folder on exit or error

git clone "https://x-access-token:${GITHUB_TOKEN}@github.com/mapbox/api-downloads.git" "${TMPDIR}"
pushd "${TMPDIR}"
echo "Checking out to ${TMPDIR}"
git checkout -b "${BRANCH_NAME}"

#
# Copy config files
#

# Config for search-sdk-common is Android-specific, iOS doesn't have it
cp "$INITIAL_PATH/sdk-registry-config-templates/search-sdk-common-android.yaml" "${TMPDIR}/config/search-sdk-common/${VERSION}.yaml"
# For iOS, both SDK and UI SDK artifacts are described in a `search-sdk` config
cp "$INITIAL_PATH/sdk-registry-config-templates/search-ui-sdk-android.yaml" "${TMPDIR}/config/search-ui-sdk/${VERSION}.yaml"

mkdir -p "${TMPDIR}/config/search-autofill-sdk"
cp "$INITIAL_PATH/sdk-registry-config-templates/search-autofill-android.yaml" "${TMPDIR}/config/search-autofill-sdk/${VERSION}.yaml"

if grep -q ios "${TMPDIR}/config/search-sdk/${VERSION}.yaml"; then
  cp "$INITIAL_PATH/sdk-registry-config-templates/search-sdk-android-ios.yaml" "${TMPDIR}/config/search-sdk/${VERSION}.yaml"
else
  cp "$INITIAL_PATH/sdk-registry-config-templates/search-sdk-android.yaml" "${TMPDIR}/config/search-sdk/${VERSION}.yaml"
fi

set +u
if [ "$1" = "--dryRun" ]; then
  echo "Dry run, exit..."
  exit 0
fi
set -u

git config user.email "release-bot@mapbox.com"
git config user.name "Release SDK bot"

#
# Commit to branch
#
git add -A
git commit -m "[mapbox-search-android] Release v${VERSION}"
git push --set-upstream origin "${BRANCH_NAME}" --force-with-lease

#
# Create PR
# Requires that GITHUB_TOKEN environment variable is set
#
TITLE="[mapbox-search-android] add config for ${VERSION}"
BODY="* Update configuration for Search SDK for Android"

echo ">>> Creating PR to mapbox/api-downloads"
gh pr create --title "${TITLE}" --body "${BODY}" --reviewer DzmitryFomchyn,sarochych,globaltrouble,4rtzel
popd > /dev/null
