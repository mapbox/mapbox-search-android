#!/usr/bin/env bash

set -eo pipefail

pushd MapboxSearch/

searchVersion="$(grep 'VERSION_NAME' gradle.properties | sed 's/^VERSION_NAME=//')"

./gradlew clean

./gradlew :sdk-common:dokkaHtml
./gradlew :sdk:dokkaHtml
./gradlew :ui:dokkaHtml
./gradlew :offline:dokkaHtml
./gradlew :autofill:dokkaHtml
./gradlew :discover:dokkaHtml
./gradlew :place-autocomplete:dokkaHtml

mkdir -p build/generated-docs/sdk-common/$searchVersion
cp -r sdk-common/build/dokka/ build/generated-docs/sdk-common/$searchVersion

mkdir -p build/generated-docs/core/$searchVersion
cp -r sdk/build/dokka/ build/generated-docs/core/$searchVersion

mkdir -p build/generated-docs/ui/$searchVersion
cp -r ui/build/dokka/ build/generated-docs/ui/$searchVersion

mkdir -p build/generated-docs/offline/$searchVersion
cp -r offline/build/dokka/ build/generated-docs/offline/$searchVersion

mkdir -p build/generated-docs/autofill/$searchVersion
cp -r autofill/build/dokka/ build/generated-docs/autofill/$searchVersion

mkdir -p build/generated-docs/discover/$searchVersion
cp -r discover/build/dokka/ build/generated-docs/discover/$searchVersion

mkdir -p build/generated-docs/place-autocomplete/$searchVersion
cp -r place-autocomplete/build/dokka/ build/generated-docs/place-autocomplete/$searchVersion

popd
