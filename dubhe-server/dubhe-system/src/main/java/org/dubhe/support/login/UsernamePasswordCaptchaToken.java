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

package org.dubhe.support.login;

import lombok.Getter;
import org.apache.shiro.authc.UsernamePasswordToken;
/**
 * @Description : 账号密码令牌类
 * @Date 2020-06-01
 */
@Getter
public class UsernamePasswordCaptchaToken extends UsernamePasswordToken {

    private static final long serialVersionUID = 1L;

    private String dptoken;


    public UsernamePasswordCaptchaToken() {
        super();

    }

    public UsernamePasswordCaptchaToken(String username, String password,
                                        String dptoken) {
        super(username, password);
    }


    public UsernamePasswordCaptchaToken(String username, String password) {
        super(username, password);
    }

}
