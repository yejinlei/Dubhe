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
package org.dubhe.serving.client;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.serving.client.fallback.MailClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description 邮箱服务远程调用
 * @date 2021-01-21
 */
@FeignClient(value = ApplicationNameConst.SERVER_ADMIN, contextId = "mailClient", fallback = MailClientFallback.class)
public interface MailClient {

    /**
     * HTML 文本邮件
     *
     * @param to      接收者邮件
     * @param subject 邮件主题
     * @param content HTML内容
     */
    @PostMapping(value = "/mail/sendHtmlMail")
    DataResponseBody sendHtmlMail(@RequestParam(value = "to") String to, @RequestParam(value = "subject") String subject, @RequestParam("content") String content);
}
