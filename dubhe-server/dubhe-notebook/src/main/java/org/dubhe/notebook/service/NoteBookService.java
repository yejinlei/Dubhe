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

package org.dubhe.notebook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.base.dto.NoteBookAlgorithmQueryDTO;
import org.dubhe.biz.base.dto.NoteBookAlgorithmUpdateDTO;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.notebook.domain.dto.*;
import org.dubhe.notebook.domain.entity.NoteBook;
import org.dubhe.notebook.enums.NoteBookStatusEnum;
import org.dubhe.biz.base.vo.NoteBookVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description notebook服务接口
 * @date 2020-04-28
 */
public interface NoteBookService {
    /**
     * 分页查询所有 notebook 记录
     *
     * @param page 分页参数
     * @param noteBookListQueryDTO 查询参数
     * @return Map<String, Object> 分页查询结果
     */
    Map<String, Object> getNoteBookList(Page page, NoteBookListQueryDTO noteBookListQueryDTO);

    /**
     * 查询所有notebook记录
     *
     * @param page 分页参数
     * @param noteBookStatusEnums notebook状态枚举
     * @return notebook集合
     */
    List<NoteBook> getList(Page page, NoteBookStatusEnum... noteBookStatusEnums);

    /**
     * 新增加 notebook
     *
     * @param createDTO notebook创建参数
     * @return NoteBookVO notebook vo对象
     */
    NoteBookVO createNoteBook(NoteBookCreateDTO createDTO);

    /**
     * 验证 notebook 是否可删除
     *
     * @param noteBookIds notebook id 集合
     * @return List<NoteBook> 被删除的notebook集合
     */
    List<NoteBook> validateDeletableNoteBook(Set<Long> noteBookIds);

    /**
     * 批量删除notebook
     *
     * @param noteBookIds notebook id 集合
     */
    void deleteNoteBooks(Set<Long> noteBookIds);


    /**
     * 启动notebook
     *
     * @param noteBookId notebook id
     * @return String 删除结果提示
     */
    String startNoteBook(Long noteBookId);

    /**
     * 停止notebook
     *
     * @param noteBookId notebook id
     * @return String 停止notebook结果提示语
     */
    String stopNoteBook(Long noteBookId);

    /**
     * 一键停止所有notebook
     *
     * @return
     */
    void batchStopNoteBooks();

    /**
     * 更新notebook
     *
     * @param noteBook 即将更新的notebook
     * @return NoteBook 更新后的notebook
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
     * 获取jupyter 地址
     *
     * @param noteBook notebook
     * @return String jupyter地址
     */
    String getJupyterUrl(NoteBook noteBook);

    /**
     * 获取notebook状态
     *
     * @param noteBook notebook
     * @return NoteBookStatusEnum notebook状态
     */
    NoteBookStatusEnum getStatus(NoteBook noteBook);

    /**
     * 第三方创建notebook
     *
     * @param bizPathEnum 业务路径枚举
     * @param sourceNoteBookDTO 第三方创建NoteBook请求对象
     * @return NoteBookVO notebook返前端数据
     */
    NoteBookVO createNoteBookByThirdParty(BizPathEnum bizPathEnum, SourceNoteBookDTO sourceNoteBookDTO);

    /**
     * 获取地址
     *
     * @param noteBookId notebook id
     * @return String url地址
     */
    String getAddress(Long noteBookId);

    /**
     * 获取正在运行的notebook数量
     *
     * @return int notebook数量
     */
    int getNoteBookRunNumber();

    /**
     * 刷新notebook状态
     *
     * @param statusEnum notebook 状态枚举
     * @param noteBook notebook
     * @param processNotebookCommand 处理notebook生命周期的回调函数
     * @return boolean true 刷新成功 false 刷新失败
     */
    boolean refreshNoteBookStatus(NoteBookStatusEnum statusEnum, NoteBook noteBook, ProcessNotebookCommand processNotebookCommand);

    /**
     * 刷新notebook对象状态
     *
     * @param statusEnum
     * @param noteBook
     * @return true 已处理 false不予处理
     */
    boolean refreshNoteBookStatus(NoteBookStatusEnum statusEnum, NoteBook noteBook);

    /**
     * 获取notebook详情
     *
     * @param noteBookIds notebook id 集合
     * @return List<NoteBookVO> notebook vo 集合
     */
    List<NoteBookVO> getNotebookDetail(Set<Long> noteBookIds);

    /**
     * 获取notebook详情
     *
     * @param noteBookId notebook id 集合
     * @return NoteBookVO notebook 详情
     */
    NoteBookVO getNotebookDetail(Long noteBookId);

    /**
     * 获取正在运行却没有URL的notebook
     *
     * @param page 分页信息
     * @return List<NoteBook> notebook集合
     */
    List<NoteBook> getRunNotUrlList(Page page);

    /**
     * 修改notebook算法ID
     *
     * @param noteBookAlgorithmListQueryDTO 算法更新notebook对象
     * @return 更新notebook数量
     */
    int updateNoteBookAlgorithm(NoteBookAlgorithmUpdateDTO noteBookAlgorithmListQueryDTO);

    /**
     * 根据算法ID查询notebook Id
     *
     * @param noteBookAlgorithmQueryDTO 算法查询notebook对象
     * @return notebook id集合
     */
    List<Long> getNoteBookIdByAlgorithm(NoteBookAlgorithmQueryDTO noteBookAlgorithmQueryDTO);
}
