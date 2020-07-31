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

import lombok.extern.slf4j.Slf4j;
import org.dubhe.domain.dto.EmailDTO;
import org.dubhe.exception.BaseErrorCode;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.MailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @Description 邮箱事件监听
 * @Date 2020-06-01
 */
@Component
@Slf4j
public class EmailEventListener {

    @Resource
    private MailService mailService;


    @EventListener
    @Async("taskExecutor")
    public void onApplicationEvent(EmailEvent event) {
        EmailDTO emailDTO = (EmailDTO) event.getSource();
        sendMail(emailDTO.getReceiverMailAddress(), emailDTO.getSubject(), emailDTO.getCode());
    }


    /**
     * 发送邮件
     *
     * @param receiverMailAddress 接受邮箱地址
     * @param subject             标题
     * @param code                验证码
     */
    public void sendMail(final String receiverMailAddress, String subject, String code) {
        try {
            final StringBuffer sb = new StringBuffer();
            sb.append("<h2>" + "亲爱的" + receiverMailAddress + "您好！</h2>")
                    .append("<p style='text-align: center; font-size: 24px; font-weight: bold'>您的验证码为:" + code + "</p>");
            mailService.sendHtmlMail(receiverMailAddress, subject, sb.toString());
        } catch (Exception e) {
            log.error("UserServiceImpl sendMail error , param:{} error:{}", receiverMailAddress, e);
            throw new BusinessException(BaseErrorCode.ERROR_SYSTEM.getCode(), BaseErrorCode.ERROR_SYSTEM.getMsg());
        }
    }

}
