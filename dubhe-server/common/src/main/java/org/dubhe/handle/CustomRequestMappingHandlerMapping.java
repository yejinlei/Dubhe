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

import lombok.Data;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.annotation.ApiVersion;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
/**
 * @description 权限注解收集
 * @date 2020-04-06
 */
@Component
@Data
public class CustomRequestMappingHandlerMapping   extends RequestMappingHandlerMapping {
  Set<String> permissionsSet =new HashSet<String>();
  @Override
  protected RequestCondition<ApiVersionCondition> getCustomTypeCondition(Class<?> handlerType) {
    ApiVersion apiVersion = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
    return createCondition(apiVersion);
  }

  @Override
  protected RequestCondition<ApiVersionCondition> getCustomMethodCondition(Method method) {
    RequiresPermissions requiresPermissions = AnnotationUtils.findAnnotation(method, RequiresPermissions.class);
    if(requiresPermissions != null){
      if(requiresPermissions.value()!=null){
        for(int i=0;i<requiresPermissions.value().length;i++){
          permissionsSet.add(requiresPermissions.value()[i]);
        }
      }

    }

    ApiVersion apiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
    return createCondition(apiVersion);
  }
  private RequestCondition<ApiVersionCondition> createCondition(ApiVersion apiVersion) {
    return apiVersion == null ? null : new ApiVersionCondition(apiVersion.value());
  }

}
