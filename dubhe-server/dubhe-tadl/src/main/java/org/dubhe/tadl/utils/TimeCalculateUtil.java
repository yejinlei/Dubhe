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
package org.dubhe.tadl.utils;

import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.dubhe.tadl.enums.TimeUnitEnum;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.util.*;

public class TimeCalculateUtil {

    /**
     * 获取运行时间
     *
     * @param endTime   结束时间
     * @param startTime 开始时间
     * @return 运行时间
     */
    public static Long getRunTime(Timestamp endTime, Timestamp startTime) {
        Timestamp nowTime = new Timestamp(System.currentTimeMillis());
        if (ObjectUtils.isEmpty(startTime)) {
            return (long) NumberConstant.NUMBER_0;
        } else if (ObjectUtils.isEmpty(endTime)) {
            return nowTime.getTime() - startTime.getTime();
        } else {
            return endTime.getTime() - startTime.getTime();
        }
    }

    /**
     * 根据时间单位转换得出毫秒的时间
     *
     * @param timeUnit 时间单位
     * @param time     时间
     * @return 毫秒的时间
     */
    public static Long getTime(String timeUnit, Double time) {
        Double rTime = 0.0;
        switch (Objects.requireNonNull(TimeUnitEnum.getTimeUnit(timeUnit))) {
            case DAY:
                rTime = time * NumberConstant.NUMBER_24 * NumberConstant.NUMBER_60 * NumberConstant.NUMBER_60 * NumberConstant.NUMBER_1000;
                break;
            case HOUR:
                rTime =  time * NumberConstant.NUMBER_60 * NumberConstant.NUMBER_60 * NumberConstant.NUMBER_1000;
                break;
            case MIN:
                rTime =  time * NumberConstant.NUMBER_60 * NumberConstant.NUMBER_1000;
                break;
            default:
                throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
        return rTime.longValue();
    }


    public static class RunTime implements Comparable<RunTime> {
        private Long start;
        private Long end;


        public void setStart(Date start) {
            this.start = start.getTime();
        }

        public void setEnd(Date end) {
            this.end = end.getTime();
        }

        public Long getStart() {
            return start;
        }

        public Long getEnd() {
            return end;
        }

        @Override
        public int compareTo(RunTime other) {
            if (start .equals(other.start)) {
                return Long.valueOf(other.end - end).intValue();
            }
            return Long.valueOf(start - other.start).intValue();
        }
    }
}
