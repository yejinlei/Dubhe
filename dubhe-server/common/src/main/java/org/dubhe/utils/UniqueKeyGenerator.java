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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 唯一码生成器 （依赖时间轴）
 *
 * @date 2020.05.08
 */
public class UniqueKeyGenerator {

    private UniqueKeyGenerator(){

    }

    /**
     * 累加上限
     */
    private static final int ACCUMULATOR_MAX = 1000;

    /**
     * 累加器 format
     */
    private static final String ACCUMULATOR_FORMAT = "%0" + (ACCUMULATOR_MAX+"").length() + "d";

    /**
     * 时钟 format
     */
    private static final String DATE_FORMAT = "yyMMddHHmmssSSS";

    /**
     * 累加计数器仓库
     *
     * key:prefix
     * value:累加计数器
     */
    private static Map<String,AtomicInteger> prefixAccumulatorStorage = new ConcurrentHashMap<>();

    /**
     * 时间点仓库
     *
     * key:prefix
     * value:DATE_FORMAT 时钟
     */
    private static Map<String,String> prefixDateStorage = new ConcurrentHashMap<>();

    private static String getCurDateStr(){
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(new Date());
    }

    /**
     * 数字前位自动补0
     *
     * @param atomicInteger
     * @return
     */
    private static String getAccumulatorStr(AtomicInteger atomicInteger){
        return String.format(ACCUMULATOR_FORMAT,atomicInteger.intValue());
    }

    private static String generateKey(String prefix,String curDateStr,AtomicInteger accumulator){
        return prefix + curDateStr + getAccumulatorStr(accumulator);
    }

    /**
     * 生成唯一key
     *
     * @param prefix
     * @return
     */
    public static String generateKey(final String prefix){
        if (prefix == null){
            return null;
        }
        synchronized(prefix){
            String curDateStr = getCurDateStr();
            String prevDateStr = prefixDateStorage.get(prefix);
            if (curDateStr.equals(prevDateStr)){
                AtomicInteger accumulator = prefixAccumulatorStorage.get(prefix);
                if (accumulator.incrementAndGet() >= ACCUMULATOR_MAX){
                    return generateKey(prefix);
                }else {
                    updateStorage(prefix,curDateStr,accumulator);
                    return generateKey(prefix,curDateStr,accumulator);
                }
            }else {
                AtomicInteger accumulator = new AtomicInteger(0);
                updateStorage(prefix,curDateStr,accumulator);
                return generateKey(prefix,curDateStr,accumulator);
            }
        }
    }


    /**
     * 更新仓库
     *
     * @param prefix
     * @param curDateStr
     * @param accumulator
     */
    private static void updateStorage(String prefix, String curDateStr, AtomicInteger accumulator) {
        prefixAccumulatorStorage.put(prefix,accumulator);
        prefixDateStorage.put(prefix,curDateStr);
    }


}
