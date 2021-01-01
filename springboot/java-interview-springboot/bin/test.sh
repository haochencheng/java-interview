#! /bin/bash

BIN_DIR=$(cd `dirname $0`; pwd)
source ${BIN_DIR}/base.sh

MVN=$(which mvn)
if [[ $? = 1 ]]; then
    echo ${MVN}
    exit 1
fi
OUT_OF_MEMORY="-DskipTests=false -Dtest=pers.interview.springboot.controller.BigObjectControllerTest#addBigObject"
CPU_TOP="-DskipTests=false -Dtest=pers.interview.springboot.controller.CpuTopControllerTest#top"
ORDER="-DskipTests=false -Dtest=pers.interview.springboot.controller.OrderControllerTest#orderWithVersion"
ORDER_MAX="-DskipTests=false -Dtest=pers.interview.springboot.controller.OrderControllerTest#orderWithVersionMax"

LOG_OPS=" -Dlogback.home=${LOG_DIR}/test "

case $1 in
    outOfMemory)
        eval "${MVN} ${OUT_OF_MEMORY} ${LOG_OPS} test"
        ;;
    cpuTop)
        eval "${MVN} ${CPU_TOP} ${LOG_OPS} test"
        ;;
    order)
        eval "${MVN} ${ORDER} ${LOG_OPS} test"
        ;;
    orderMax)
        eval "${MVN} ${ORDER_MAX} ${LOG_OPS} test"
        ;;
      *)
        echo "Usage: $0 |outOfMemory|cpuTop|order}"
        exit 1
        ;;
esac

