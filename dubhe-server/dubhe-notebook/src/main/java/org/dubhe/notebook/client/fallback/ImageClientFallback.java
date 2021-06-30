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
package org.dubhe.notebook.client.fallback;

import org.dubhe.biz.base.dto.PtImageQueryUrlDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.notebook.client.ImageClient;
import org.springframework.stereotype.Component;

/**
 * @description Image远程服务调用熔断类
 * @date 2020-12-15
 */
@Component
public class ImageClientFallback implements ImageClient {
    @Override
    public DataResponseBody getImageUrl(PtImageQueryUrlDTO ptImageQueryUrlDTO) {
        return DataResponseFactory.failed("call dubhe-image server getImageUrl error");
    }
}
