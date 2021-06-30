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

package org.dubhe.cloud.authconfig.decorator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @description 线程池设置ThreadLocal用户信息
 * @date 2021-06-10
 */
@Slf4j
public class AuthenticationThreadLocalTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return () -> {
            try {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                runnable.run();
            } catch (Throwable e){
                log.error(e.getMessage(), e);
            } finally {
                try {
                    SecurityContextHolder.getContext().setAuthentication(null);
                } catch (Exception e){
                    log.error(e.getMessage(), e);
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
