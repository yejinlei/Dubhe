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

package org.dubhe.biz.log.utils;

import ch.qos.logback.classic.Level;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.aspect.LogAspect;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.log.entity.LogInfo;
import org.dubhe.biz.log.enums.LogEnum;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.UUID;

/**
 * @description 日志工具类
 * @date 2020-06-29
 */
@Slf4j
public class LogUtil {

    private static final String TRACE_TYPE = "TRACE_TYPE";

    public static final String SCHEDULE_LEVEL = "SCHEDULE";

    public static final String K8S_CALLBACK_LEVEL = "K8S_CALLBACK";

    private static final String GLOBAL_REQUEST_LEVEL = "GLOBAL_REQUEST";

    private static final String TRACE_LEVEL = "TRACE";

    private static final String DEBUG_LEVEL = "DEBUG";

    private static final String INFO_LEVEL = "INFO";

    private static final String WARN_LEVEL = "WARN";

    private static final String ERROR_LEVEL = "ERROR";


    public static void startScheduleTrace() {
        MDC.put(TRACE_TYPE, SCHEDULE_LEVEL);
    }

    public static void startK8sCallbackTrace() {
        MDC.put(TRACE_TYPE, K8S_CALLBACK_LEVEL);
    }

    public static void cleanTrace() {
        MDC.clear();
    }

    /**
     * info级别的日志
     *
     * @param logType 日志类型
     * @param object  打印的日志参数
     * @return void
     */

    public static void info(LogEnum logType, Object... object) {

        logHandle(logType, Level.INFO, object);
    }

    /**
     * debug级别的日志
     *
     * @param logType 日志类型
     * @param object  打印的日志参数
     * @return void
     */
    public static void debug(LogEnum logType, Object... object) {
        logHandle(logType, Level.DEBUG, object);
    }

    /**
     * error级别的日志
     *
     * @param logType 日志类型
     * @param object  打印的日志参数
     * @return void
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
     * @return void
     */
    public static void warn(LogEnum logType, Object... object) {
        logHandle(logType, Level.WARN, object);
    }

    /**
     * trace级别的日志
     *
     * @param logType 日志类型
     * @param object  打印的日志参数
     * @return void
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
     * @return void
     */
    private static void logHandle(LogEnum logType, Level level, Object[] object) {

        LogInfo logInfo = generateLogInfo(logType, level, object);

        switch (logInfo.getLevel()) {
            case TRACE_LEVEL:
                log.trace(MarkerFactory.getMarker(TRACE_LEVEL), logJsonStringLengthLimit(logInfo));
                break;
            case DEBUG_LEVEL:
                log.debug(MarkerFactory.getMarker(DEBUG_LEVEL), logJsonStringLengthLimit(logInfo));
                break;
            case GLOBAL_REQUEST_LEVEL:
                logInfo.setLevel(null);
                logInfo.setType(null);
                logInfo.setLocation(null);
                log.info(MarkerFactory.getMarker(GLOBAL_REQUEST_LEVEL), logJsonStringLengthLimit(logInfo));
                break;
            case SCHEDULE_LEVEL:
                log.info(MarkerFactory.getMarker(SCHEDULE_LEVEL), logJsonStringLengthLimit(logInfo));
                break;
            case K8S_CALLBACK_LEVEL:
                log.info(MarkerFactory.getMarker(K8S_CALLBACK_LEVEL), logJsonStringLengthLimit(logInfo));
                break;
            case INFO_LEVEL:
                log.info(MarkerFactory.getMarker(INFO_LEVEL), logJsonStringLengthLimit(logInfo));
                break;
            case WARN_LEVEL:
                log.warn(MarkerFactory.getMarker(WARN_LEVEL), logJsonStringLengthLimit(logInfo));
                break;
            case ERROR_LEVEL:
                log.error(MarkerFactory.getMarker(ERROR_LEVEL), logJsonStringLengthLimit(logInfo));
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
     * @return LogInfo
     */
    private static LogInfo generateLogInfo(LogEnum logType, Level level, Object[] object) {


        LogInfo logInfo = new LogInfo();
        // 日志类型检测
        if (!LogEnum.isLogType(logType)) {
            level = Level.ERROR;
            object = new Object[MagicNumConstant.ONE];
            object[MagicNumConstant.ZERO] = "日志类型【".concat(String.valueOf(logType)).concat("】不正确！");
            logType = LogEnum.SYS_ERR;
        }

        // 获取trace_id
        if (StringUtils.isEmpty(MDC.get(LogAspect.TRACE_ID))) {
            MDC.put(LogAspect.TRACE_ID, UUID.randomUUID().toString());
        }
        // 设置logInfo的level,type,traceId属性
        logInfo.setLevel(level.levelStr)
                .setType(logType.toString())
                .setTraceId(MDC.get(LogAspect.TRACE_ID));


        //自定义日志级别
        //LogEnum、 MDC中的 TRACE_TYPE 做日志分流标识
        if (Level.INFO.toInt() == level.toInt()) {
            if (LogEnum.GLOBAL_REQ.equals(logType)) {
                //info全局请求
                logInfo.setLevel(GLOBAL_REQUEST_LEVEL);
            } else if (LogEnum.BIZ_K8S.equals(logType)) {
                logInfo.setLevel(K8S_CALLBACK_LEVEL);
            } else {
                //schedule定时等 链路记录
                String traceType = MDC.get(TRACE_TYPE);
                if (StringUtils.isNotBlank(traceType)) {
                    logInfo.setLevel(traceType);
                }
            }
        }

        // 设置logInfo的堆栈信息
        setLogStackInfo(logInfo);
        // 设置logInfo的info信息
        setLogInfo(logInfo, object);
        // 截取logInfo的长度并转换成json字符串
        return logInfo;
    }

    /**
     * 设置loginfo的堆栈信息
     *
     * @param logInfo 日志对象
     * @return void
     */
    private static void setLogStackInfo(LogInfo logInfo) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length >= MagicNumConstant.SIX) {
            StackTraceElement element = elements[MagicNumConstant.FIVE];
            logInfo.setLocation(String.format("%s#%s:%s", element.getClassName(), element.getMethodName(), element.getLineNumber()));
        }
    }

    /**
     * 限制log日志的长度并转换成json
     *
     * @param logInfo 日志对象
     * @return String
     */
    private static String logJsonStringLengthLimit(LogInfo logInfo) {
        try {

            String jsonString = JSON.toJSONString(logInfo);
            if (StringUtils.isBlank(jsonString)) {
                return "";
            }
            if (jsonString.length() > MagicNumConstant.TEN_THOUSAND) {
                String trunk = logInfo.getInfo().toString().substring(MagicNumConstant.ZERO, MagicNumConstant.NINE_THOUSAND);
                logInfo.setInfo(trunk);
                jsonString = JSON.toJSONString(logInfo);
            }
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
     * @return void
     */
    private static void setLogInfo(LogInfo logInfo, Object[] object) {

        if (object.length > MagicNumConstant.ONE) {
            logInfo.setInfo(MessageFormatter.arrayFormat(object[MagicNumConstant.ZERO].toString(),
                    Arrays.copyOfRange(object, MagicNumConstant.ONE, object.length)).getMessage());

        } else if (object.length == MagicNumConstant.ONE && object[MagicNumConstant.ZERO] instanceof Exception) {
            logInfo.setInfo((ExceptionUtils.getStackTrace((Exception) object[MagicNumConstant.ZERO])));
            log.error((ExceptionUtils.getStackTrace((Exception) object[MagicNumConstant.ZERO])));
        } else if (object.length == MagicNumConstant.ONE) {
            logInfo.setInfo(object[MagicNumConstant.ZERO] == null ? "" : object[MagicNumConstant.ZERO]);
        } else {
            logInfo.setInfo("");
        }

    }

    /**
     * 处理Exception的情况
     *
     * @param object 打印的日志参数
     * @return void
     */
    private static void errorObjectHandle(Object[] object) {

        if (object.length == MagicNumConstant.TWO && object[MagicNumConstant.ONE] instanceof Exception) {
            log.error(String.valueOf(object[MagicNumConstant.ZERO]), (Exception) object[MagicNumConstant.ONE]);
            object[MagicNumConstant.ONE] = ExceptionUtils.getStackTrace((Exception) object[MagicNumConstant.ONE]);

        } else if (object.length >= MagicNumConstant.THREE) {
            log.error(String.valueOf(object[MagicNumConstant.ZERO]),
                    Arrays.copyOfRange(object, MagicNumConstant.ONE, object.length));
            for (int i = 0; i < object.length; i++) {
                if (object[i] instanceof Exception) {
                    object[i] = ExceptionUtils.getStackTrace((Exception) object[i]);
                }

            }
        }
    }
}
