#! /bin/bash

BIN_DIR=$(cd `dirname $0`; pwd)
source ${BIN_DIR}/base.sh

HEAP_LOG=${LOG_DIR}/heap/${HEAP_LOG_NAME}
PID=$(getPid)
HEAP_LOG_NAME=${PID}-heap-${DATE_TIME}.log
HEAP_CMD="jhsdb jmap  --heap --pid  ${PID} >> ${HEAP_LOG}"

if [[ ${JAVA_VERSION} >  ${JAVA_8} ]] ; then
    # jdk 1.8以上参数
    HEAP_CMD="jhsdb jmap  --heap --pid  ${PID} >> ${HEAP_LOG}"
else
    # jdk 1.8 参数
    HEAP_CMD="jmap -heap ${PID} >> ${HEAP_LOG}"
fi

case $1
    in
    heap)
        eval ${HEAP_CMD}
        ;;
    stop)
        stop
        ;;
    *)
        echo "Usage: $0 {heap|stop|status|restart}"
        exit 1
        ;;
esac
exit ${RET_VAL}