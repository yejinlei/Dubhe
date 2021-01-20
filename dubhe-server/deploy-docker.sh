#!/bin/bash
#环境，用以区分部署的命名空间，日志路径
ENV=$1

SOURCE_CODE_PATH=$(cd $(dirname ${BASH_SOURCE[0]}); pwd )
#harbor 地址
HARBOR_URL=harbor.dubhe.ai
#harbor 用户名
HARBOR_USERNAME=admin
#harbor 密码
HARBOR_PWD=123456
#nfs 地址
NFS_ADDRESS=127.0.0.1
#nfs 服务器 root权限用户名
NFS_HOST_USER=root
#nfs 服务器 root权限用户密码
NFS_HOST_PWD=123456
#nfs 共享目录
NFS_PATH=/nfs
#容器日志路径
CONTAINER_LOG_PATH=/logs
#宿主机日志路径
HOST_LOG_PATH=/logs/dubhe-${ENV}
#admin模块端口
ADMIN_PORT=8000
#serving-gateway模块端口
GATEWAY_PORT=8081

#挂载nfs
mount_nfs() {
	mkdir ${NFS_PATH}
	mount -t nfs ${NFS_ADDRESS}:${NFS_PATH} ${NFS_PATH}
}

update_dockerfile() {
	cd ${SOURCE_CODE_PATH} && sed -i "s#nfs-host-pwd#${NFS_HOST_PWD}#g;s#nfs-host-user#${NFS_HOST_USER}#g;s#nfs-host-ip#${NFS_ADDRESS}#g" Dockerfile
}
mvn_build() {
	cd ${SOURCE_CODE_PATH} && mvn clean compile package
	cd ${SOURCE_CODE_PATH}/dubhe-serving-gateway && mvn clean compile package
}
#删除镜像
delete_old_image() {
	docker rmi -f ${HARBOR_URL}/dubhe/dubhe-spring:${ENV}
}
#构建镜像
build_image() {
	cd ${SOURCE_CODE_PATH} && docker build -t ${HARBOR_URL}/dubhe/dubhe-spring:${ENV} .
}

#运行admin模块
run_admin() {
	docker run -d -v ${HOST_LOG_PATH}:${CONTAINER_LOG_PATH} -v  ${NFS_PATH}:${NFS_PATH} -v /var/run/docker.sock:/var/run/docker.sock -p ${ADMIN_PORT}:${ADMIN_PORT}  ${HARBOR_URL}/dubhe/dubhe-spring:${ENV}  java -jar /dubhe/dubhe-admin-1.0-exec.jar --spring.profiles.active=${ENV} 2>&1 > /dev/null
}
#运行task模块
run_task() {
	docker run -d -v ${HOST_LOG_PATH}:${CONTAINER_LOG_PATH} -v ${NFS_PATH}:${NFS_PATH} -v /var/run/docker.sock:/var/run/docker.sock ${HARBOR_URL}/dubhe/dubhe-spring:${ENV} java -jar /dubhe/dubhe-task-1.0.jar --spring.profiles.active=${ENV} 2>&1 > /dev/null
}
#运行serving-gateway模块
run_gateway() {
	docker run -d -v ${HOST_LOG_PATH}:${CONTAINER_LOG_PATH} -v ${NFS_PATH}:${NFS_PATH} -v /var/run/docker.sock:/var/run/docker.sock -p ${GATEWAY_PORT}:${GATEWAY_PORT} ${HARBOR_URL}/dubhe/dubhe-spring:${ENV}  java -jar /dubhe/dubhe-serving-gateway.jar --spring.profiles.active=${ENV} 2>&1 > /dev/null
}

mount_nfs
update_dockerfile
mvn_build
delete_old_image
build_image
run_admin
run_task
run_gateway
