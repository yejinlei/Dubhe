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


import java.util.List;

/**
 * @description trial状态
 * @date 2020-08-27
 */
public abstract class AbstractTrialState {

    /**
     * 运行trial 事件
     * @param trialId trial id
     */
  public void runningTrialEvent(Long trialId){}
    /**
     * 运行完成trial 事件
     * @param trialId trial id
     */
  public void finishedTrialEvent(Long trialId){}

    /**
     * 运行失败trial 事件
     * @param trialId trial id
     */
  public void failedTrialEvent(Long trialId,String statusDetail){}

  /**
   * 待运行trial 事件
   * @param trialId trial id
   */
  public void toRunTrialEvent(Long trialId){}
  /**
   * 待运行trial 事件
   * @param trialId trial id
   */
  public void toRunBatchTrialEvent(List<Long> trialId){}

  /**
   * 运行异常trial 事件
   * @param trialId trial id
   */
  public void unknownTrialEvent(Long trialId,String statusDetail){}

  /**
   * 等待中trial 事件
   * @param trialId
   */
  public void waitingTrialEvent(Long trialId){}

  public String currentStatus(){return null;};
}
