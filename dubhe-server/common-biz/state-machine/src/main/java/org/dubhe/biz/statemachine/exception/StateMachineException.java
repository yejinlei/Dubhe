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
package org.dubhe.biz.statemachine.exception;

import lombok.Getter;
import org.dubhe.biz.base.exception.BusinessException;

/**
 * @description 状态机异常类
 * @date 2020-08-27
 */
@Getter
public class StateMachineException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * 自定义状态机异常(抛出异常堆栈信息)
     *
     * @param cause
     */
    public StateMachineException(Throwable cause){
        super(cause);
    }

    /**
     * 自定义状态机异常(抛出异常信息)
     *
     * @param msg
     */
    public StateMachineException(String msg){
        super(msg);
    }

}