#! /bin/bash
# 工作目录信息
BIN_DIR=$(cd `dirname $0`; pwd)
WORK_DIR=$(cd `dirname ${BIN_DIR}`; pwd)
echo "工作目录:"${WORK_DIR}
SERVER_NAME=java-interview-springboot
LOG_DIR=${WORK_DIR}/logs
PID_FILE=${WORK_DIR}/${SERVER_NAME}.pid
# java版本信息
JAVA="$JAVA_HOME/bin/java"
JAVA_VERSION=$(${JAVA} -version 2>&1 | sed '1!d' | sed -e 's/"//g' | awk '{print $3}')
JAVA_8=1.9

DATE_TIME=$(date +%Y%m%d-%X)

function getPid() {
    echo `cat "${PID_FILE}"`
}