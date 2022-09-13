# Mapbox Search SDK

[![codecov](https://codecov.io/gh/mapbox/mapbox-search-android/branch/develop/graph/badge.svg?token=KDZTqlSKlh)](https://codecov.io/gh/mapbox/mapbox-search-android)

## Table of contents

- [Overview](#overview)
- [Main features](#main-features)
- [Setup environment](#setup-environment)
- [Configure credentials](#configure-credentials)
- [Sample code](#sample-code)
- [Custom dictionary](#custom-dictionary)
- [Codestyle](#codestyle)
- [Code analysis](#code-analysis)
- [Public API changes tracking](#public-api-changes-tracking)
- [Testing](#testing)
- [Offline integration tests](#offline-integration-tests)
- [Complex checks run](#complex-checks-run)
- [Documentation](#documentation)
- [Third-party SDKs license](#third-party-sdks-license)
- [Contributing](#contributing)
- [Versioning](#versioning)

# Overview

The Mapbox Search SDK is a developer toolkit to add location search on mobile devices.
With the same speed and scale of the Mapbox Search API, the SDK is built specifically for on-demand and local search use cases, like ride-share, food delivery, and store finders apps.
Whether your users are trying to find a place among the vast amount of data on a global map, or to find the exact location of a venue a few miles down the road, the Search SDK provides location search for countries all over the globe, in many different languages.

Previously, implementing search into your application required custom tuning with every API request to set a language, location biasing, and result types.
There was no pre-built UI and no option for a user to see their search history, or save favorites.

The Mapbox Search SDK allows you to drop pre-tuned search into your application, removing the complexity of API configuration, while still giving you control to customize.
It ships with an optional UI framework, or you can build a completely custom implementation with your own UI elements by using the core library.
The Search SDK is pre-configured for autocomplete, local search biasing, and includes new features like category search, search history, and search favorites.


## Main features

- Easy-to-use pre-tuned search options to integrate search into your app quickly.
  - Local search for a specific address or POI
- Pre-configured and customizable category search for popular categories like cafes, ATMs, hotels, and gas stations.
- Offline search (private beta)
- Address Autofill
- On-device user search history
- On-device favorites
- Import/export customer data with your own protocols
- Provide you own persistent providers for customer data like History or Favorites

Visit [Search SDK documentation page](https://docs.mapbox.com/android/search/overview/) for more information.


## Setup environment
Below are listed the versions on which everything is going to work fine. Other versions might work as well but wasn't tested yet.
- Android Studio
- Java 11


## Configure credentials
Before installing the SDK, you will need to gather two pieces of sensitive information from your Mapbox account. If you don't have a Mapbox account: [sign up](https://account.mapbox.com/auth/signup/) and navigate to your [Account page](https://account.mapbox.com/). You'll need:
1. A public access token: From your [account's tokens page](https://account.mapbox.com/access-tokens/), you can either copy your default public token or click the Create a token button to create a new public token.
2. A secret access token with the `Downloads:Read` scope.

Export your public token as an environment variable `MAPBOX_ACCESS_TOKEN` and your secret access token as an environment variable `SDK_REGISTRY_TOKEN`. Alternatively, you can provide those credentials as project properties.


## Sample code
Examples for Mapbox Search SDK for Android are available [here](MapboxSearch/sample).


## Custom dictionary
Don't forget to add custom dictionary file located at `AndroidStudio/dictionary.dic` to Android Studio: `Preferences -> Editor -> Spelling -> Add custom dictionaries`. Update this file with correct words that are not contained in the default dictionary of Android Studio.


## Codestyle
We use [ktlint](https://ktlint.github.io/) for checking Kotlin codestyle and formatting.

To run `ktlint` checks locally:
```
cd MapboxSearch
./gradlew :sdk:ktlint
```
To run `ktlint` formatter:
```
./gradlew :sdk:ktlintFormat
```

Also we have custom codestyle settings for XML. To apply those settings, please, select `AndroidStudio/code_style.xml` file in Android Studio: `Preferences -> Editor -> Code style -> XML -> "gear button" -> Import Scheme`.

For any resource name we use prefix `mapbox_search_sdk` or `MapboxSearchSdk` depending on resource type according to official Google recommendations: [Android library creation recommendations](https://developer.android.com/studio/projects/android-library)


## Code analysis
We use [detekt](https://arturbosch.github.io/detekt/) as static code analyzer.

To run `detekt` locally:
```
cd MapboxSearch
./gradlew :sdk:detekt
```

## Public API changes tracking
We use [binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator) and [Metalava](https://android.googlesource.com/platform/tools/metalava/) for tracking binary and source compatibility of the APIs we ship. 

Also we use [special Gradle task](https://github.com/mapbox/mapbox-search-android/blob/develop/MapboxSearch/gradle/track-public-xml-apis.gradle) to generate `public.xml` file, which helps us track changes in public API of Android `values/` resources. Please note that not every `values/` resource is tracked, so if you want to add extra resources under the radar, please, specify file contatining these resources manually in mentioned Gradle task.

To check whether your change affects public API, run `public-api.sh` script with `--check` argument:
```
cd MapboxSearch
../scripts/public-api.sh --check
```

If your change implies changes to the public API, run `public-api.sh` script with `--update` argument:
```
cd MapboxSearch
../scripts/public-api.sh --update
```

### Metalava update guide

If you want to update [Metalava](https://android.googlesource.com/platform/tools/metalava/), please, use `update_metalava.sh` script. This scripts places the latest `metalava.jar` into [`MapboxSearch/metalava`](https://github.com/mapbox/mapbox-search-android/blob/main/MapboxSearch/metalava) folder and prints out its deps:

```
./scripts/update_metalava.sh
Cloning… Done
Building… Done

Dependencies:

com.android.tools.external.org-jetbrains:uast:27.2.0-alpha11
com.android.tools.external.com-intellij:kotlin-compiler:27.2.0-alpha11
com.android.tools.external.com-intellij:intellij-core:27.2.0-alpha11
com.android.tools.lint:lint-api:27.2.0-alpha11
com.android.tools.lint:lint-checks:27.2.0-alpha11
com.android.tools.lint:lint-gradle:27.2.0-alpha11
com.android.tools.lint:lint:27.2.0-alpha11
com.android.tools:common:27.2.0-alpha11
com.android.tools:sdk-common:27.2.0-alpha11
com.android.tools:sdklib:27.2.0-alpha11
org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.30
org.jetbrains.kotlin:kotlin-reflect:1.4.30
org.ow2.asm:asm:8.0
org.ow2.asm:asm-tree:8.0
```

Copy and paste (update) the new deps into [`gradle/metalava-dependencies.gradle`](https://github.com/mapbox/mapbox-search-android/blob/main/MapboxSearch/gradle/metalava-dependencies.gradle)

### Notes about Japicmp usage

[Japicmp](https://github.com/siom79/japicmp) is a library, that helps you to determine the differences between the Java `.class` files, contained in two given `.jar` archives. Also, this library provides HTML reports with detailed information about each class/method that has been changed.

To use this tool, follow this steps:

1) Uncomment `include ':gradle:japicmp'` line in `MapboxSearch/settings.gradle`;
2) Open `MapboxSearch/gradle/japicmp/build.gradle` and make sure:
    - `baseline` targets to the latest `mapbox-search-android` (or `mapbox-search-android-ui`) artifact;
    - `latest` targets to `project(path: ':sdk', configuration: 'default')` (or `project(path: ':ui', configuration: 'default')`).
3) Run `./gradlew :gradle:japicmp:japicmp` task. If breaking changes have been found, you'll see Gradle failure and a link to HTML report in the output.

## Testing
We use **Kotlin Test DSL** for unit tests. Kotlin Test DSL provides human-readable DSL and is based on JUnit 5 dynamic tests. 
To run unit tests use the following commands:
```
./gradlew :sdk:testReleaseUnitTest
./gradlew :ui:testReleaseUnitTest
./gradlew :sdk-common:testReleaseUnitTest
```

Search SDK also contains a bunch of instrumentation and UI tests. To run them, please, use the following command:
```
./gradlew connectedAndroidTest mergeAndroidReports --continue
```

We use [pitest](https://pitest.org/) for mutation testing.
To run pitest, execute the following commands:
```
cd MapboxSearch
./gradlew :sdk:pitestDebug
```
You can find report at `MapboxSearch/sdk/build/reports`.


## Offline integration tests
To run offline integration tests, please, make sure your `MAPBOX_ACCESS_TOKEN` has `offline_search` feature flag enabled. As for now, offline functionality (including offline tests) is in a private beta and available to selected customers only.

## Complex checks run
To run all checks and unit tests locally, execute `local_checks.sh` script:
```
./scripts/local_checks.sh
```

Add `--runInstrumentationTests` flag to also run instrumentation and UI tests. In this case only one running emulator should be available via adb.


## Documentation

[Public Search SDK documentation.](https://docs.mapbox.com/android/search/overview/)

We use [dokka](https://github.com/Kotlin/dokka) for top level public classes overview. To generate docs for all modules use the following command:
```
./scripts/generate_docs.sh
```

You can find generated docs in `MapboxSearch/build/generated-docs/` directory.


## Third-party SDKs license

Project's additional 3rd-party licences, used by the SDK module and by the UI module, are stored [here](https://github.com/mapbox/mapbox-search-android/blob/develop/LICENSE.md).

To update license file, please, run `python scripts/license-generate.py` from the project's root directory. To validate current license file, please, run `python scripts/license-validate.py` from the project's root directory.


## Contributing

We welcome feedback and code contributions!

If you found a bug in Android SDK [open a github issue](https://github.com/mapbox/mapbox-search-android/issues). General feedback is welcoming in the [search-sdk repo](https://github.com/mapbox/mapbox-search-sdk/issues).

## Code of Conduct

### Our Standards

Examples of behavior that contributes to creating a positive environment include:

- Using welcoming and inclusive language.
- Being respectful of differing viewpoints and experiences.
- Gracefully accepting constructive criticism.
- Focusing on what is best for the community.
- Showing empathy towards other community members.

We recommend reading [this blog post from Github on writing great PRs.](https://github.blog/2015-01-21-how-to-write-the-perfect-pull-request/).

# Versioning

We use [SemVer](http://semver.org/) for versioning.
