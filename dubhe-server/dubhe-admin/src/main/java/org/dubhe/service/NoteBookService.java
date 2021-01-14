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

package org.dubhe.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.domain.entity.NoteBook;
import org.dubhe.domain.entity.NoteBookModel;
import org.dubhe.domain.dto.*;
import org.dubhe.domain.vo.NoteBookVO;
import org.dubhe.enums.BizNfsEnum;
import org.dubhe.enums.NoteBookStatusEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description notebook服务接口
 * @date 2020-04-28
 */
public interface NoteBookService {
    /**
     * 分页查询所有notebook记录
     *
     * @param page
     * @param noteBookListQueryDTO
     * @return Map<String, Object>
     */
    Map<String, Object> getNoteBookList(Page page, NoteBookListQueryDTO noteBookListQueryDTO);

    /**
     * 查询所有notebook记录
     *
     * @param page
     * @param noteBookQueryDTO
     * @return List<NoteBook>
     */
    List<NoteBook> getList(Page page, NoteBookQueryDTO noteBookQueryDTO);

    /**
     * 新增加notebook
     *
     * @param noteBook
     * @return NoteBookVO
     */
    NoteBookVO createNoteBook(NoteBook noteBook);

    /**
     * 验证notebook是否可删除
     *
     * @param noteBookIds
     * @return List<NoteBook>
     */
    List<NoteBook> validateDeleteNoteBook(Set<Long> noteBookIds);

    /**
     * 删除notebook异步方法
     *
     * @param noteBookList
     */
    void deleteNoteBooks(List<NoteBook> noteBookList);


    /**
     * 启动notebook
     *
     * @param noteBookId
     * @return String
     */
    String startNoteBook(Long noteBookId);

    /**
     * 停止notebook
     *
     * @param noteBookId
     * @return String
     */
    String stopNoteBook(Long noteBookId);

    /**
     * update by ID
     *
     * @param noteBook
     * @return noteBook
     */
    NoteBook updateById(NoteBook noteBook);

    /**
     * 打开notebook
     *
     * @param noteBookId
     * @return String
     */
    String openNoteBook(Long noteBookId);

    /**
     * 获取notebook可访问URL
     *
     * @param noteBook
     * @return String
     */
    String getJupyterUrl(NoteBook noteBook);

    /**
     * 查询notebook在K8s对应的状态
     *
     * @param noteBook
     * @return NoteBookStatusEnum
     */
    NoteBookStatusEnum getStatus(NoteBook noteBook);

    /**
     * 第三方创建notebook
     *
     * @param bizNfsEnum
     * @param sourceNoteBookDTO
     * @return NoteBookDTO
     */
    NoteBookVO createNoteBookByThirdParty(BizNfsEnum bizNfsEnum, SourceNoteBookDTO sourceNoteBookDTO);

    /**
     * 获取编辑地址
     *
     * @param noteBookId
     * @return String
     */
    String getAddress(Long noteBookId);

    /**
     * 删除PVC
     *
     * @param noteBook
     * @return String
     */
    String deletePvc(NoteBook noteBook);

    /**
     * 获取状态编码
     *
     * @return List<NoteBookStatusDTO>
     */
    List<NoteBookStatusDTO> getNoteBookStatus();

    /**
     * 获取正在运行的notebook数量
     *
     * @return int
     */
    int getNoteBookRunNumber();

    /**
     * 获取notebook模板
     *
     * @return Map<String, List < NoteBookModel>>
     */
    Map<String, List<NoteBookModel>> getNoteBookModel();

    /**
     * 刷新notebook对象状态
     *
     * @param statusEnum
     * @param noteBook
     * @return true 无需更新
     */
    boolean refreshNoteBookStatus(NoteBookStatusEnum statusEnum, NoteBook noteBook);

    /**
     * 根据notebook id更新训练id
     *
     * @param noteBookId
     * @param algorithmId
     * @return boolean
     */
    boolean updateTrainIdByNoteBookId(Long noteBookId, Long algorithmId);

    /**
     * 根据ID查询notebook详情
     *
     * @param noteBookIds
     * @return List<NoteBookDTO>
     */
    List<NoteBookVO> getNotebookDetail(Set<Long> noteBookIds);

    /**
     * 获取已经运行却没有URL的notebook
     *
     * @param page
     * @return List<NoteBook>
     */
    List<NoteBook> getRunNotUrlList(Page page);
}
