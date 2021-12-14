#!/usr/bin/env bash

set -eo pipefail

# Always exit on Ctrl+C
trap "exit;" SIGINT

# Color codes for "echo"
RED=$'\e[0;31m'; GREEN=$'\e[0;32m'; NC=$'\e[0m';

echo -e "\nRunning PIT configuration check..."
(./gradlew :sdk:quickPitestDebugCheck 2>&1 || true) | grep -i 'Timeout has been exceeded' \
  && echo -e "${GREEN}Congrats, PIT configuration setup is working!${NC}\n" \
  || { echo -e "${RED}Error: PIT configuration is broken!${NC}\n" 1>&2; exit 1; }
