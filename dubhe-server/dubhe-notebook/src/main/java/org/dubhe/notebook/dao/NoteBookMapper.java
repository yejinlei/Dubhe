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

package org.dubhe.notebook.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.notebook.domain.entity.NoteBook;

import java.util.Collection;
import java.util.List;


/**
 * @description notebook mapper
 * @date 2020-04-28
 */
@DataPermission(ignoresMethod = {"insert","findByNamespaceAndResourceName","selectRunNotUrlList"})
public interface NoteBookMapper extends BaseMapper<NoteBook> {



    /**
     * 查询正在运行的notebook数量
     *
     * @param status
     * @return int
     */
    @Select("select count(1) from notebook where status = #{status} and deleted = 0")
    int selectRunNoteBookNum( @Param("status") Integer status);

    /**
     * 查询正在运行的notebook列表
     *
     * @return
     */
    @Select("select * from notebook where status = 0 and deleted = 0 and (url is not null or url != '')")
    List<NoteBook> selectRunningList();

    /**
     * 根据namespace + resourceName查询
     *
     * @param namespace
     * @param resourceName
     * @return NoteBook
     */
    @Select("select * from notebook where k8s_namespace = #{namespace} and k8s_resource_name = #{resourceName} and deleted = 0 limit 1")
    NoteBook findByNamespaceAndResourceName(@Param("namespace") String namespace, @Param("resourceName") String resourceName);

    /**
     * 查询已经运行并且没有URL的notebook
     *
     * @param page
     * @param status
     * @return List<NoteBook>
     */
    @Select("select * from notebook where deleted = 0 and status = #{status} and (url is null or url = '')")
    List<NoteBook> selectRunNotUrlList(@Param("page") Page page, @Param("status") Integer status);


    @Update({
            "<script>",
                "update notebook ",
                "set algorithm_id = #{algorithmId} ",
                "where id in ",
                "<foreach item='item' collection='notebookIds' open='(' separator=',' close=')'>",
                    "#{item}",
                "</foreach>",
            "</script>"
    })
    int updateNoteBookAlgorithm(@Param("notebookIds")Collection<Long> notebookIds, @Param("algorithmId")Long algorithmId);

    @Select({
            "<script>",
                "select id from notebook",
                "where algorithm_id in ",
                "<foreach item='item' collection='algorithmIds' open='(' separator=',' close=')'>",
                    "#{item}",
                "</foreach>",
            "</script>"
    })
    List<Long> getNoteBookIdByAlgorithm(@Param("algorithmIds")Collection<Long> algorithmIds);
}
