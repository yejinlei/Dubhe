/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.event;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.domain.dto.EmailDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description 邮箱事件发布者
 * @Date 2020-06-01
 */
@Service
@Slf4j
public class EmailEventPublisher {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 邮件发送事件
     *
     * @param dto
     */
    @Async("taskExecutor")
    public void sentEmailEvent(final EmailDTO dto) {
        try {
            EmailEvent emailEvent = new EmailEvent(dto);
            applicationEventPublisher.publishEvent(emailEvent);
        } catch (Exception e) {
            log.error("EmailEventPublisher sentEmailEvent error , param:{} error:{}", JSONObject.toJSONString(dto), e);
        }
    }
}
