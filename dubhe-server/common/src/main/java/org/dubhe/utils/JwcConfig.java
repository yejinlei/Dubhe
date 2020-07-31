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

package org.dubhe.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
/**
 * @description  JWT配置类
 * @date 2020-04-15
 */
@Configuration
public class JwcConfig {
  @Value("${jwt.online-key}")
  public    String onlineKey;
  @Autowired
  public    RedisUtils redisUtils;
  @Value("${jwt.token-validity-in-seconds}")
  public   Long jwtExpiration;
  @Value("${jwt.base64-secret}")
  public   String secret;
}
