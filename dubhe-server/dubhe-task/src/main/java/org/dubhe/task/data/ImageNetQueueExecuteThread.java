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

package org.dubhe.task.data;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.AnnotationInfoCreateDTO;
import org.dubhe.data.domain.dto.BatchAnnotationInfoCreateDTO;
import org.dubhe.data.service.AnnotationService;
import org.dubhe.data.util.TaskUtils;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description imageNet标注任务完成队列处理类
 * @date 2020-09-01
 */
@Slf4j
@Component
public class ImageNetQueueExecuteThread implements Runnable {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private AnnotationService annotationService;
    @Autowired
    private TaskUtils taskUtils;

    /**
     * imageNet执行中任务队列
     */
    private static final String IMAGENET_START_QUEUE = "imagenet_processing_queue";
    /**
     * imageNet未执行任务队列
     */
    private static final String IMAGENET_PENDING_QUEUE = "imagenet_task_queue";
    /**
     * 标注算法已完成任务队列
     */
    private static final String IMAGENET_FINISHED_QUEUE = "imagenet_finished_queue";
    /**
     * 启动imageNet任务处理线程
     */
    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "ImageNet标注完成任务处理队列");
        thread.start();
    }

    /**
     * imageNet任务处理线程方法
     */
    @Override
    public void run() {
        while (true) {
            try {
                Object object = redisUtils.lpop(IMAGENET_FINISHED_QUEUE);
                if (ObjectUtil.isNotNull(object)) {
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(object));
                    String task = jsonObject.getString("task");
                    Object taskDetail = redisUtils.get(task.replaceAll("\"",""));
                    TaskSplitBO taskSplitBO = JSON.parseObject(JSON.toJSONString(taskDetail), TaskSplitBO.class);
                    JSONArray jsonArray = jsonObject.getJSONArray("annotations");
                    List<AnnotationInfoCreateDTO> list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        list.add(JSON.toJavaObject(jsonArray.getJSONObject(i), AnnotationInfoCreateDTO.class));
                    }
                    BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO = new BatchAnnotationInfoCreateDTO();
                    batchAnnotationInfoCreateDTO.setAnnotations(list);
                    annotationService.doFinishAuto(taskSplitBO, batchAnnotationInfoCreateDTO.toMap());
                    redisUtils.del(task);
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.TEN);
                } else {
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.THREE_THOUSAND);
                }
            } catch (Exception exception) {
                LogUtil.error(LogEnum.BIZ_DATASET, "get imageNet finish task failed:{}", exception);
            }
        }
    }

    /**
     * imageNet任务是否过期
     */
    @Scheduled(cron = "*/15 * * * * ?")
    public void expireImageNetTask() {
        taskUtils.restartTask(IMAGENET_START_QUEUE, IMAGENET_PENDING_QUEUE);
    }

}
