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

package org.dubhe.biz.file.exception;

import lombok.Getter;
import org.dubhe.biz.base.exception.BusinessException;

/**
 * @description NFS utils 工具异常
 * @date 2020-06-15
 */
@Getter
public class NfsBizException extends BusinessException {

    private static final long serialVersionUID = 1L;


    public NfsBizException(Throwable cause){
        super(cause);
    }

    public NfsBizException(String msg){
        super(msg);
    }


}
