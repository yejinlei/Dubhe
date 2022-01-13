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
package org.dubhe.tadl.machine.state;


/**
 * @description 文件抽象类
 * @date 2020-08-27
 */
public abstract class AbstractExperimentState {
    /**
     * 运行实验事件 暂停/待运行/等待中 -->运行中
     * @param experimentId 实验id
     */
    public  void runningExperimentEvent(Long experimentId){}

    /**
     * 暂停实验事件 运行中-->已暂停
     * @param experimentId 实验id
     */
    public void pausedExperimentEvent(Long experimentId){}
    /**
     * 运行完成实验事件 运行中-->已完成
     * @param experimentId 实验id
     */
    public void finishedExperimentEvent(Long experimentId){}

    /**
     * 运行失败实验事件 运行中-->运行失败
     * @param experimentId 实验id
     */
    public void failedExperimentEvent(Long experimentId,String statusDetail){}
    /**
     * 等待中实验事件 暂停中-->等待中
     */
    public void waitingExperimentEvent(Long experimentId){}

    /**
     * 删除实验相关信息
     * @param experimentId 实验id
     */
    public void deleteExperimentInfoEvent(Long experimentId){}


    public String currentStatus(){return null;};

}
