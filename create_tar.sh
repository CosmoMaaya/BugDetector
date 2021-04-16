#!/usr/bin/env bash

set -o pipefail
set -o errexit
set -o nounset
set -o xtrace

if [[ "$#" != 1 ]] ; then
  echo "Usage: $0 FirstName-LastName-StudentNo"
  exit 1
fi

readonly NAME=$1

rm -f "${NAME}.tar.gz"
find * -mindepth 1 -maxdepth 1 ! -name "*.tar.gz" -exec tar zcf "${NAME}.tar.gz" \{\} \+
