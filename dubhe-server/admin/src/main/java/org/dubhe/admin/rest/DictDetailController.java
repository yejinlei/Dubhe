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
package org.dubhe.admin.rest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.domain.dto.DictDetailCreateDTO;
import org.dubhe.admin.domain.dto.DictDetailDeleteDTO;
import org.dubhe.admin.domain.dto.DictDetailQueryDTO;
import org.dubhe.admin.domain.dto.DictDetailUpdateDTO;
import org.dubhe.admin.service.DictDetailService;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.dto.DictDetailQueryByLabelNameDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.DictDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @description 字典详情管理 控制器
 * @date 2020-06-01
 */
@Api(tags = "系统：字典详情管理")
@RestController
@RequestMapping("/dictDetail")
public class DictDetailController {

    @Autowired
    private DictDetailService dictDetailService;

    @ApiOperation("查询字典详情")
    @GetMapping
    @PreAuthorize(Permissions.DICT)
    public DataResponseBody getDictDetails(DictDetailQueryDTO resources, Page page) {
        return new DataResponseBody(dictDetailService.queryAll(resources, page));
    }


    @ApiOperation("新增字典详情")
    @PostMapping
    @PreAuthorize(Permissions.DICT_DETAIL_CREATE)
    public DataResponseBody create(@Valid @RequestBody DictDetailCreateDTO resources) {
        return new DataResponseBody(dictDetailService.create(resources));
    }


    @ApiOperation("修改字典详情")
    @PutMapping
    @PreAuthorize(Permissions.DICT_DETAIL_EDIT)
    public DataResponseBody update(@Valid @RequestBody DictDetailUpdateDTO resources) {
        dictDetailService.update(resources);
        return new DataResponseBody();
    }

    @ApiOperation("删除字典详情")
    @DeleteMapping
    @PreAuthorize(Permissions.DICT_DETAIL_DELETE)
    public DataResponseBody delete(@Valid @RequestBody DictDetailDeleteDTO dto) {
        dictDetailService.delete(dto.getIds());
        return new DataResponseBody();
    }

    @ApiOperation("根据名称查询字典详情")
    @GetMapping("/getDictDetails")
    public DataResponseBody<List<DictDetailVO>> findDictDetailByName(@Validated DictDetailQueryByLabelNameDTO dictDetailQueryByLabelNameDTO) {
        return new DataResponseBody(dictDetailService.getDictName(dictDetailQueryByLabelNameDTO));
    }

}
