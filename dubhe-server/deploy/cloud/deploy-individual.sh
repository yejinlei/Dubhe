#!/bin/bash
#引用基础脚本
source $(cd $(dirname ${BASH_SOURCE[0]}); pwd )/../../deploy-base.sh

#网关暴露端口
GATEWAY_NODE_PORT=$2
#模块列表
MODULES=${@:3}

#删除服务
delete_k8s_app() {
  echo "start delete ${MODULES}"
  for i in ${MODULES}
  do
    echo "kubectl delete -f "server-${i}.yaml" -n dubhe-${ENV}"
    cd ${SOURCE_CODE_PATH}/deploy/cloud && kubectl delete -f "server-${i}.yaml" -n dubhe-${ENV}
  done
}
#配置gateway端口
update_gateway_node_port() {
  sed -i "s#gatewayNodePort#${GATEWAY_NODE_PORT}#g" ${SOURCE_CODE_PATH}/deploy/*/*
}
#部署服务
deploy_k8s_app() {
  echo "start deploy ${MODULES}"
  kubectl create ns dubhe-${ENV}
  for i in ${MODULES}
  do
    echo "kubectl apply -f "server-${i}.yaml" -n dubhe-${ENV}"
    cd ${SOURCE_CODE_PATH}/deploy/cloud && kubectl apply -f "server-${i}.yaml" -n dubhe-${ENV}
  done
}

delete_k8s_app
delete_old_image
update_k8s_yaml
update_gateway_node_port
mvn_build
build_image
push_image
deploy_k8s_app
