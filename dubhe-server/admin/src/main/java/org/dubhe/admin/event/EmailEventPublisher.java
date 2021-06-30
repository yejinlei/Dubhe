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

package org.dubhe.admin.event;

import org.dubhe.admin.domain.dto.EmailDTO;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description 邮箱事件发布者
 * @date 2020-06-01
 */
@Service
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
            LogUtil.error(LogEnum.SYS_ERR, "EmailEventPublisher sentEmailEvent error , param:{} error:{}", dto, e);
        }
    }
}
