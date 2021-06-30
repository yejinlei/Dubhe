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

package org.dubhe.biz.log.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @description 全局请求 日志过滤器
 * @date 2020-08-13
 */
public class GlobalRequestLogFilter extends BaseLogFilter {


    @Override
    public boolean checkLevel(ILoggingEvent iLoggingEvent) {
        return this.level != null;
    }

}