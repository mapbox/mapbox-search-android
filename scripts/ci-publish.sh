#!/usr/bin/env bash

set -eo pipefail

pushd MapboxSearch/

modules=("sdk-common" "base" "sdk" "ui" "offline" "autofill" "discover" "place-autocomplete")

./gradlew clean
for module in "${modules[@]}"; do
  ./gradlew ":$module:mapboxSDKRegistryUpload"
done

./gradlew clean
for module in "${modules[@]}"; do
  ./gradlew ":$module:mapboxSDKRegistryUpload" "-PndkMajor=27"
done

popd
