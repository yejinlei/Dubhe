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

package org.dubhe.handle;

import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description API版本控制
 * @date 2020-04-06
 */
public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {
  /**
   * 路径中版本的前缀， 这里用 /v[1-9]/的形式
   */
  private final static Pattern VERSION_PREFIX_PATTERN = Pattern.compile("v(\\d+)/");
  private int apiVersion;

  public ApiVersionCondition(int apiVersion) {
    this.apiVersion = apiVersion;
  }

  @Override
  public ApiVersionCondition combine(ApiVersionCondition other) {
    // 采用最后定义优先原则，则方法上的定义覆盖类上面的定义
    return new ApiVersionCondition(other.getApiVersion());
  }

  @Override
  public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
    Matcher m = VERSION_PREFIX_PATTERN.matcher(request.getRequestURI());
    if (m.find()) {
      Integer version = Integer.valueOf(m.group(1));
      if (version >= this.apiVersion) {
        return this;
      }
    }
    return null;
  }


  @Override
  public int compareTo(ApiVersionCondition other, HttpServletRequest request) {
    // 优先匹配最新的版本号
    return other.getApiVersion() - this.apiVersion;
  }
  public int getApiVersion() {
    return apiVersion;
  }
}
