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

package org.dubhe.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.dubhe.utils.JwtUtils;

/**
 * @Description : Jwt 令牌管理类
 * @Date 2020-06-01
 */
public class JwtToken implements AuthenticationToken {

  private static final long serialVersionUID = 1L;
  /**
   * 加密后的 JWT token串
   */
  private String token;

  private String userName;

  public JwtToken(String token) {
    this.token = token;
    this.userName = JwtUtils.getUserName(token);
  }

  @Override
  public Object getPrincipal() {
    return this.userName;
  }

  @Override
  public Object getCredentials() {
    return token;
  }

}
