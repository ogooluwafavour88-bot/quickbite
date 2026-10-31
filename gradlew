#!/usr/bin/env bash
set -e
execute() {
  if [ "x$1" = "x" ]; then
    command "$2" "${@:3}"
  else
    "$1" "${@:2}"
  fi
}

if [ -z "$GRADLE_OPTS" ]; then
  GRADLE_OPTS=""
fi

# This is a simplified wrapper that calls gradle if available
# On GitHub Actions, we can use the pre-installed gradle
execute gradle "$@"
