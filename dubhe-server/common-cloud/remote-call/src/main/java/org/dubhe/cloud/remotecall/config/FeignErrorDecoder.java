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

package org.dubhe.cloud.remotecall.config;




import com.alibaba.fastjson.JSONObject;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.dubhe.biz.base.exception.FeignException;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.context.annotation.Configuration;


/**
 * @description feign 异常处理类
 * @date 2020-12-21
 */

@Configuration
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        FeignException baseException = null;
        try {
            String errorContent = Util.toString(response.body().asReader());
            DataResponseBody result = JSONObject.parseObject(errorContent, DataResponseBody.class);
            baseException = new FeignException(result.getCode(), result.getMsg());
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR,"FeignClient error :{}",e);
        }
        return baseException;
    }
}
