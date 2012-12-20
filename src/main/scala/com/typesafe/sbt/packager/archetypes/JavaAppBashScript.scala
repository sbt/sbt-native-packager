package com.typesafe.sbt.packager.archetypes

object JavaAppBashScript {
  
  def generateScript(mainClass: String,
                     libDir: String = "$(dirname $(realpath $0))/../lib",
                     configFile: Option[String] = None): String = {
    val sb = new StringBuffer
    sb append template_header
    sb append mainClassDefine(mainClass)
    sb append defaultUsage
    configFile foreach (f => sb append configFileDefine(f))
    sb append defineLibLocation(libDir)
    sb append template_footer
    sb.toString
  }
  
  def defineLibLocation(libDir: String) =
    "declare -r lib_dir=%s" format (libDir)

  def configFileDefine(configFile: String) =
    "declare -r script_conf_file=\"%s\"" format (configFile)
  
  def mainClassDefine(mainClass: String) = 
    "declare -r app_mainclass=\"%s\"\n" format (mainClass)
  
  
  val defaultUsage = """usage() {
 cat <<EOM
Usage: $script_name [options]

  -h | -help         print this message
  -v | -verbose      this runner is chattier
  -d | -debug        set sbt log level to debug
  -mem    <integer>  set memory options (default: $sbt_mem, which is $(get_mem_opts $sbt_mem))
  -jvm-debug <port>  Turn on JVM debugging, open at the given port.

  # java version (default: java from PATH, currently $(java -version 2>&1 | grep version))
  -java-home <path>         alternate JAVA_HOME

  # jvm options and output control
  JAVA_OPTS          environment variable, if unset uses "$java_opts"
  -Dkey=val          pass -Dkey=val directly to the java runtime
  -J-X               pass option -X directly to the java runtime 
                     (-J is stripped)

In the case of duplicated or conflicting options, the order above
shows precedence: JAVA_OPTS lowest, command line options highest.
EOM
}
"""
  
  
  
  val template_header = """#!/bin/bash

###  ------------------------------- ###
###  Helper methods for BASH scripts ###
###  ------------------------------- ###

realpath () {
(
  TARGET_FILE=$1

  cd $(dirname $TARGET_FILE)
  TARGET_FILE=$(basename $TARGET_FILE)

  COUNT=0
  while [ -L "$TARGET_FILE" -a $COUNT -lt 100 ]
  do
      TARGET_FILE=$(readlink $TARGET_FILE)
      cd $(dirname $TARGET_FILE)
      TARGET_FILE=$(basename $TARGET_FILE)
      COUNT=$(($COUNT + 1))
  done

  echo $(pwd -P)/$TARGET_FILE
)
}
echoerr () {
  echo 1>&2 "$@"
}
vlog () {
  [[ $verbose || $debug ]] && echoerr "$@"
}
dlog () {
  [[ $debug ]] && echoerr "$@"
}
execRunner () {
  # print the arguments one to a line, quoting any containing spaces
  [[ $verbose || $debug ]] && echo "# Executing command line:" && {
    for arg; do
      if printf "%s\n" "$arg" | grep -q ' '; then
        printf "\"%s\"\n" "$arg"
      else
        printf "%s\n" "$arg"
      fi
    done
    echo ""
  }

  exec "$@"
}
addJava () {
  dlog "[addJava] arg = '$1'"
  java_args=( "${java_args[@]}" "$1" )
}
addApp () {
  dlog "[addApp] arg = '$1'"
  sbt_commands=( "${app_commands[@]}" "$1" )
}
addResidual () {
  dlog "[residual] arg = '$1'"
  residual_args=( "${residual_args[@]}" "$1" )
}
addDebugger () {
  addJava "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$1"
}
# a ham-fisted attempt to move some memory settings in concert
# so they need not be dicked around with individually.
get_mem_opts () {
  local mem=${1:-1536}
  local perm=$(( $mem / 4 ))
  (( $perm > 256 )) || perm=256
  (( $perm < 1024 )) || perm=1024
  local codecache=$(( $perm / 2 ))

  echo "-Xms${mem}m -Xmx${mem}m -XX:MaxPermSize=${perm}m -XX:ReservedCodeCacheSize=${codecache}m"
}
require_arg () {
  local type="$1"
  local opt="$2"
  local arg="$3"
  if [[ -z "$arg" ]] || [[ "${arg:0:1}" == "-" ]]; then
    die "$opt requires <$type> argument"
  fi
}
require_arg () {
  local type="$1"
  local opt="$2"
  local arg="$3"
  if [[ -z "$arg" ]] || [[ "${arg:0:1}" == "-" ]]; then
    die "$opt requires <$type> argument"
  fi
}
is_function_defined() {
  declare -f "$1" > /dev/null
}

# Processes incoming arguments and places them in appropriate global variables.  called by the run method.
process_args () {
  while [[ $# -gt 0 ]]; do
    case "$1" in
       -h|-help) usage; exit 1 ;;
    -v|-verbose) verbose=1 && shift ;;
      -d|-debug) debug=1 && shift ;;

           -mem) require_arg integer "$1" "$2" && app_mem="$2" && shift 2 ;;
     -jvm-debug) require_arg port "$1" "$2" && addDebugger $2 && shift 2 ;;

     -java-home) require_arg path "$1" "$2" && java_cmd="$2/bin/java" && shift 2 ;;

            -D*) addJava "$1" && shift ;;
            -J*) addJava "${1:2}" && shift ;;
              *) addResidual "$1" && shift ;;
    esac
  done
  
  is_function_defined process_my_args && {
    myargs=("${residual_args[@]}")
    residual_args=()
    process_my_args "${myargs[@]}"
  }
}

# Actually runs the script.
run() {
  # TODO - check for sane environment

  # process the combined args, then reset "$@" to the residuals
  process_args "$@"
  set -- "${residual_args[@]}"
  argumentCount=$#

  # run sbt
  execRunner "$java_cmd" \
    $(get_mem_opts $app_mem) \
    ${java_opts} \
    ${java_args[@]} \
    -cp "$app_classpath" \
    $app_mainclass \
    "${app_commands[@]}" \
    "${residual_args[@]}"
}

# Loads a configuration file full of default command line options for this script.
loadConfigFile() {
  cat "$1" | sed '/^\#/d'
}

###  ------------------------------- ###
###  Start of customized settings    ###
###  ------------------------------- ###
"""
    
    
  val template_footer = """

###  ------------------------------- ###
###  Main script                     ###
###  ------------------------------- ###

declare -a residual_args
declare -a java_args
declare -a app_commands
declare java_cmd=java
declare -r app_classpath="$lib_dir/*"


# if configuration files exist, prepend their contents to $@ so it can be processed by this runner
[[ -f "$script_conf_file" ]] && set -- $(loadConfigFile "$script_conf_file") "$@"

run "$@"
"""
}