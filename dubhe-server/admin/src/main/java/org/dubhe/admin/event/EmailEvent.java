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


/**
 * @description 邮件事件
 * @date 2020-06-01
 */
public class EmailEvent extends BaseEvent<EmailDTO> {

    private static final long serialVersionUID = 8103187726344703089L;

    public EmailEvent(EmailDTO msg) {
        super(msg);
    }

    public EmailEvent(Object source, EmailDTO msg) {
        super(source, msg);
    }

}
