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

package org.dubhe.notebook.rest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.dto.NoteBookAlgorithmQueryDTO;
import org.dubhe.biz.base.dto.NoteBookAlgorithmUpdateDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.notebook.domain.dto.NoteBookCreateDTO;
import org.dubhe.notebook.domain.dto.NoteBookListQueryDTO;
import org.dubhe.notebook.domain.dto.SourceNoteBookDTO;
import org.dubhe.notebook.service.NoteBookService;
import org.dubhe.notebook.utils.NotebookUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @description notebook controller
 * @date 2020-04-27
 */
@Api(tags = "模型开发：Notebook")
@RestController
@RequestMapping("/notebooks")
public class NoteBookController {

    @Autowired
    private NoteBookService noteBookService;

    @ApiOperation("新增加notebook")
    @PostMapping
    @PreAuthorize(Permissions.NOTEBOOK_CREATE)
    public DataResponseBody createNoteBook(@Validated @RequestBody NoteBookCreateDTO noteBookCreateDTO) {

        return new DataResponseBody(noteBookService.createNoteBook(noteBookCreateDTO));
    }

    @ApiOperation("查询notebook")
    @GetMapping
    @PreAuthorize(Permissions.NOTEBOOK)
    public DataResponseBody getNoteBookList(Page page, NoteBookListQueryDTO noteBookListQueryDTO) {
        return new DataResponseBody(noteBookService.getNoteBookList(page, noteBookListQueryDTO));
    }

    @ApiOperation("根据id查询notebook")
    @GetMapping("/detail/{id}")
    @PreAuthorize(Permissions.NOTEBOOK)
    public DataResponseBody getNoteBook(@PathVariable Long id) {
        return  DataResponseFactory.success(noteBookService.getNotebookDetail(id));
    }

    @ApiOperation("修改notebook算法ID")
    @PutMapping(value = "/algorithm")
    @PreAuthorize(Permissions.NOTEBOOK_UPDATE)
    public DataResponseBody updateNoteBookAlgorithm(@Validated @RequestBody NoteBookAlgorithmUpdateDTO noteBookAlgorithmUpdateDTO) {
        return DataResponseFactory.success(noteBookService.updateNoteBookAlgorithm(noteBookAlgorithmUpdateDTO));
    }

    @ApiOperation("根据算法ID查询NotebookID")
    @GetMapping(value = "/algorithm")
    @PreAuthorize(Permissions.NOTEBOOK)
    public DataResponseBody<List<Long>> getNoteBookIdByAlgorithm(@Validated NoteBookAlgorithmQueryDTO noteBookAlgorithmQueryDTO) {
        return DataResponseFactory.success(noteBookService.getNoteBookIdByAlgorithm(noteBookAlgorithmQueryDTO));
    }

    @ApiOperation("异步批量删除notebook")
    @DeleteMapping
    @PreAuthorize(Permissions.NOTEBOOK_DELETE)
    public DataResponseBody deleteNoteBook(@RequestBody Set<Long> noteBookIds) {

        noteBookService.deleteNoteBooks( noteBookIds);
        return new DataResponseBody(ResponseCode.SUCCESS, "正在异步删除NoteBook中..");
    }

    @ApiOperation("启动notebook")
    @PutMapping(value = "/start")
    @PreAuthorize(Permissions.NOTEBOOK_START)
    public DataResponseBody startNotebook(@RequestParam Long noteBookId) {
        String resultInfo = noteBookService.startNoteBook(noteBookId);
        return new DataResponseBody(
                NotebookUtil.validateFailedInfo(resultInfo) ? ResponseCode.ERROR : ResponseCode.SUCCESS
                , resultInfo);
    }

    @ApiOperation("停止notebook")
    @PutMapping(value = "/stop")
    @PreAuthorize(Permissions.NOTEBOOK_STOP)
    public DataResponseBody stopNotebook(@RequestParam Long noteBookId) {
        String resultInfo = noteBookService.stopNoteBook(noteBookId);
        return new DataResponseBody(
                NotebookUtil.validateFailedInfo(resultInfo) ? ResponseCode.ERROR : ResponseCode.SUCCESS
                , resultInfo);
    }

    @ApiOperation("一键停止所有notebook")
    @PutMapping(value = "/batchStop")
    @PreAuthorize(Permissions.NOTEBOOK_STOP)
    public DataResponseBody batchStopNotebook() {
        noteBookService.batchStopNoteBooks();
        return new DataResponseBody();
    }

    @ApiOperation("打开notebook")
    @GetMapping(value = "/{id}")
    @PreAuthorize(Permissions.NOTEBOOK_OPEN)
    public DataResponseBody openNotebook(@PathVariable(name = "id") Long noteBookId) {
        return DataResponseFactory.success("获取成功", noteBookService.openNoteBook(noteBookId));
    }


    @ApiOperation("第三方业务创建notebook")
    @PostMapping(value = "/create/{source}")
    @PreAuthorize(Permissions.NOTEBOOK_CREATE)
    public DataResponseBody createByThirdParty(@PathVariable int source, @Validated @RequestBody SourceNoteBookDTO sourceNoteBookDTO) {
        BizPathEnum bizPathEnum = BizPathEnum.getByCreateResource(source);
        if (bizPathEnum == null || BizPathEnum.NOTEBOOK == bizPathEnum) {
            return new DataResponseBody(ResponseCode.BADREQUEST, "不支持该渠道创建Notebook！");
        }
        return DataResponseFactory.success("操作成功", noteBookService.createNoteBookByThirdParty(bizPathEnum, sourceNoteBookDTO));
    }

    @ApiOperation("获取编辑地址")
    @GetMapping(value = "/{id}/get-address")
    @PreAuthorize(Permissions.NOTEBOOK)
    public DataResponseBody getAddress(@PathVariable(name = "id") Long noteBookId) {
        return new DataResponseBody(noteBookService.getAddress(noteBookId));
    }

    @ApiOperation("获取正在运行的notebook数量")
    @GetMapping(value = "/run-number")
    @PreAuthorize(Permissions.NOTEBOOK)
    public DataResponseBody getNoteBookRunNumber() {
        return new DataResponseBody(noteBookService.getNoteBookRunNumber());
    }


    @ApiOperation("获取notebook详情")
    @PostMapping(value = "/detail")
    @PreAuthorize(Permissions.NOTEBOOK)
    public DataResponseBody getNotebookDetail(@RequestBody Set<Long> noteBookIds) {
        return new DataResponseBody(noteBookService.getNotebookDetail(noteBookIds));
    }

}
