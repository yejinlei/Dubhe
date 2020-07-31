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

import ch.qos.logback.classic.Level;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dubhe.aspect.LogAspect;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.domain.entity.LogInfo;
import org.dubhe.enums.LogEnum;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.UUID;

/**
 * @description 日志工具类
 * @date 2020-06-29
 */
@Slf4j
public class LogUtil {
	/**
	 * info级别的日志
	 *
	 * @param logType 日志类型
	 * @param object  打印的日志参数
	 */
	public static void info(LogEnum logType, Object... object) {

		logHandle(logType, Level.INFO, object);
	}

	/**
	 * debug级别的日志
	 *
	 * @param logType 日志类型
	 * @param object  打印的日志参数
	 */
	public static void debug(LogEnum logType, Object... object) {
		logHandle(logType, Level.DEBUG, object);
	}

	/**
	 * error级别的日志
	 *
	 * @param logType 日志类型
	 * @param object  打印的日志参数
	 */
	public static void error(LogEnum logType, Object... object) {
		errorObjectHandle(object);
		logHandle(logType, Level.ERROR, object);
	}

	/**
	 * warn级别的日志
	 *
	 * @param logType 日志类型
	 * @param object  打印的日志参数
	 */
	public static void warn(LogEnum logType, Object... object) {
		logHandle(logType, Level.WARN, object);
	}

	/**
	 * trace级别的日志
	 *
	 * @param logType 日志类型
	 * @param object  打印的日志参数
	 */
	public static void trace(LogEnum logType, Object... object) {
		logHandle(logType, Level.TRACE, object);
	}

	/**
	 * 日志处理
	 *
	 * @param logType 日志类型
	 * @param level   日志级别
	 * @param object  打印的日志参数
	 */
	private static void logHandle(LogEnum logType, Level level, Object[] object) {

		LogInfo logInfo = generateLogInfo(logType, level, object);
		String logInfoJsonStr = logJsonStringLengthLimit(logInfo);
		switch (Level.toLevel(logInfo.getLevel()).levelInt) {
		case Level.TRACE_INT:
			log.trace(logInfoJsonStr);
			break;
		case Level.DEBUG_INT:
			log.debug(logInfoJsonStr);
			break;
		case Level.INFO_INT:
			log.info(logInfoJsonStr);
			break;
		case Level.WARN_INT:
			log.warn(logInfoJsonStr);
			break;
		case Level.ERROR_INT:
			log.error(logInfoJsonStr);
			break;
		default:
		}

	}

	/**
	 * 日志信息组装的内部方法
	 *
	 * @param logType 日志类型
	 * @param level   日志级别
	 * @param object  打印的日志参数
	 * @return LogInfo日志对象信息
	 */
	private static LogInfo generateLogInfo(LogEnum logType, Level level, Object[] object) {
		LogInfo logInfo = new LogInfo();
		// 日志类型检测
		if (!LogEnum.isLogType(logType)) {
			level = Level.ERROR;
			object = new Object[MagicNumConstant.ONE];
			object[MagicNumConstant.ZERO] = String.valueOf("logType【").concat(String.valueOf(logType))
					.concat("】is error !");
			logType = LogEnum.SYS_ERR;
		}

		// 获取trace_id
		if (StringUtils.isEmpty(MDC.get(LogAspect.TRACE_ID))) {
			MDC.put(LogAspect.TRACE_ID, UUID.randomUUID().toString());
		}
		// 设置logInfo的level,type,traceId属性
		logInfo.setLevel(level.levelStr).setType(logType.toString()).setTraceId(MDC.get(LogAspect.TRACE_ID));
		// 设置logInfo的堆栈信息
		setLogStackInfo(logInfo);
		// 设置logInfo的info信息
		setLogInfo(logInfo, object);
		// 截取loginfo的长度并转换成json字符串
		return logInfo;

	}

	/**
	 * 设置loginfo的堆栈信息
	 *
	 * @param logInfo 日志对象
	 */
	private static void setLogStackInfo(LogInfo logInfo) {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		if (elements.length >= MagicNumConstant.SIX) {
			logInfo.setCName(elements[MagicNumConstant.FIVE].getClassName())
					.setMName(elements[MagicNumConstant.FIVE].getMethodName())
					.setLine(String.valueOf(elements[MagicNumConstant.FIVE].getLineNumber()));
		}
	}

	/**
	 * 限制log日志的长度并转换成json
	 *
	 * @param logInfo 日志对象
	 * @return String 日志对象Json字符串
	 */
	private static String logJsonStringLengthLimit(LogInfo logInfo) {
		try {
			String jsonString = JSON.toJSONString(logInfo.getInfo());
			if (jsonString.length() > MagicNumConstant.TEN_THOUSAND) {
				jsonString = jsonString.substring(MagicNumConstant.ZERO, MagicNumConstant.TEN_THOUSAND);
			}

			logInfo.setInfo(jsonString);
			jsonString = JSON.toJSONString(logInfo);
			jsonString = jsonString.replace(SymbolConstant.BACKSLASH_MARK, SymbolConstant.MARK)
					.replace(SymbolConstant.DOUBLE_MARK, SymbolConstant.MARK)
					.replace(SymbolConstant.BRACKETS, SymbolConstant.BLANK);

			return jsonString;

		} catch (Exception e) {
			logInfo.setLevel(Level.ERROR.levelStr).setType(LogEnum.SYS_ERR.toString())
					.setInfo("cannot serialize exception: " + ExceptionUtils.getStackTrace(e));
			return JSON.toJSONString(logInfo);
		}
	}

	/**
	 * 设置日志对象的info信息
	 *
	 * @param logInfo 日志对象
	 * @param object  打印的日志参数
	 */
	private static void setLogInfo(LogInfo logInfo, Object[] object) {
		for (Object obj : object) {
			if (obj instanceof Exception) {
				log.error((ExceptionUtils.getStackTrace((Throwable) obj)));
			}
		}

		if (object.length > MagicNumConstant.ONE) {
			logInfo.setInfo(MessageFormatter.arrayFormat(object[MagicNumConstant.ZERO].toString(),
					Arrays.copyOfRange(object, MagicNumConstant.ONE, object.length)).getMessage());

		} else if (object.length == MagicNumConstant.ONE && object[MagicNumConstant.ZERO] instanceof Exception) {
			logInfo.setInfo((ExceptionUtils.getStackTrace((Exception) object[MagicNumConstant.ZERO])));
		} else if (object.length == MagicNumConstant.ONE) {
			logInfo.setInfo(
					object[MagicNumConstant.ZERO] == null ? SymbolConstant.BLANK : object[MagicNumConstant.ZERO]);
		} else {
			logInfo.setInfo(SymbolConstant.BLANK);
		}

	}

	/**
	 * 处理Exception的情况
	 *
	 * @param object 打印的日志参数
	 */
	private static void errorObjectHandle(Object[] object) {
		if (object.length >= MagicNumConstant.TWO) {
			object[MagicNumConstant.ZERO] = String.valueOf(object[MagicNumConstant.ZERO])
					.concat(SymbolConstant.BRACKETS);
		}

		if (object.length == MagicNumConstant.TWO && object[MagicNumConstant.ONE] instanceof Exception) {
			log.error((ExceptionUtils.getStackTrace((Throwable) object[MagicNumConstant.ONE])));
			object[MagicNumConstant.ONE] = ExceptionUtils.getStackTrace((Exception) object[MagicNumConstant.ONE]);

		} else if (object.length >= MagicNumConstant.THREE) {
			for (int i = 0; i < object.length; i++) {
				if (object[i] instanceof Exception) {
					log.error((ExceptionUtils.getStackTrace((Throwable) object[i])));
					object[i] = ExceptionUtils.getStackTrace((Exception) object[i]);
				}

			}
		}
	}

}
