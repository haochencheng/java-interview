#! /bin/bash

BIN_DIR=$(cd `dirname $0`; pwd)
source ${BIN_DIR}/base.sh


PID=$(getPid)
HEAP_LOG_NAME=${PID}-heap-${DATE_TIME}.log
HISTO_LOG_NAME=${PID}-histo-${DATE_TIME}.log
HEAP_LOG=${LOG_DIR}/heap/${HEAP_LOG_NAME}
HISTO_LOG=${LOG_DIR}/histo/${HISTO_LOG_NAME}

JHSDB="jhsdb"
JMAP="jmap"

if [[ ${JAVA_VERSION} >  ${JAVA_8} ]] ; then
    # jdk 1.8以上参数
    JMAP="${JHSDB} jmap"
fi

PID_CMD="--pid ${PID}"
HEAP_CMD=""
HISTO_CMD="jhsdb jmap  --histo:live --pid 40730 | head -n 30"

HEAP_CMD=" --heap ${PID_CMD} >> ${HEAP_LOG}"
HISTO_CMD=" --histo ${PID_CMD} >> ${HISTO_LOG}"

if [[ ! ${PID} ]]; then
    echo "pid not exist"
    exit 1
fi

case $1
    in
    heap)
        eval ${JMAP} ${HEAP_CMD}
        ;;
    histo)
        eval ${JMAP} ${HISTO_CMD}
        ;;
    *)
        echo "Usage: $0 {heap|stop|status|restart}"
        exit 1
        ;;
esac
exit ${RET_VAL}