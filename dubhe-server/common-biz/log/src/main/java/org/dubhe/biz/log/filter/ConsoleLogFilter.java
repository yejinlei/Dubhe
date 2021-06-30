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
import ch.qos.logback.core.spi.FilterReply;
import org.dubhe.biz.log.utils.LogUtil;
import org.slf4j.MarkerFactory;

/**
 * @description 自定义日志过滤器
 * @date 2020-07-21
 */
public class ConsoleLogFilter extends BaseLogFilter {

    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }
        return checkLevel(iLoggingEvent) ? onMatch : onMismatch;
    }

    @Override
    protected boolean checkLevel(ILoggingEvent iLoggingEvent) {


        return this.level != null
                && iLoggingEvent.getLevel() != null
                && iLoggingEvent.getLevel().toInt() >= this.level.toInt()
                && !MarkerFactory.getMarker(LogUtil.K8S_CALLBACK_LEVEL).equals(iLoggingEvent.getMarker())
                && !MarkerFactory.getMarker(LogUtil.SCHEDULE_LEVEL).equals(iLoggingEvent.getMarker())
                && !"log4jdbc.log4j2".equals(iLoggingEvent.getLoggerName());
    }
}