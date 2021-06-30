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

package org.dubhe.k8s.utils;

import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.RegexUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.k8s.constant.K8sParamConstants;

/**
 * @description k8s 资源单位转换
 * @date 2020-10-13
 */
public class UnitConvertUtils {

    /**
     * cpu转化为 n
     * @param amount 值
     * @param format 单位
     * @return
     */
    public static Long cpuFormatToN(String amount,String format){
        if (StringUtils.isEmpty(amount) || !RegexUtil.isDigits(amount)){
            return MagicNumConstant.ZERO_LONG;
        }
        if (StringUtils.isEmpty(format)){
            return Long.valueOf(amount) * MagicNumConstant.ONE_THOUSAND * MagicNumConstant.MILLION;
        }
        if (K8sParamConstants.CPU_UNIT.equals(format)){
            return Long.valueOf(amount) * MagicNumConstant.MILLION;
        }
        return Long.valueOf(amount);
    }

    /**
     * 内存转为 Mi
     * @param amount 值
     * @param format 单位
     * @return
     */
    public static Long memFormatToMi(String amount,String format){
        if (StringUtils.isEmpty(amount) || !RegexUtil.isDigits(amount)){
            return MagicNumConstant.ZERO_LONG;
        }
        if (K8sParamConstants.MEM_UNIT_TI.equals(format)){
            return Long.valueOf(amount) * MagicNumConstant.BINARY_TEN_EXP * MagicNumConstant.BINARY_TEN_EXP;
        }
        if (K8sParamConstants.MEM_UNIT_GI.equals(format)){
            return Long.valueOf(amount) * MagicNumConstant.BINARY_TEN_EXP;
        }
        if (K8sParamConstants.MEM_UNIT_KI.equals(format)){
            return Long.valueOf(amount) / MagicNumConstant.BINARY_TEN_EXP;
        }
        return Long.valueOf(amount);
    }
}
