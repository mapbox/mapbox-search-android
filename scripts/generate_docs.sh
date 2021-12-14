#!/usr/bin/env bash

set -eo pipefail

pushd MapboxSearch/

searchVersion="$(grep 'versionName' gradle.properties | sed 's/^versionName=//')"

./gradlew clean

./gradlew :sdk:dokkaHtml
./gradlew :ui:dokkaHtml

mkdir -p build/generated-docs/core/$searchVersion
cp -r sdk/build/dokka/ build/generated-docs/core/$searchVersion

mkdir -p build/generated-docs/ui/$searchVersion
cp -r ui/build/dokka/ build/generated-docs/ui/$searchVersion

popd
