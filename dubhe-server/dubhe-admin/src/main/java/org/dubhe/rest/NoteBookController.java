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

package org.dubhe.rest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.base.DataResponseBody;
import org.dubhe.base.ResponseCode;
import org.dubhe.constant.Permissions;
import org.dubhe.domain.entity.NoteBook;
import org.dubhe.domain.dto.NoteBookListQueryDTO;
import org.dubhe.domain.dto.SourceNoteBookDTO;
import org.dubhe.domain.dto.NoteBookCreateDTO;
import org.dubhe.enums.BizNfsEnum;
import org.dubhe.factory.DataResponseFactory;
import org.dubhe.service.NoteBookService;
import org.dubhe.utils.NotebookUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/{version}/notebook")
public class NoteBookController {

    @Autowired
    private NoteBookService noteBookService;

    @ApiOperation("新增加notebook")
    @PostMapping(value = "/notebooks")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody createNoteBook(@Validated @RequestBody NoteBookCreateDTO noteBookCreateDTO) {
        long userId = NotebookUtil.getCurUserId();
        NoteBook noteBook = new NoteBook();
        BeanUtils.copyProperties(noteBookCreateDTO, noteBook);
        noteBook.setCreateUserId(userId);
        noteBook.setUserId(userId);
        noteBook.setCreateResource(BizNfsEnum.NOTEBOOK.getCreateResource());
        return new DataResponseBody(noteBookService.createNoteBook(noteBook));
    }

    @ApiOperation("查询notebook")
    @GetMapping(value = "/notebooks")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody getNoteBookList(Page page, NoteBookListQueryDTO noteBookListQueryDTO) {
        noteBookListQueryDTO.setUserId(NotebookUtil.getCurUserId());
        return new DataResponseBody(noteBookService.getNoteBookList(page, noteBookListQueryDTO));
    }

    @ApiOperation("异步批量删除notebook")
    @DeleteMapping
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody deleteNoteBook(@RequestBody Set<Long> noteBookIds) {
        List<NoteBook> noteBookList = noteBookService.validateDeleteNoteBook(noteBookIds);
        noteBookService.deleteNoteBooks(noteBookList);
        return new DataResponseBody(ResponseCode.SUCCESS, "正在异步删除NoteBook中..");
    }

    @ApiOperation("启动notebook")
    @PutMapping(value = "/start")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody startNotebook(@RequestParam(required = true) Long noteBookId) {
        String resultInfo = noteBookService.startNoteBook(noteBookId);
        return new DataResponseBody(
                NotebookUtil.validateFailedInfo(resultInfo) ? ResponseCode.ERROR : ResponseCode.SUCCESS
                , resultInfo);
    }

    @ApiOperation("停止notebook")
    @PutMapping(value = "/stop")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody stopNotebook(@RequestParam(required = true) Long noteBookId) {
        String resultInfo = noteBookService.stopNoteBook(noteBookId);
        return new DataResponseBody(
                NotebookUtil.validateFailedInfo(resultInfo) ? ResponseCode.ERROR : ResponseCode.SUCCESS
                , resultInfo);
    }


    @ApiOperation("开启notebook")
    @GetMapping(value = "/{id}")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody openNotebook(@PathVariable(name = "id", required = true) Long noteBookId) {
        return DataResponseFactory.success("获取成功", noteBookService.openNoteBook(noteBookId));
    }


    @ApiOperation("第三方业务创建notebook")
    @PostMapping(value = "/create/{source}")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody createByThirdParty(@PathVariable int source, @Validated @RequestBody SourceNoteBookDTO sourceNoteBookDTO) {
        BizNfsEnum bizNfsEnum = BizNfsEnum.getByCreateResource(source);
        if (bizNfsEnum == null || BizNfsEnum.NOTEBOOK == bizNfsEnum) {
            return new DataResponseBody(ResponseCode.BADREQUEST, "不支持该渠道创建Notebook！");
        }
        sourceNoteBookDTO.setCurUserId(NotebookUtil.getCurUserId());
        return DataResponseFactory.success("操作成功", noteBookService.createNoteBookByThirdParty(bizNfsEnum, sourceNoteBookDTO));
    }

    @ApiOperation("获取编辑地址")
    @GetMapping(value = "/{id}/get-address")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody getAddress(@PathVariable(name = "id", required = true) Long noteBookId) {
        return new DataResponseBody(noteBookService.getAddress(noteBookId));
    }

    @ApiOperation("获取状态")
    @GetMapping(value = "/status")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody getNoteBookStatus() {
        return new DataResponseBody(noteBookService.getNoteBookStatus());
    }


    @ApiOperation("获取正在运行的notebook数量")
    @GetMapping(value = "/run-number")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody getNoteBookRunNumber() {
        return new DataResponseBody(noteBookService.getNoteBookRunNumber(NotebookUtil.getCurUserId()));
    }


    @ApiOperation("获取notebook模板")
    @GetMapping(value = "/notebook-model")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody getNoteBookModel() {
        return new DataResponseBody(noteBookService.getNoteBookModel());
    }


    @ApiOperation("获取notebook详情")
    @PostMapping(value = "/detail")
    @RequiresPermissions(Permissions.DEVELOPMENT_NOTEBOOK)
    public DataResponseBody getNotebookDetail(@RequestBody Set<Long> noteBookIds) {
        return new DataResponseBody(noteBookService.getNotebookDetail(noteBookIds));
    }

}
