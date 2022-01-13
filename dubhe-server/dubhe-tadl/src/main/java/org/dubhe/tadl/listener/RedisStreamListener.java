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
package org.dubhe.tadl.listener;

import org.dubhe.tadl.domain.dto.ExperimentAndTrailDTO;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;


/**
 * @description redis stream 监听
 * @date 2021-03-04
 */
@Component
public class RedisStreamListener implements StreamListener<String, ObjectRecord<String, ExperimentAndTrailDTO>> {

    /**
     * redis 有消息  通知接口运行
     *
     * @param mapRecord
     */
    @Override
    public void onMessage(ObjectRecord<String, ExperimentAndTrailDTO> mapRecord) {

    }
}
