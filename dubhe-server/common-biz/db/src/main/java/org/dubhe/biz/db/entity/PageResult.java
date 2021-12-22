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


package org.dubhe.biz.db.entity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * @description  Entity基础类
 * @date 2021-10-12
 */
@Data
public class PageResult<T> {

    private List<T> result;
    Page page;

    public PageResult(IPage iPage, List<T> data) {
        page = new Page();
        page.current = iPage.getCurrent();
        page.size = iPage.getSize();
        page.total = iPage.getTotal();
        result = data;
    }

    @Data
    class Page {
        private long current;
        private long size;
        private long total;
    }
}
