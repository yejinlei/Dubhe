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

package org.dubhe.rest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.dubhe.base.DataResponseBody;
import org.dubhe.harbor.api.HarborApi;
import org.dubhe.service.HarborProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * @description Harbor镜像 控制器
 *
 * @create 2020/5/25
 */
@Api(tags = "系统：Harbor镜像")
@RestController
@RequestMapping("/api/{version}/harbor")
public class HarborController {

    @Autowired
    private HarborApi harborApi;

    @Autowired
    private HarborProjectService harborProjectService;


    @ApiOperation("查询Project")
    @GetMapping(value = "/projects/{source}")
    public DataResponseBody getProjectList(@ApiParam(value = "0 - NOTEBOOK模型管理  1- ALGORITHM算法管理") @PathVariable int source) {
        return new DataResponseBody(harborProjectService.getHarborProjects(source));
    }

    @ApiOperation("查询镜像")
    @GetMapping(value = "/images")
    public DataResponseBody getImageList(@ApiParam(value = "项目名称",required = true)@RequestParam(required = true) String project) {
        return new DataResponseBody(harborApi.searchImageNames(Arrays.asList(project)));
    }

    @ApiOperation("分页查询所有镜像")
    @GetMapping("/image_page")
    public DataResponseBody findImagePage(Page page){
        return new DataResponseBody(harborApi.findImagePage(page));
    }
}
