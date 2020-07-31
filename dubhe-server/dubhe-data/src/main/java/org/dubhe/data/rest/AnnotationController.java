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

package org.dubhe.data.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.base.DataResponseBody;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.service.AnnotationService;
import org.dubhe.data.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.dubhe.constant.Permissions.DATA;

/**
 * @description 标注管理
 * @date 2020-04-10
 */
@Api(tags = "数据处理：标注")
@RestController
@RequestMapping(Constant.MODULE_URL_PREFIX + "/datasets/files")
public class AnnotationController {

    /**
     * 标注服务实现类
     */
    @Autowired
    private AnnotationService annotationService;

    /**
     * 任务服务实现类
     */
    @Autowired
    private TaskService taskService;

    @ApiOperation(value = "标注保存")
    @PostMapping(value = "/{fileId}/annotations")
    @RequiresPermissions(DATA)
    public DataResponseBody save(@PathVariable(value = "fileId") Long fileId,
                                 @Validated @RequestBody AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        return new DataResponseBody(annotationService.save(fileId, annotationInfoCreateDTO));
    }

    @ApiOperation(value = "标注保存", notes = "状态直接转为完成，用于分类的批量保存")
    @PostMapping(value = "/annotations")
    @RequiresPermissions(DATA)
    public DataResponseBody save(@Validated @RequestBody BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO) {
        return new DataResponseBody(annotationService.save(batchAnnotationInfoCreateDTO));
    }

    @ApiOperation(value = "标注完成")
    @PostMapping(value = "/{fileId}/annotations/finish")
    @RequiresPermissions(DATA)
    public DataResponseBody finish(@PathVariable(value = "fileId") Long fileId,
                                   @RequestBody AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        return new DataResponseBody(annotationService.finishManual(fileId, annotationInfoCreateDTO));
    }

    @ApiOperation(value = "标注清除", notes = "删除文件或数据集下所有文件的标注，自动标注中的数据集下的文件不允许清除")
    @DeleteMapping(value = "/annotations")
    @RequiresPermissions(DATA)
    public DataResponseBody delete(@Validated @RequestBody AnnotationDeleteDTO annotationDeleteDTO) {
        annotationService.delete(annotationDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "自动标注，只对未标注状态的文件进行标注")
    @PostMapping(value = "/annotations/auto")
    @RequiresPermissions(DATA)
    public DataResponseBody auto(@Validated @RequestBody AutoAnnotationCreateDTO autoAnnotationCreateDTO) {
        return new DataResponseBody(taskService.auto(autoAnnotationCreateDTO));
    }

    @ApiOperation(value = "自动标注完成")
    @PostMapping(value = "/annotations/auto/{taskId}")
    public DataResponseBody finishAuto(@PathVariable(value = "taskId") String taskId,
                                       @Validated @RequestBody BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO) {
        return new DataResponseBody(annotationService.finishAuto(taskId, batchAnnotationInfoCreateDTO));
    }

    @ApiOperation(value = "自动标注任务")
    @GetMapping(value = "/annotations/auto/tasks")
    public DataResponseBody getTaskPool() {
        return new DataResponseBody(annotationService.getTaskPool());
    }

    @ApiOperation(value = "自动目标追踪完成")
    @PostMapping(value = "/annotations/auto/track/{datasetId}")
    public DataResponseBody finishAutoTrack(@PathVariable(value = "datasetId") Long datasetId, @Validated @RequestBody AutoTrackCreateDTO autoTrackCreateDTO) {
        annotationService.finishAutoTrack(datasetId, autoTrackCreateDTO);
        return new DataResponseBody();
    }

}