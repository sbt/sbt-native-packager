#!/bin/sh


export APP_NAME
export APP_HOME
export START_SCRIPT
export QUALIFIED_CLASS_NAME
export ARGS
export DEBUG="false"


dlog() {
  $DEBUG && echo "[debug] [$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@" >&2
}

elog() {
  echo "[error] [$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@" >&2
}

die() {
  elog "$1"
  exit "$2"
}

resolve_primary_environment_variables() {
  readonly APP_NAME="${0##*/}" || die "Error resolving APP_NAME" $?
  readonly APP_HOME="$(dirname "$(realpath "$0")")" || die "Error resolving APP_HOME" $?
  readonly START_SCRIPT="$APP_HOME/${{startScript}}" || die "Error resolving START_SCRIPT" $?
  readonly QUALIFIED_CLASS_NAME=${{qualifiedClassName}} || die "Error resolving QUALIFIED_CLASS_NAME" $?
}

main() {
  resolve_primary_environment_variables || die "Error resolving primary environment variables" $?
  exec "$START_SCRIPT" -main "$QUALIFIED_CLASS_NAME" "$@"
}
