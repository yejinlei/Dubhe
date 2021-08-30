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

package org.dubhe.notebook.convert;


import org.dubhe.biz.base.vo.NoteBookVO;
import org.dubhe.biz.db.base.BaseConvert;
import org.dubhe.notebook.domain.entity.NoteBook;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @description notebook 转化器
 * @create 2020-04-28
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoteBookConvert extends BaseConvert<NoteBookVO, NoteBook> {
}
