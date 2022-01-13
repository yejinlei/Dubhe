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

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.AesUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.k8s.enums.BusinessLabelServiceNameEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description k8s命名相关工具类 token使用MD5加密，并设置超时时间
 * @date 2020-05-28
 */
@Component
public class K8sCallBackTool {

    /**
     * k8s 回调 token秘钥
     */
    @Value("${k8s.callback.token.secret-key}")
    private String secretKey;
    /**
     * k8s 回调token超时时间
     */
    @Value("${k8s.callback.token.expire-seconds}")
    private Integer expireSeconds;
    /**
     * k8s 回调域名或IP:Port
     */
    @Value("${k8s.callback.url}")
    private String url;

    /**
     * k8s 回调token key
     */
    public static final String K8S_CALLBACK_TOKEN = AuthConst.K8S_CALLBACK_TOKEN;
    /**
     * 失败重试次数
     */
    private static final int RETRY_COUNT = 3;

    /**
     * k8s 回调匹配地址
     */
    private static final List<String> K8S_CALLBACK_PATH;
    /**
     * k8s 回调路径
     */
    private static final String K8S_CALLBACK_PATH_DEPLOYMENT = "/api/k8s/callback/deployment/";
    public static final String K8S_CALLBACK_PATH_POD = StringConstant.K8S_CALLBACK_URI + SymbolConstant.SLASH;

    static {
        K8S_CALLBACK_PATH = new ArrayList<>();
        // 添加需要token权限校验的地址（Shiro匿名访问的地址）
        K8S_CALLBACK_PATH.add(K8S_CALLBACK_PATH_POD + "**");
        K8S_CALLBACK_PATH.add(K8S_CALLBACK_PATH_DEPLOYMENT + "**");
    }

    /**
     * 获取 k8s 回调匹配地址
     *
     * @return List<String>
     */
    public static List<String> getK8sCallbackPaths() {
        return new ArrayList<>(K8S_CALLBACK_PATH);
    }


    /**
     * 生成k8s回调
     *
     * @return String
     */
    public String generateToken() {
        String expireTime = DateUtil.format(
                DateUtil.offset(new Date(), DateField.SECOND, expireSeconds),
                DatePattern.PURE_DATETIME_PATTERN
        );
        return AesUtil.encrypt(expireTime, secretKey);
    }

    /**
     * 验证token
     *
     * @param token
     * @return boolean
     */
    public boolean validateToken(String token) {
        String expireTime = AesUtil.decrypt(token, secretKey);
        if (StringUtils.isEmpty(expireTime)) {
            return false;
        }
        String nowTime = DateUtil.format(
                new Date(),
                DatePattern.PURE_DATETIME_PATTERN
        );
        return expireTime.compareTo(nowTime) > 0;
    }


    /**
     * 判断当前是否可以再次重试
     *
     * @param retryTimes 第n次试图重试
     * @return boolean
     */
    public boolean continueRetry(int retryTimes) {
        return retryTimes <= RETRY_COUNT;
    }

    /**
     * 获取回调地址
     *
     * @param podLabel
     * @return String
     */
    public String getPodCallbackUrl(String podLabel) {
        return "http://" + BusinessLabelServiceNameEnum.getServiceNameByBusinessLabel(podLabel) + K8S_CALLBACK_PATH_POD + podLabel;
    }

    /**
     * 获取回调地址
     *
     * @param businessLabel
     * @return String
     */
    public String getDeploymentCallbackUrl(String businessLabel) {
        return "http://" + BusinessLabelServiceNameEnum.getServiceNameByBusinessLabel(businessLabel) + K8S_CALLBACK_PATH_DEPLOYMENT + businessLabel;
    }


    /**
     * 获取超时时间秒
     *
     * @param timeoutSecond 超时秒数
     * @return Long
     */
    public static Long getTimeoutSecondLong(int timeoutSecond) {
        return Long.valueOf(
                DateUtil.format(
                        DateUtil.offset(
                                new Date(), DateField.SECOND, timeoutSecond
                        ),
                        DatePattern.PURE_DATETIME_PATTERN
                )
        );
    }

    /**
     * 获取当前秒数
     *
     * @return Long
     */
    public static Long getCurrentSecondLong() {
        return Long.valueOf(
                DateUtil.format(
                        new Date(),
                        DatePattern.PURE_DATETIME_PATTERN
                )
        );
    }

}
