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

package org.dubhe.config;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @description 加载配置
 * @date 2020-03-15
 */
@PropertySource(value = {"classpath:config/thirdParty.properties", "classpath:i18n/messages*.properties"})
public final class Resources {
    /**
     * 第三方登录配置
     */
    public static final ResourceBundle THIRDPARTY = ResourceBundle.getBundle("config/thirdParty");
    /**
     * 国际化信息
     */
    private static final Map<String, ResourceBundle> MESSAGES = new HashMap<String, ResourceBundle>();

    /**
     * 国际化信息
     */
    public static String getMessage(String key, Object... params) {
        Locale locale = LocaleContextHolder.getLocale();
        ResourceBundle message = MESSAGES.get(locale.getLanguage());
        if (message == null) {
            synchronized (MESSAGES) {
                message = MESSAGES.get(locale.getLanguage());
                if (message == null) {
                    message = ResourceBundle.getBundle("i18n/messages", locale);
                    MESSAGES.put(locale.getLanguage(), message);
                }
            }
        }
        if (params != null && params.length > 0) {
            return String.format(message.getString(key), params);
        }
        return message.getString(key);
    }

    /**
     * 清除国际化信息
     */
    public static void flushMessage() {
        MESSAGES.clear();
    }
}
