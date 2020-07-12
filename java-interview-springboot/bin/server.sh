#! /bin/bash
BIN_DIR=$(cd `dirname $0`; pwd)
source ${BIN_DIR}/base.sh

JAR_DIR=${WORK_DIR}/target
JAR_NAME=$(find ${JAR_DIR} -name *.jar)
SERVER_LOG=${LOG_DIR}/server/start.log
PID_FILE=${WORK_DIR}/${SERVER_NAME}.pid
# gc log
GC_LOG_NAME=gc.log
GC_LOG=${LOG_DIR}/gclog/${GC_LOG_NAME}

LOW_MEMORY="-Xmx30m -Xms30m"
MID_MEMORY="-Xmx512m -Xms512m"
HEIGHT_MEMORY="-Xmx1g -Xms1g"

MEMORY=""
case $2
    in
    low)
       MEMORY=${LOW_MEMORY}
        ;;
    mid)
       MEMORY=${MID_MEMORY}
        ;;
    big)
       MEMORY=${HEIGHT_MEMORY}
        ;;
esac

if [[ ${JAVA_VERSION} >  ${JAVA_8} ]] ; then
    # jdk 1.8以上参数
      JAVA_OPS="-server -verbose:gc  ${MEMORY} -Xss256k \
        -XX:+UseConcMarkSweepGC -XX:MaxTenuringThreshold=2 \
           -Xloggc:${GC_LOG}  -Xlog:gc*   \
        "
else
    # jdk 1.8 参数
    JAVA_OPS="-server -verbose:gc  ${MEMORY} -Xss256k \
        -XX:ParallelGCThreads=5 \
        -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:MaxTenuringThreshold=2 \
        -XX:CMSFullGCsBeforeCompaction=5    \
        -Xloggc:${GC_LOG} -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps   \
        -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=512m "
fi
echo ${JAVA_OPS}

SPRINGBOOT_OPS="-server.tomcat.accept-count=10 \
                -server.tomcat.max-connections=10 \
                -server.tomcat.threads.max=10"
LOG_OPS=" -Dlogback.home=${LOG_DIR}/server "

JAVA_CMD="${JAVA} ${JAVA_OPS} ${LOG_OPS} -jar ${JAR_NAME} ${SPRINGBOOT_OPS} "

RET_VAL=0

#启动服务方法
function start() {
    STATUS=$(status)
    if [[ $? == 1 ]]; then
        echo ${STATUS}
        exit 0
    fi
    START_CMD="nohup $JAVA_CMD > /dev/null 2>&1 &"
    echo "执行启动命令:[$START_CMD]"
    echo $(date) "服务启动开始\n" >> ${SERVER_LOG}
    eval "${START_CMD}"
    RET_VAL=$?
    if [[ ${RET_VAL} = 0 ]]; then
        PID=$!
        echo ${PID} > "${PID_FILE}"
#        wait ${PID}
        echo "执行启动命令成功！pid:"${PID}
        echo $(date) "服务启动成功\n" >> ${SERVER_LOG}
    else
        echo "start failure"
        echo $(date) "服务启动失败\n" >> ${SERVER_LOG}
    fi
}

function stop(){
     STATUS=$(status)
     if [[ $? == 0 ]]; then
        echo ${STATUS}
        return
     fi
     echo  $(date) "服务停止开始\n" >> "${SERVER_LOG}"
     echo "服务停止开始"
     sleep 1
     echo "进程睡眠1秒\n" >> "${SERVER_LOG}"
     CURRENT_GC_LOG=$(find ${WORK_DIR} -name ${GC_LOG_NAME})
     mv ${CURRENT_GC_LOG} ${GC_LOG}_${DATE_TIME}
     kill -9 `cat "${PID_FILE}"`
     echo "" > ${PID_FILE}
     echo $(date) "服务停止成功\n" >> "${SERVER_LOG}"
     echo "服务停止成功"
}

function status() {
    if [[ ! -f ${PID_FILE} ]]; then
        echo "${SERVER_NAME} is not running "
        return 0
    fi
    PID=`cat "${PID_FILE}"`
    if [[ ! ${PID} || ! $(ps aux | awk '{print $2}'| grep -w ${PID}) ]]; then
         echo "${SERVER_NAME} is not running "
        return 0
    fi
    echo "${SERVER_NAME} is running "
    return 1
}

case $1
    in
    start)
        start
        ;;
    stop)
        stop
        ;;
    status)
        status
        ;;
    restart)
        stop
        start
        ;;
    *)
        echo "Usage: $0 {start|stop|status|restart}"
        exit 1
        ;;
esac
exit ${RET_VAL}