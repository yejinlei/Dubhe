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

package org.dubhe.image.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.image.domain.dto.*;
import org.dubhe.image.service.PtImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 镜像
 * @date 2020-04-27
 */
@Api(tags = "镜像：镜像管理")
@RestController
@RequestMapping("/ptImage")
public class PtImageController {

    @Autowired
    private PtImageService ptImageService;

    @GetMapping("/info")
    @ApiOperation("查询镜像")
    @PreAuthorize(Permissions.IMAGE)
    public DataResponseBody getImage(PtImageQueryDTO ptImageQueryDTO) {
        return new DataResponseBody(ptImageService.getImage(ptImageQueryDTO));
    }

    @ApiOperation("通过projectName查询镜像")
    @GetMapping
    public DataResponseBody getTagsByImageName(@Validated PtImageQueryImageDTO ptImageQueryImageDTO) {
        return new DataResponseBody(ptImageService.searchImages(ptImageQueryImageDTO));
    }

    @PostMapping("uploadImage")
    @ApiOperation("上传镜像包到harbor")
    @PreAuthorize(Permissions.IMAGE_UPLOAD)
    public DataResponseBody uploadImage(@Validated @RequestBody PtImageUploadDTO ptImageUploadDTO) {
        ptImageService.uploadImage(ptImageUploadDTO);
        return new DataResponseBody();
    }

    @DeleteMapping
    @ApiOperation("删除镜像")
    @PreAuthorize(Permissions.IMAGE_DELETE)
    public DataResponseBody deleteTrainImage(@RequestBody PtImageDeleteDTO ptImageDeleteDTO) {
        ptImageService.deleteTrainImage(ptImageDeleteDTO);
        return new DataResponseBody();
    }

    @PutMapping
    @ApiOperation("修改镜像信息")
    @PreAuthorize(Permissions.IMAGE_EDIT)
    public DataResponseBody updateTrainImage(@Validated @RequestBody PtImageUpdateDTO ptImageUpdateDTO) {
        ptImageService.updateTrainImage(ptImageUpdateDTO);
        return new DataResponseBody();
    }

    @GetMapping("/imageNameList")
    @ApiOperation("获取镜像名称列表")
    public DataResponseBody getImageNameList(@Validated PtImageQueryNameDTO ptImageQueryNameDTO) {
        return new DataResponseBody(ptImageService.getImageNameList(ptImageQueryNameDTO));
    }

    @PutMapping("/imageResource")
    @ApiOperation("修改镜像来源(notebook定制)")
    public DataResponseBody updateImageResource(@RequestParam Long id) {
        ptImageService.updImageResource(id);
        return new DataResponseBody();
    }

    @GetMapping("/imageUrl")
    @ApiOperation("查询镜像url")
    public DataResponseBody<String> getImageUrl(@Validated PtImageQueryUrlDTO ptImageQueryUrlDTO) {
        return new DataResponseBody(ptImageService.getImageUrl(ptImageQueryUrlDTO));
    }

    @GetMapping("/terminalImageList")
    @ApiOperation("获取终端镜像列表")
    public DataResponseBody getTerminalImageList() {
        return new DataResponseBody(ptImageService.getTerminalImageList());
    }
}
