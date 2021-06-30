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
package org.dubhe.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.admin.domain.entity.DictDetail;

import java.io.Serializable;
import java.util.List;

/**
 * @description  字典详情 mapper
 * @date 2020-03-26
 */
public interface DictDetailMapper extends BaseMapper<DictDetail> {
    /**
     * 根据字典ID查找
     *
     * @param dictId
     * @return
     */
    @Select("select * from dict_detail where dict_id =#{dictId} order by sort")
    List<DictDetail> selectByDictId(Serializable dictId);

    /**
     * 根据字典ID和标签查找
     *
     * @param dictId
     * @param label
     * @return
     */
    @Select("select * from dict_detail where dict_id=#{dictId} and label=#{label}")
    DictDetail selectByDictIdAndLabel(Serializable dictId, String label);

    /**
     * 根据字典ID删除
     *
     * @param dictId
     * @return
     */
    @Update("delete from dict_detail where dict_id =#{dictId}")
    int deleteByDictId(Serializable dictId);
}
