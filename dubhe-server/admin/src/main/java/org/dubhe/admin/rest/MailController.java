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
package org.dubhe.admin.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.service.MailService;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.mail.MessagingException;

/**
 * @description 邮件服务
 * @date 2021-01-21
 */
@Api(tags = "系统：邮件服务")
@RestController
@RequestMapping("/mail")
public class MailController {

    @Resource
    private MailService mailService;

    @ApiOperation("发送html邮件")
    @PostMapping(value = "/sendHtmlMail")
    public DataResponseBody sendHtmlMail(@RequestParam(value = "to") String to, @RequestParam(value = "subject") String subject, @RequestParam("content") String content) throws MessagingException {
        mailService.sendHtmlMail(to, subject, content);
        return new DataResponseBody();
    }

}
