#!/usr/bin/env bash

# Treat unset variables as error when substituting.
set -u

# The command line parameters that will be passed to the Java program.
CLASS=""
DYNAMIC=""
LINE_NO=""
METHOD=""
SRC_FILE=""
TGT_FILE=""
VAR_NAME=""
XML_FILE=""

# Required options.
declare -a REQUIRED
REQUIRED=("CLASS" "METHOD" "VAR_NAME" "LINE_NO")
readonly REQUIRED

# Constants.
readonly MAVEN="$(which mvn)"
readonly JAR="target/slicer.jar"
readonly JAVA="$(command -v java)"
readonly MAIN="de.uni_passau.fim.se2.sa.slicing.SlicerMain"

# Color escape sequences.
readonly RED="\033[0;31m"
readonly RESET="\033[0m"

# Prints a help message.
function help() {
  cat <<__EOF__
usage: ${0} -c <arg> -m "<arg>" -v <arg> -l <arg> [-d <arg>] [-s <arg>] [-t <arg>] [-x]
 -c,--class <arg>          Path to the class file
 -d,--dynamic <arg>        Create a dynamic slice by executing the given test
 -l,--linenumber <arg>     Line number where to start the slice
 -m,--method "<arg>"       Method name and descriptor of the method (must be given in quotes)
 -s,--sourcefile <arg>     Path to the class file's source code
 -t,--targetfile <arg>     Path to a target file where to write the slice code to
 -v,--variablename <arg>   Name of variable
 -x,--xmlfile              Extracts the result as an XML file for grading
 -h,--help                 Prints this help message
__EOF__
}

# Prints an error message to stderr.
function error() {
  local -r error_msg="${1}"
  echo -e "${RED}Error: ${error_msg}${RESET}" >&2
}

# Parse the command line arguments.
function parse_args() {
  while (("$#")); do
    case "${1}" in
    -c | --class)
      CLASS="${2}"
      shift 2
      ;;
    -d | --dynamic)
      DYNAMIC="${2}"
      shift 2
      ;;
    -l | --linenumber)
      LINE_NO="${2}"
      shift 2
      ;;
    -m | --method)
      METHOD="${2}"
      shift 2
      ;;
    -s | --sourcefile)
      SRC_FILE="${2}"
      shift 2
      ;;
    -t | --targetfile)
      TGT_FILE="${2}"
      shift 2
      ;;
    -v | --variablename)
      VAR_NAME="${2}"
      shift 2
      ;;
    -x,--xmlfile)
      XML_FILE="TRUE"
      shift 1
      ;;
    -h | --help)
      help
      exit 0
      ;;
    -*)
      error "Unknown option ${1}"
      help
      exit 1
      ;;
    esac
  done
}

# Validate command line arguments.
function validate_args() {
  for opt in "${REQUIRED[@]}"; do
    if [[ -z "${!opt}" ]]; then
      error "Missing required option ${opt}"
      help
      exit 1
    fi
  done
}

# Try to build the Java program and create the JAR artefact.
function build_jar() {
  if [[ ! -x "${MAVEN}" ]]; then
    error "Cannot execute Maven ${MAVEN}"
    exit 1
  fi

  eval "${MAVEN} -DskipUnitTests package"

  if [[ ! -f "${JAR}" ]]; then
    error "Cannot locate ${JAR}"
    exit 1
  fi
}

# Escapes the argument string in a way such that it can be reused as shell input.
# In particular, when specifying the slicing criterion, the characters '(', ')' and ';' of the
# signature of the method must be escaped. For example:
#   "evaluate:(Ljava/lang/String;)I"   -->   "evaluate:\(Ljava/lang/String\;\)I"
function escape() {
  local -r input="${1}"
  printf '%q' "${input}"
}

# Runs the slicer with the arguments given to the shell script.
function run_slicer() {
  # local -r java_opt="-cp ${JAR} -jar ${JAR} ${MAIN}"
  local -r java_opt="-cp ${JAR} -jar ${JAR}"
  local jvm_opt=""
  local -r escaped_method=$(escape "${METHOD}")
  local slicer_opt="-c ${CLASS} -m ${escaped_method} -v ${VAR_NAME} -l ${LINE_NO}"

  if [[ -n "${DYNAMIC}" ]]; then
    slicer_opt="${slicer_opt} -d ${DYNAMIC}"
    jvm_opt="-javaagent:${JAR}=${CLASS}"
  fi

  if [[ -n "${SRC_FILE}" ]]; then
    slicer_opt="${slicer_opt} -s ${SRC_FILE}"
  fi

  if [[ -n "${TGT_FILE}" ]]; then
    slicer_opt="${slicer_opt} -t ${TGT_FILE}"
  fi

  if [[ -n "${XML_FILE}" ]]; then
    slicer_opt="${slicer_opt} -x"
  fi

  local -r slicer="${JAVA} ${jvm_opt} ${java_opt} ${slicer_opt}"

  echo
  echo "${slicer}"
  echo

  eval "${slicer}"
}

function main() {
  parse_args "$@"
  validate_args
  build_jar
  run_slicer
}

main "$@"