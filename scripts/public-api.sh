#!/usr/bin/env bash

set -o pipefail

# Color codes for "echo"
RED=$'\e[0;31m'; GREEN=$'\e[0;32m'; NC=$'\e[0m';


function show_usage {
  echo "Usage : public-api.sh [--check | --update]"
}

if [ "$1" != "--check" ] && [ "$1" != "--update" ]; then
  show_usage >&2
  exit 1
fi

if [ "$1" = "--check" ]; then
  set -e

  ./gradlew apiCheck

  ./gradlew :sdk:checkApi
  ./gradlew :ui:checkApi

  mkdir -p ./ui/build/temp/
  ./gradlew :ui:generatePublicXml -Pdestination=./ui/build/temp/public.xml
  diff ./ui/build/temp/public.xml ./ui/src/main/res/values/public.xml \
    && echo -e "${GREEN}Congrats, public.xml is up-to-date!${NC}\n" \
    || { echo -e "${RED}Error: public.xml is out of date!${NC}\n" 1>&2; exit 1; }
fi

if [ "$1" = "--update" ]; then
  ./gradlew apiDump

  ./gradlew :sdk:updateApi
  ./gradlew :ui:updateApi
  ./gradlew :autofill:updateApi

  ./gradlew :ui:generatePublicXml
fi
