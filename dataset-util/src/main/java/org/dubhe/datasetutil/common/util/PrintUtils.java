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
package org.dubhe.datasetutil.common.util;

/**
 * @description 控制台字体颜色
 * @date 2020-10-22
 */
public class PrintUtils {
    /**
     * 白色
     */
    public static final int WHITE = 30;

    /**
     * 白色背景
     */
    public static final int WHITE_BACKGROUND = 40;

    /**
     * 红色
     */
    public static final int RED = 31;

    /**
     * 红色背景
     */
    public static final int RED_BACKGROUND = 41;

    /**
     * 绿色
     */
    public static final int GREEN = 32;
    
    /**
     * 绿色背景
     */
    public static final int GREEN_BACKGROUND = 42;

    /**
     * 黄色
     */
    public static final int YELLOW = 33;

    /**
     * 黄色背景
     */
    public static final int YELLOW_BACKGROUND = 43;

    /**
     * 蓝色
     */
    public static final int BLUE = 34;

    /**
     * 蓝色背景
     */
    public static final int BLUE_BACKGROUND = 44;

    /**
     * 品红（洋红）
     */
    public static final int MAGENTA = 35;

    /**
     * 品红背景
     */
    public static final int MAGENTA_BACKGROUND = 45;

    /**
     * 蓝绿
     */
    public static final int CYAN = 36;

    /**
     * 蓝绿背景
     */
    public static final int CYAN_BACKGROUND = 46;

    /**
     * 黑色
     */
    public static final int BLACK = 37;

    /**
     * 黑色背景
     */
    public static final int BLACK_BACKGROUND = 47;

    /**
     * 粗体
     */
    public static final int BOLD = 1;

    /**
     * 斜体
     */
    public static final int ITATIC = 3;

    /**
     * 下划线
     */
    public static final int UNDERLINE = 4;

    /**
     * 反转
     */
    public static final int REVERSE = 7;

    /**
     * 格式化
     *
     * @param txt   文本
     * @param codes 信息
     * @return String 格式化后的内容
     */       
    private static String FMT(String txt, int... codes) {
        StringBuffer sb = new StringBuffer();
        for (int code : codes) {
            sb.append(code + ";");
        }
        String _code = sb.toString();
        if (_code.endsWith(";")) {
            _code = _code.substring(0, _code.length() - 1);
        }
        return (char) 27 + "[" + _code + "m" + txt + (char) 27 + "[0m";
    }

    /**
     * 打印并换行
     */
    public static void printLine(String txt, int... codes) {
        System.out.println(FMT(txt, codes));
    }

    /**
     * 默认打印红色文字
     */
    public static void PN(String txt) {
        System.out.println(FMT(txt, new int[]{RED}));
    }
}
