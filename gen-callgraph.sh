#!/usr/bin/env bash

set -o pipefail
set -o nounset
set -o errexit
set -o xtrace

if [[ "$#" != 2 ]] ; then
  echo "Usage: <source file> <graph file>"
  exit 1
fi

readonly SRC=$1
readonly GRAPH=$2

readonly PATH="/opt-src/llvm-3.0rc4/bin/"
readonly CLANG="${PATH}/clang"
readonly OPT="${PATH}/opt"

[[ -f "${CLANG}" ]] || ( echo "${CLANG} does not exist" ; exit 1 )
[[ -f "${OPT}" ]] || ( echo "${OPT} does not exist" ; exit 1)

"${CLANG}" -c -g -emit-llvm -O0 "${SRC}" -o "${SRC}.bc"
"${OPT}" --print-callgraph -o /dev/null "${SRC}.bc" 2>"${GRAPH}"
