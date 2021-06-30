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

package org.dubhe.task.data;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.data.domain.dto.OfRecordTaskDto;
import org.dubhe.data.service.DatasetVersionService;
import org.dubhe.data.service.TaskService;
import org.dubhe.data.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @description ofRecord转换任务完成处理类
 * @date 2020-09-04
 */
@Slf4j
@Component
public class OfRecordQueueExecuteThread implements Runnable {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private TaskService taskService;
    @Autowired
    private DatasetVersionService datasetVersionService;
    @Autowired
    private TaskUtils taskUtils;

    /**
     * ofRecord执行中任务队列
     */
    private static final String OFRECORD_START_QUEUE = "ofrecord_processing_queue";
    /**
     * ofRecord未执行任务队列
     */
    private static final String OFRECORD_PENDING_QUEUE = "ofrecord_task_queue";
    /**
     * ofRecord已完成任务队列
     */
    private static final String OFRECORD_FINISHED_QUEUE = "ofrecord_finished_queue";

    /**
     * 启动ofRecord任务处理线程
     */
    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "ofRecord转换完成任务处理队列");
        thread.start();
    }

    /**
     * ofRecord任务处理线程方法
     */
    @Override
    public void run() {
        while (true) {
            try {
                Object object = redisUtils.lpop(OFRECORD_FINISHED_QUEUE);
                if (ObjectUtil.isNotNull(object)) {
                    String taskId = object.toString();
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(redisUtils.get(taskId)));
                    String detailId = jsonObject.getString("reTaskId");
                    execute(detailId);
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.TEN);
                    redisUtils.del(object.toString());
                } else {
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.THREE_THOUSAND);
                }
            } catch (Exception exception) {
                LogUtil.error(LogEnum.BIZ_DATASET, "get ofrecord finish task failed:{}", exception);
            }
        }
    }

    /**
     * ofRecord任务处理
     *
     * @param detailId ofRecord转换详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void execute(String detailId) {
        LogUtil.info(LogEnum.BIZ_DATASET, detailId);
        OfRecordTaskDto ofRecordTaskDto = JSON.parseObject(JSON.toJSONString(redisUtils.get(detailId)), OfRecordTaskDto.class);
        Boolean flag = taskService.finishTask(ofRecordTaskDto.getId(), ofRecordTaskDto.getFiles().size());
        if (flag) {
            datasetVersionService.update(ofRecordTaskDto.getDatasetVersionId(), 1, 2);
        }
    }

    /**
     * ofrecord任务是否过期
     */
    @Scheduled(cron = "*/15 * * * * ?")
    public void expireOfrecordTask() {
        taskUtils.restartTask(OFRECORD_START_QUEUE, OFRECORD_PENDING_QUEUE);
    }

}
