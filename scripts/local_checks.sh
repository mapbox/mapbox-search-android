#!/usr/bin/env bash

set -eo pipefail

python scripts/license-validate.py

pushd MapboxSearch/

./gradlew :base:ktlintFormat
./gradlew :sdk:ktlintFormat
./gradlew :ui:ktlintFormat
./gradlew :offline:ktlintFormat
./gradlew :sdk-common:ktlintFormat
./gradlew :autofill:ktlintFormat
./gradlew :discover:ktlintFormat
./gradlew :place-autocomplete:ktlintFormat
./gradlew :sample:ktlintFormat

./gradlew :base:lint
./gradlew :sdk:lint
./gradlew :ui:lint
./gradlew :offline:lint
./gradlew :sdk-common:lint
./gradlew :autofill:lint
./gradlew :discover:lint
./gradlew :place-autocomplete:lint
./gradlew :sample:lint

./gradlew detektAll

../scripts/public-api.sh --check

../scripts/validate-kdocs.sh

./gradlew :base:testReleaseUnitTest
./gradlew :sdk:testReleaseUnitTest
./gradlew :ui:testReleaseUnitTest
./gradlew :offline:testReleaseUnitTest
./gradlew :sdk-common:testReleaseUnitTest
./gradlew :autofill:testReleaseUnitTest
./gradlew :discover:testReleaseUnitTest
./gradlew :place-autocomplete:testReleaseUnitTest

# Check only whether PIT setup doesn't fail.
# Full PIT test run may be executed locally or via dedicated nightly CI task.
#../scripts/quick_pitest_check.sh

# To provide argument: .../local_checks.sh --runInstrumentationTests
if [ "$1" = "--runInstrumentationTests" ]; then
  # TODO start emulator if there's no active
  ./gradlew connectedAndroidTest mergeAndroidReports --continue
  ./gradlew connectedAndroidTest mergeAndroidReports --continue -PndkMajor=27
fi

popd
