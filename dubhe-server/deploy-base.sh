#!/bin/bash
#基础部署脚本
#环境，用以区分部署的命名空间，日志路径
ENV=$1
#本文件绝对路径
SOURCE_CODE_PATH=$(cd $(dirname ${BASH_SOURCE[0]}); pwd )

#harbor 地址
HARBOR_URL=harbor.dubhe.ai
#harbor 用户名
HARBOR_USERNAME=admin
#harbor 密码
HARBOR_PWD=Harbor12345
#文件存储服务 共享目录
FS_PATH=/nfs
#容器日志路径
CONTAINER_LOG_PATH=/logs
#宿主机日志路径
HOST_LOG_PATH=/logs/dubhe-${ENV}

#删除镜像
delete_old_image() {
	docker rmi -f ${HARBOR_URL}/dubhe/dubhe-spring-cloud-k8s:${ENV}
}
#构建镜像
build_image() {
	cd ${SOURCE_CODE_PATH} && docker build -t ${HARBOR_URL}/dubhe/dubhe-spring-cloud-k8s:${ENV} .
}
#推送镜像到harbor
push_image() {
	docker login -u ${HARBOR_USERNAME} -p ${HARBOR_PWD} ${HARBOR_URL}
	docker push ${HARBOR_URL}/dubhe/dubhe-spring-cloud-k8s:${ENV}
}
#编译打包源码
mvn_build() {
  # -T 1C 每核心打包一个工程
	# -Dmaven.test.skip=true 跳过测试代码的编译
	# -Dmaven.compile.fork=true 多线程编译
	cd ${SOURCE_CODE_PATH} && mvn clean compile package -T 1C -Dmaven.test.skip=true -Dmaven.compile.fork=true
}

update_k8s_yaml() {
  sed -i "s#harbor.test.com#${HARBOR_URL}#g;s#fsPath#${FS_PATH}#g;s#env-value#${ENV}#g;s#containerLogPath#${CONTAINER_LOG_PATH}#g;s#hostLogPath#${HOST_LOG_PATH}#g;s#gatewayNodePort#${GATEWAY_NODE_PORT}#g" ${SOURCE_CODE_PATH}/deploy/*/*
}