#!/bin/bash

PROG_NAME=$0
ACTION=$1
ENV=$2
APP_HOME=$3

APP_NAME=dubhe-${ENV}

APP_HOME=$APP_HOME/${APP_NAME} # 从package.tgz中解压出来的jar包放到这个目录下
JAR_NAME=${APP_HOME}/dubhe-admin/target/dubhe-admin-1.0-exec.jar # jar包的名字
JAVA_OUT=/dev/null

# 创建出相关目录
mkdir -p ${APP_HOME}
mkdir -p ${APP_HOME}/logs

usage() {
    echo "Usage: $PROG_NAME {start|stop|restart} {dev|test|prod}"
    exit 2
}

start_application() {
    echo "starting java process"
    echo "nohup java -jar ${JAR_NAME} > ${JAVA_OUT} --spring.profiles.active=${ENV} 2>&1 &"
    nohup java -jar ${JAR_NAME} > ${JAVA_OUT} --spring.profiles.active=${ENV} 2>&1 &
    echo "started java process"
}

stop_application() {
   checkjavapid=`ps -ef | grep java | grep ${APP_NAME} | grep -v grep |grep -v 'deploy.sh'| awk '{print$2}'`

   if [ -z $checkjavapid ];then
      echo -e "\rno java process "$checkjavapid
      return
   fi

   echo "stop java process"
   times=60
   for e in $(seq 60)
   do
        sleep 1
        COSTTIME=$(($times - $e ))
        checkjavapid=`ps -ef | grep java | grep ${APP_NAME} | grep -v grep |grep -v 'deploy.sh'| awk '{print$2}'`
        if [ "$checkjavapid" != "" ];then
            echo "kill "$checkjavapid
            kill -9 $checkjavapid
            echo -e  "\r        -- stopping java lasts `expr $COSTTIME` seconds."
        else
            echo -e "\rjava process has exited"
            break;
        fi
   done
   echo ""
}

case "$ACTION" in
    start)
        start_application
    ;;
    stop)
        stop_application
    ;;
    restart)
        stop_application
        start_application
    ;;
    *)
        usage
    ;;
esac
