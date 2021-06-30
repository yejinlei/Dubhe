#!/bin/bash
#引用基础脚本
source $(cd $(dirname ${BASH_SOURCE[0]}); pwd )/../../deploy-base.sh

#网关暴露端口
GATEWAY_NODE_PORT=$2

#删除服务
delete_k8s_app() {
	kubectl delete ns dubhe-${ENV}
}
#配置gateway端口
update_gateway_node_port() {
  sed -i "s#gatewayNodePort#${GATEWAY_NODE_PORT}#g" ${SOURCE_CODE_PATH}/deploy/*/*
}
#部署服务
deploy_k8s_app() {
	cd ${SOURCE_CODE_PATH}/deploy/cloud && kubectl create ns dubhe-${ENV} && kubectl apply -f server-dubhe.yaml -n dubhe-${ENV}
}

delete_k8s_app
delete_old_image
update_k8s_yaml
update_gateway_node_port
mvn_build
build_image
push_image
deploy_k8s_app
