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

package org.dubhe.data.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.domain.dto.AnnotationDeleteDTO;
import org.dubhe.data.domain.dto.AnnotationInfoCreateDTO;
import org.dubhe.data.domain.dto.AutoAnnotationCreateDTO;
import org.dubhe.data.domain.dto.BatchAnnotationInfoCreateDTO;
import org.dubhe.data.service.AnnotationService;
import org.dubhe.data.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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
    @PostMapping(value = "/{datasetId}/{fileId}/annotations")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody save(@PathVariable(value = "fileId") Long fileId,
                                 @PathVariable(value = "datasetId") Long datasetId,
                                 @Validated @RequestBody AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        annotationService.save(fileId,datasetId, annotationInfoCreateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标注保存", notes = "状态直接转为完成，用于分类的批量保存")
    @PostMapping(value = "/{datasetId}/annotations")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody save(@Validated @RequestBody BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO,@PathVariable(value = "datasetId") Long datasetId) {
        annotationService.save(datasetId,batchAnnotationInfoCreateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标注完成")
    @PostMapping(value = "/{datasetId}/{fileId}/annotations/finish")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody finish(@PathVariable(value = "fileId") Long fileId,
                                   @RequestBody AnnotationInfoCreateDTO annotationInfoCreateDTO,@PathVariable(value = "datasetId") Long datasetId) {
        annotationService.finishManual(fileId,datasetId,annotationInfoCreateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "重新标注", notes = "删除文件或数据集下所有文件的标注，并且重新标注，自动标注中的数据集下的文件不允许清除")
    @DeleteMapping(value = "/annotations")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody reAuto(@Validated @RequestBody AnnotationDeleteDTO annotationDeleteDTO) {
        annotationService.reAuto(annotationDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "自动标注，只对未标注状态的文件进行标注")
    @PostMapping(value = "/annotations/auto")
    @PreAuthorize(Permissions.DATA)
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



    @ApiOperation(value = "目标跟踪")
    @GetMapping(value = "/annotations/auto/track/{datasetId}")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody track(@PathVariable(value = "datasetId") Long datasetId) {
        annotationService.track(datasetId);
        return new DataResponseBody();
    }

}