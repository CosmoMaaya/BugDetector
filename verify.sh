#!/usr/bin/env bash

set -o pipefail
set -o nounset
#set -o errexit

TESTS=$(ls -d test* | sort)
NAME=$(whoami)
readonly RESULT_ROOT=$(mktemp -d)
LOG="${RESULT_ROOT}/testing-$NAME-pi-$(date +%F_%T).log"
TOTAL=0
PASSED=0

readonly PWD="$(pwd)"
readonly JAR="${PWD}/target/se465-1.0-SNAPSHOT.jar"

function verifyTest() {
  TESTNAME=$1
  T_SUPPORT=$2
  T_CONFIDENCE=$3

  OUT_FILE="${RESULT_ROOT}/${TESTNAME}_${T_SUPPORT}_${T_CONFIDENCE}.out"
  touch "${OUT_FILE}"
  GOLD_FILE="gold_${T_SUPPORT}_${T_CONFIDENCE}.txt"
  CALLGRAPH_FILE="${TESTNAME}/callgraph.txt"

  TOTAL=$(echo $TOTAL | awk '{ print $1+1 }')
  echo Verifying $TESTNAME $T_SUPPORT $T_CONFIDENCE...

  # give 2 minutes
  timeout -s 9 60 java -cp "${JAR}" \
      -Xms128m -Xmx128m \
      se465.Main \
      --support "${T_SUPPORT}"\
      --confidence "${T_CONFIDENCE}" \
      --callgraph "${CALLGRAPH_FILE}" \
      --output "${OUT_FILE}"

  RET=$?

  if [ "$RET" -eq 124 ]; then
    echo "time out."
    return
  fi
  if [ "$RET" -ne 0 ]; then
    echo "error."
    return
  fi

  TEST_RESULT="diff <(sort "$OUT_FILE" | sed 's/\r$//' 2>>$LOG) <(sort $TESTNAME/$GOLD_FILE | sed 's/\r$//' 2>>$LOG)"

  DIFF_COUNT=$(eval $TEST_RESULT | wc -l 2>>$LOG)
  TOTAL_COUNT=$(cat $TESTNAME/$GOLD_FILE 2>>$LOG | wc -l 2>>$LOG)

  if [ $? -eq 0 -a $DIFF_COUNT -eq 0 ]; then
    echo -e "<<<PASS>>>"
    PASSED=$(echo $PASSED | awk '{ print $1+1 }')
  else
    FALSE_COUNT=$(eval $TEST_RESULT | grep -e [\<] 2>>$LOG | wc -l 2>>$LOG)
    MISSING_COUNT=$(eval $TEST_RESULT | grep -e [\>] 2>>$LOG | wc -l 2>>$LOG)
    echo -e "<<<FAIL>>> with $MISSING_COUNT missing, $FALSE_COUNT extra and $TOTAL_COUNT total"
  fi
}

###########################
# Check for a makefile
mvn clean
mvn compile
mvn test
mvn spotbugs:check
mvn pmd:check
mvn package

if ! [[ -e "${JAR}" ]] ; then
  echo "Cannot locate the jar file at ${JAR}"
  exit 1
fi

# Show the log file
echo "Log: " $LOG

# Perform the testing
for TEST in ${TESTS}
do
  verifyTest $TEST 3 65
  verifyTest $TEST 10 80
done

echo PASSED/TOTAL:\ $PASSED/$TOTAL
