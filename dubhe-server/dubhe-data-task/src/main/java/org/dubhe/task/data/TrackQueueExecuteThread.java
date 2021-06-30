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
import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.data.domain.dto.AutoTrackCreateDTO;
import org.dubhe.data.domain.entity.Task;
import org.dubhe.data.machine.constant.DataStateMachineConstant;
import org.dubhe.data.machine.utils.StateMachineUtil;
import org.dubhe.data.service.AnnotationService;
import org.dubhe.data.service.TaskService;
import org.dubhe.data.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @description 跟踪任务完成队列处理类
 * @date 2020-09-08
 */
@Slf4j
@Component
public class TrackQueueExecuteThread implements Runnable {

    @Autowired
    private AnnotationService annotationService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskUtils taskUtils;

    /**
     * 跟踪算法执行中任务队列
     */
    private static final String TRACK_START_QUEUE = "track_processing_queue";
    /**
     * 跟踪算法未执行任务队列
     */
    private static final String TRACK_PENDING_QUEUE = "track_task_queue";
    /**
     * 跟踪算法已完成任务队列
     */
    private static final String TRACK_FINISHED_QUEUE = "track_finished_queue";

    /**
     * 跟踪算法失败任务队列
     */
    private static final String TRACK_FAILED_QUQUE = "track_failed_queue";

    /**
     * 启动标注任务处理线程
     */
    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "目标跟踪完成任务处理队列");
        thread.start();
    }

    /**
     * 标注任务处理线程方法
     */
    @Override
    public void run() {
        while (true) {
            try {
                Object object = redisUtils.lpop(TRACK_FINISHED_QUEUE);
                if (ObjectUtil.isNotNull(object)) {
                    String taskId = object.toString();
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(redisUtils.get(taskId)));
                    Long id = jsonObject.getLong("id");
                    Task task = taskService.detail(id);
                    Boolean flag = taskService.finishTask(id, 1);
                    if (flag) {
                        AutoTrackCreateDTO autoTrackCreateDTO = new AutoTrackCreateDTO();
                        autoTrackCreateDTO.setCode(MagicNumConstant.TWO_HUNDRED);
                        autoTrackCreateDTO.setData(null);
                        autoTrackCreateDTO.setMsg("success");
                        annotationService.finishAutoTrack(task.getDatasetId(), autoTrackCreateDTO);
                    }
                    redisUtils.del(taskId);
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.TEN);
                } else {
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.THREE_THOUSAND);
                }
                Object failedTask = redisUtils.lpop(TRACK_FAILED_QUQUE);
                if (ObjectUtil.isNotNull(failedTask)) {
                    String failedId = failedTask.toString();
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(redisUtils.get(failedId)));
                    Long id = jsonObject.getLong("id");
                    Task task = taskService.detail(id);
                    Boolean flag = taskService.finishTask(id, MagicNumConstant.ONE);
                    if (flag) {
                        //嵌入状态机（目标跟踪中—>目标跟踪失败）
                        StateMachineUtil.stateChange(new StateChangeDTO() {{
                            setObjectParam(new Object[]{task.getDatasetId()});
                            setEventMethodName(DataStateMachineConstant.DATA_AUTO_TRACK_FAIL_EVENT);
                            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
                        }});
                        redisUtils.del(failedId);
                    }
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.TEN);
                } else {
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.THREE_THOUSAND);
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "get track finish task failed:{}", e);
            }
        }
    }

    /**
     * 跟踪任务是否过期
     */
    @Scheduled(cron = "*/15 * * * * ?")
    public void expireTrackTask() {
        taskUtils.restartTask(TRACK_START_QUEUE, TRACK_PENDING_QUEUE);
    }

}
