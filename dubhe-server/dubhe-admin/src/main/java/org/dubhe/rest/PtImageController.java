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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.annotation.ApiVersion;
import org.dubhe.base.DataResponseBody;
import org.dubhe.constant.Permissions;
import org.dubhe.domain.dto.*;
import org.dubhe.service.PtImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 镜像
 * @date 2020-04-27
 */
@Api(tags = "镜像：镜像管理")
@RestController
@ApiVersion(1)
@RequestMapping("/api/{version}/ptImage")
public class PtImageController {

    @Autowired
    private PtImageService ptImageService;

    @GetMapping("/info")
    @ApiOperation("查询镜像")
    @RequiresPermissions(Permissions.TRAINING_IMAGE)
    public DataResponseBody getImage(PtImageQueryDTO ptImageQueryDTO) {
        return new DataResponseBody(ptImageService.getImage(ptImageQueryDTO));
    }

    @ApiOperation("通过projectName查询镜像")
    @GetMapping
    @RequiresPermissions(Permissions.TRAINING_IMAGE)
    public DataResponseBody getTagsByImageName(@RequestParam Integer projectType, @RequestParam String imageName) {
        return new DataResponseBody(ptImageService.searchImages(projectType, imageName));
    }

    @PostMapping("uploadImage")
    @ApiOperation("上传镜像包到harbor")
    @RequiresPermissions(Permissions.TRAINING_IMAGE)
    public DataResponseBody uploadImage(@Validated @RequestBody PtImageUploadDTO ptImageUploadDTO) {
        ptImageService.uploadImage(ptImageUploadDTO);
        return new DataResponseBody();
    }

    @DeleteMapping
    @ApiOperation("删除镜像")
    @RequiresPermissions(Permissions.TRAINING_IMAGE)
    public DataResponseBody deleteTrainImage(@Validated @RequestBody PtImageDeleteDTO ptImageDeleteDTO) {
        ptImageService.deleteTrainImage(ptImageDeleteDTO);
        return new DataResponseBody();
    }

    @PutMapping
    @ApiOperation("修改镜像信息")
    @RequiresPermissions(Permissions.TRAINING_IMAGE)
    public DataResponseBody updateTrainImage(@Validated @RequestBody PtImageUpdateDTO ptImageUpdateDTO) {
        ptImageService.updateTrainImage(ptImageUpdateDTO);
        return new DataResponseBody();
    }

    @GetMapping("/imageNameList")
    @ApiOperation("获取镜像名称列表")
    @RequiresPermissions(Permissions.TRAINING_IMAGE)
    public DataResponseBody getImageNameList(@RequestParam Integer projectType) {
        return new DataResponseBody(ptImageService.getImageNameList(projectType));
    }

    @PutMapping("/imageResource")
    @ApiOperation("修改镜像来源(notebook定制)")
    @RequiresPermissions(Permissions.TRAINING_IMAGE)
    public DataResponseBody updateImageResource(@RequestParam Long id) {
        ptImageService.updImageResource(id);
        return new DataResponseBody();
    }

    @GetMapping("/imageUrl")
    @ApiOperation("查询镜像url")
    @RequiresPermissions(Permissions.TRAINING_IMAGE)
    public DataResponseBody getImageUrl(PtImageQueryUrlDTO ptImageQueryUrlDTO) {
        return new DataResponseBody(ptImageService.getImageUrl(ptImageQueryUrlDTO));
    }
}
