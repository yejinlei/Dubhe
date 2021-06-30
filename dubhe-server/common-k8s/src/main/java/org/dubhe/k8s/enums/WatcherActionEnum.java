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

package org.dubhe.k8s.enums;

import org.dubhe.biz.base.utils.StringUtils;

/**
 * @description k8s event watcher action enum
 * @date 2020-06-02
 */
public enum WatcherActionEnum {
    /**
     * 添加
     */
    ADDED("ADDED"),
    /**
     * 修改
     */
    MODIFIED("MODIFIED"),
    /**
     * 删除
     */
    DELETED("DELETED"),
    /**
     * 错误
     */
    ERROR("ERROR"),
    ;

    private String action;

    WatcherActionEnum(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public static WatcherActionEnum get(String action) {
        for (WatcherActionEnum watcherActionEnum : WatcherActionEnum.values()) {
            if (StringUtils.equals(action, watcherActionEnum.getAction())) {
                return watcherActionEnum;
            }
        }
        return null;
    }
}
