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
package org.dubhe.k8s.utils;

/**
 * @description k8s Pod 工具类
 * @date 2020-09-17
 */
public class PodUtil {

    private PodUtil(){

    }

    /**
     * 判断节点是否是master节点
     * @param podName k8s pod名称
     * @return true master节点,false 其他
     */
    public static boolean isMaster(String podName){
        if(podName == null){
            return false;
        }
        return podName.contains("-master-");
    }

    /**
     * 判断节点是否是slave节点
     * @param podName k8s pod名称
     * @return true slave节点,false 其他
     */
    public static boolean isSlave(String podName){
        if(podName == null){
            return false;
        }
        return podName.contains("-slave-");
    }


}
