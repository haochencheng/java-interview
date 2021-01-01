#! /bin/bash

BIN_DIR=$(cd `dirname $0`; pwd)
source ${BIN_DIR}/base.sh


PID=$(getPid)
HEAP_LOG_NAME=${PID}-heap-${DATE_TIME}.log
HISTO_LOG_NAME=${PID}-histo-${DATE_TIME}.log
JSTACK_LOG_NAME=${PID}-jstack-${DATE_TIME}.log
HEAP_LOG=${LOG_DIR}/heap/${HEAP_LOG_NAME}
HISTO_LOG=${LOG_DIR}/histo/${HISTO_LOG_NAME}
JSTACK_LOG=${LOG_DIR}/jstack/${JSTACK_LOG_NAME}

JHSDB="jhsdb"
JMAP="jmap"
JSTACK="jstack"

if [[ ${JAVA_VERSION} >  ${JAVA_8} ]] ; then
    # jdk 1.8以上参数
    JMAP="${JHSDB} ${JMAP}"
    JSTACK="${JHSDB} ${JSTACK}"
fi

PID_CMD="--pid ${PID}"
HEAP_CMD="${JMAP} --heap ${PID_CMD} >> ${HEAP_LOG}"
HISTO_CMD="${JMAP} --histo:live  ${PID_CMD} | head -n 30 >> ${HISTO_LOG}"

JSTACK_CMD="${JSTACK} ${PID_CMD} > ${JSTACK_LOG} 2>&1 &"

if [[ ! ${PID} ]]; then
    echo "pid not exist"
    exit 1
fi

case $1
    in
    heap)
        eval ${HEAP_CMD}
        ;;
    histo)
        eval ${HISTO_CMD}
        ;;
    thread)
        echo ${JSTACK_CMD}
        eval ${JSTACK_CMD}
        pid=$!
        echo ${pid}
        wait ${pid}
        THREAD_LOG=$(cat ${JSTACK_LOG})
        THREAD_COUNT=$(echo ${THREAD_LOG} | grep -o 'tid' |wc -l)
        TOMCAT_THREAD_COUNT=$(echo ${THREAD_LOG} | grep -o 'http-nio-8080' |wc -l)
        echo "总线程数量:"${THREAD_COUNT}
        echo "tomcat线程数量:"${TOMCAT_THREAD_COUNT}
        ;;
    *)
        echo "Usage: $0 {heap|histo|thread}"
        exit 1
        ;;
esac
exit ${RET_VAL}