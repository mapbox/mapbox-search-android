#!/usr/bin/env bash

set -eo pipefail

pushd MapboxSearch/

./gradlew clean

./gradlew :sdk-common:mapboxSDKRegistryUpload
./gradlew :base:mapboxSDKRegistryUpload
./gradlew :sdk:mapboxSDKRegistryUpload
./gradlew :ui:mapboxSDKRegistryUpload
./gradlew :offline:mapboxSDKRegistryUpload
./gradlew :autofill:mapboxSDKRegistryUpload

popd
