 /**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */

package org.onebrain.operator.action;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.apiextensions.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.onebrain.operator.controller.DistributeTrainController;
import org.onebrain.operator.crd.DistributeTrain;
import org.onebrain.operator.crd.DistributeTrainList;
import org.onebrain.operator.crd.DoneableDistributeTrain;
import org.onebrain.operator.utils.DistributeTrainClientHolder;
import org.onebrain.operator.utils.SpringContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.onebrain.operator.constants.CrdConstants.*;

/**
 * @description operator 主控制器
 * @date 2020-09-23
 */
@Component
@Slf4j
public class DistributeTrainOperatorManager {

    public static final String NAMESPACE_DEFAULT = "default";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_INTEGER = "integer";
    public static final String TYPE_OBJECT = "object";
    public static final String TYPE_ARRAY = "array";
    public static final String FORMAT_INT_32 = "int32";
    @Autowired
    private KubernetesClient client;

    private CustomResourceDefinition crd;

    private String namespace;

    /**
     * 检查crd是否存在，若不存在则创建
     * @throws JsonProcessingException
     */
    public void createCrdIfNotExists() throws JsonProcessingException {
        String namespace = client.getNamespace();
        if (namespace == null) {
            log.info("No namespace found via config, assuming default.");
            namespace = NAMESPACE_DEFAULT;
        }
        this.namespace = namespace;
        log.info("Using namespace : {}", namespace);
        //检查crd是否已存在
        CustomResourceDefinition crd = client.customResourceDefinitions().withName(CRD_NAME).get();
        if(crd == null){
            Map<String, JSONSchemaProps> crdPropsMap = buildCrdProperties();
            log.info("crd props map is : 【{}】",crdPropsMap);
            //如不存在，则创建
            CustomResourceDefinition distributeTrainCustomResourceDefinition = new CustomResourceDefinitionBuilder()
                    .withApiVersion(CRD_API_VERSION)
                    .withNewMetadata()
                        .withName(CRD_NAME)
                    .endMetadata()
                    .withNewSpec()
                        .withGroup(CRD_GROUP)
                        .withVersion(CRD_VERSION)
                        .withScope(CRD_SCOPE)
                        .withNewNames()
                            .withKind(CRD_KIND)
                            .withSingular(CRD_SINGULAR_NAME)
                            .withPlural(CRD_PLURAL_NAME)
                            .withShortNames(CRD_SHORT_NAME)
                        .endNames()
                        .withNewValidation()
                            .withNewOpenAPIV3Schema()
                            .addToProperties(crdPropsMap)
                            .endOpenAPIV3Schema()
                        .endValidation()
                    .endSpec()
                    .build();
            distributeTrainCustomResourceDefinition = client.customResourceDefinitions().create(distributeTrainCustomResourceDefinition);
            log.info("create crd successfully : \n{}", SerializationUtils.dumpAsYaml(distributeTrainCustomResourceDefinition));
            crd = distributeTrainCustomResourceDefinition;
        }
        //注册到k8s反序列化解析器
        KubernetesDeserializer.registerCustomKind(CRD_GROUP + StrUtil.SLASH + CRD_VERSION, CRD_KIND, DistributeTrain.class);
        this.crd = crd;
    }

    /**
     * 初始化informer
     */
    public void initInformer(){
        CustomResourceDefinitionContext distributeTrainCustomResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
                .withVersion(CRD_VERSION)
                .withScope(CRD_SCOPE)
                .withGroup(CRD_GROUP)
                .withPlural(CRD_PLURAL_NAME)
                .build();

        SharedInformerFactory informerFactory = client.informers();

        MixedOperation<DistributeTrain, DistributeTrainList, DoneableDistributeTrain, Resource<DistributeTrain, DoneableDistributeTrain>> distributeTrainClient = client.customResources(this.crd, DistributeTrain.class, DistributeTrainList.class, DoneableDistributeTrain.class);
        SharedIndexInformer<DistributeTrain> distributeTrainSharedIndexInformer = informerFactory.sharedIndexInformerForCustomResource(distributeTrainCustomResourceDefinitionContext, DistributeTrain.class, DistributeTrainList.class, 10 * 60 * 1000);
        //使用静态变量维持
        DistributeTrainClientHolder.setDistributeTrainClient(distributeTrainClient);
        //手动注册controller到ioc容器
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DistributeTrainController.class);
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)((ConfigurableApplicationContext) SpringContextHolder.applicationContext).getBeanFactory();
        beanDefinitionBuilder.addConstructorArgValue(distributeTrainClient);
        beanDefinitionBuilder.addConstructorArgValue(distributeTrainSharedIndexInformer);
        beanDefinitionBuilder.addConstructorArgValue(namespace);
        beanFactory.registerBeanDefinition("org.onebrain.operator.controller.DistributeTrainController", beanDefinitionBuilder.getRawBeanDefinition());

        //取得托管的controller
        DistributeTrainController controller = SpringContextHolder.getBean(DistributeTrainController.class);
        //注册informer监听
        controller.create();
        informerFactory.startAllRegisteredInformers();
        //等待就绪
        controller.run();
    }

    /**
     * 生成crd属性
     * @return crd属性集合
     */
    private Map<String, JSONSchemaProps> buildCrdProperties(){
        Map<String, JSONSchemaProps> properties = Maps.newHashMap();
        JSONSchemaProps stringType = new JSONSchemaPropsBuilder()
                .withType(TYPE_STRING)
                .build();
        JSONSchemaProps intType = new JSONSchemaPropsBuilder()
                .withType(TYPE_INTEGER)
                .withFormat(FORMAT_INT_32)
                .build();
        JSONSchemaProps objectType = new JSONSchemaPropsBuilder()
                .withType(TYPE_OBJECT)
                .build();
        JSONSchemaProps arrayType = new JSONSchemaPropsBuilder()
                .withType(TYPE_ARRAY)
                .withNewItems()
                .endItems()
                .build();

        //添加属性校验规则
        JSONSchemaProps specObjectType = new JSONSchemaPropsBuilder()
                .addToProperties("image", stringType)
                .addToProperties("imagePullPolicy", stringType)
                .addToProperties("size", intType)
                .addToProperties("env", arrayType)
                .addToProperties("masterCmd", stringType)
                .addToProperties("slaveCmd", stringType)
                .addToProperties("masterResources", objectType)
                .addToProperties("slaveResources", objectType)
                .addToProperties("nodeSelector", objectType)
                .addToProperties("initContainer", objectType)
                .addToProperties("volumeMounts", arrayType)
                .addToProperties("volumes", arrayType)
                .addToProperties("tolerations", arrayType)
                .withType("object")
                .addToRequired("image", "imagePullPolicy", "size", "masterCmd", "slaveCmd")
                .build();
        properties.put("apiVersion", stringType);
        properties.put("kind", stringType);
        properties.put("metadata", objectType);
        properties.put("spec", specObjectType);
        return properties;
    }
}
