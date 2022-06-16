#!/usr/bin/env bash

set -eo pipefail

pushd MapboxSearch/

./gradlew clean

./gradlew :sdk:mapboxSDKRegistryUpload
./gradlew :ui:mapboxSDKRegistryUpload
./gradlew :sdk-common:mapboxSDKRegistryUpload
./gradlew :autofill:mapboxSDKRegistryUpload

popd
