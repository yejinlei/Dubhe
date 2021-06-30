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
package org.dubhe.biz.log.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @description 日志切面
 * @date 2020-04-10
 */
@Component
@Aspect
@Slf4j
public class LogAspect {

	public static final String TRACE_ID = "traceId";

	@Pointcut("execution(* org.dubhe..task..*.*(..)))")
	public void serviceAspect() {
	}

	@Pointcut("execution(* org.dubhe..rest..*.*(..))) ")
	public void restAspect() {
	}

	@Pointcut(" serviceAspect() ")
	public void aroundAspect() {
	}

	@Around("aroundAspect()")
	public Object around(JoinPoint joinPoint) throws Throwable {
		if (StringUtils.isEmpty(MDC.get(TRACE_ID))) {
			MDC.put(TRACE_ID, UUID.randomUUID().toString());
		}
		return ((ProceedingJoinPoint) joinPoint).proceed();
	}

	@Around("restAspect()")
	public Object aroundRest(JoinPoint joinPoint) throws Throwable {
		MDC.clear();
		MDC.put(TRACE_ID, UUID.randomUUID().toString());
		return combineLogInfo(joinPoint);
	}

	private Object combineLogInfo(JoinPoint joinPoint) throws Throwable {
		Object[] param = joinPoint.getArgs();
		LogUtil.info(LogEnum.LOG_ASPECT, "uri:{},input:{},==>begin", joinPoint.getSignature(), param);
		long start = System.currentTimeMillis();
		Object result = ((ProceedingJoinPoint) joinPoint).proceed();
		long end = System.currentTimeMillis();
		LogUtil.info(LogEnum.LOG_ASPECT, "uri:{},output:{},proc_time:{}ms,<==end", joinPoint.getSignature().toString(),
				result, end - start);
		return result;
	}

}
