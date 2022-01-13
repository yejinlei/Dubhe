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
package org.dubhe.tadl.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.domain.dto.TrialDTO;
import org.dubhe.tadl.service.TadlTrialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description
 * @date 2020-12-28
 */
@Api(tags = "TADL开发：TADL")
@RestController
@RequestMapping(TadlConstant.MODULE_URL_PREFIX + "/trial")
public class TadlTrialController {

    @Autowired
    private TadlTrialService tadlTrialService;

    @ApiOperation(value = "trial列表查询")
    @GetMapping("/{experimentId}/{stageOrder}/list")
    public DataResponseBody query(TrialDTO trialDTO) {
        return new DataResponseBody(tadlTrialService.listVO(trialDTO));
    }

    @ApiOperation(value = "获取trial下pod信息")
    @GetMapping("/pod/{trialId}")
    public DataResponseBody getTrailLog(Long trialId){
        return new DataResponseBody(tadlTrialService.getPods(trialId));
    }
}
