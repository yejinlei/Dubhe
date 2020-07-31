/**
 * Copyright 2019-2020 Zheng Jie
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
 */
package org.dubhe.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @description 自定义日志过滤器
 * @date 2020-07-21
 */
public class FileLogFilter extends AbstractMatcherFilter<ILoggingEvent> {

	Level level;

	/**
	 * 重写decide方法
	 *
	 * @param iLoggingEvent event to decide upon.
	 * @return FilterReply
	 */
	@Override
	public FilterReply decide(ILoggingEvent iLoggingEvent) {
		if (!isStarted()) {
			return FilterReply.NEUTRAL;
		}
		if (iLoggingEvent.getLevel().equals(level) && iLoggingEvent.getMessage() != null
				&& iLoggingEvent.getMessage().startsWith("{") && iLoggingEvent.getMessage().endsWith("}")) {
			return onMatch;
		}
		return onMismatch;

	}

	public void setLevel(Level level) {
		this.level = level;
	}

	@Override
	public void start() {
		if (this.level != null) {
			super.start();
		}
	}
}