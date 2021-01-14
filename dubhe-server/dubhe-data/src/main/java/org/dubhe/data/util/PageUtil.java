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

package org.dubhe.data.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @description 分页工具
 * @date 2020-04-10
 */
public class PageUtil {

    /**
     * 构建分页
     *
     * @param current 当前页码
     * @param size    总页数
     * @param <T>     当前页数据
     * @return        Page
     */
    public static <T> Page<T> build(Integer current, Integer size) {
        Page<T> page = new Page<>();
        if (current != null) {
            page.setCurrent(current);
        }
        if (size != null) {
            page.setSize(size);
        }
        return page;
    }

}
