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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.data.constant.FileTypeEnum;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.AnnotationInfoCreateDTO;
import org.dubhe.data.domain.dto.BatchAnnotationInfoCreateDTO;
import org.dubhe.data.service.AnnotationService;
import org.dubhe.data.util.TaskUtils;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @description 文本分类完成队列处理
 * @date 2020-12-03
 */
@Slf4j
@Component
public class TextClassificationQueueExecuteThread implements Runnable {

    /**
     * esSearch索引
     */
    @Value("${es.index}")
    private String esIndex;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private AnnotationService annotationService;
    @Autowired
    private TaskUtils taskUtils;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 文本分类算法执行中任务队列
     */
    private static final String TC_START_QUEUE = "text_classification_processing_queue";
    /**
     * 文本分类算法未执行任务队列
     */
    private static final String TC_PENDING_QUEUE = "text_classification_task_queue";
    /**
     * 文本分类算法已完成任务队列
     */
    private static final String TC_FINISHED_QUEUE = "text_classification_finished_queue";

    /**
     * 启动文本分类任务处理线程
     */
    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "自动文本分类完成任务处理队列");
        thread.start();
    }

    /**
     * 文本分类任务处理方法
     */
    @Override
    public void run() {
        while (true) {
            try {
                Object object = redisUtils.lpop(TC_FINISHED_QUEUE);
                if (ObjectUtil.isNotNull(object)) {
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(redisUtils.get(object.toString())));
                    String detailId = jsonObject.getString("reTaskId");
                    JSONObject taskDetail = JSON.parseObject(JSON.toJSONString(redisUtils.get(detailId)));
                    // 得到一个任务的拆分 包括多个file
                    TaskSplitBO taskSplitBO = JSON.parseObject(JSON.toJSONString(taskDetail), TaskSplitBO.class);
                    JSONArray jsonArray = jsonObject.getJSONArray("classifications");

                    List<AnnotationInfoCreateDTO> list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        list.add(JSON.toJavaObject(jsonArray.getJSONObject(i), AnnotationInfoCreateDTO.class));
                    }
                    BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO = new BatchAnnotationInfoCreateDTO();
                    batchAnnotationInfoCreateDTO.setAnnotations(list);
                    annotationService.doFinishAuto(taskSplitBO, batchAnnotationInfoCreateDTO.toMap());
                    list.forEach(annotationInfoCreateDTO -> {
                        UpdateRequest updateRequest = new UpdateRequest(esIndex,"_doc"
                                ,annotationInfoCreateDTO.getId().toString());
                        JSONObject annotationJson = JSONArray.parseArray(annotationInfoCreateDTO.getAnnotation()).getJSONObject(0);
                        JSONObject esJsonObject = new JSONObject();
                        esJsonObject.put("labelId",annotationJson.getString("category_id"));
                        esJsonObject.put("prediction",annotationJson.getString("score"));
                        if(annotationInfoCreateDTO.getAnnotation().isEmpty()){
                            esJsonObject.put("status", String.valueOf(FileTypeEnum.ANNOTATION_NOT_DISTINGUISH_FILE.getValue()));
                        } else {
                            esJsonObject.put("status", String.valueOf(FileTypeEnum.AUTO_FINISHED.getValue()));
                        }
                        updateRequest.doc(esJsonObject, XContentType.JSON);
                        try {
                            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
                        } catch (IOException e) {
                            LogUtil.error(LogEnum.BIZ_DATASET, "update es data error:{}", e);
                            }
                    });
                    redisUtils.del(detailId);
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.TEN);
                } else {
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.THREE_THOUSAND);
                }
            } catch (Exception exception) {
                LogUtil.error(LogEnum.BIZ_DATASET, "text classification exception:{}", exception);
            }
        }
    }

    /**
     * annotation任务是否过期
     */
    @Scheduled(cron = "*/15 * * * * ?")
    public void expireAnnotationTask() {
        taskUtils.restartTask(TC_START_QUEUE, TC_PENDING_QUEUE);
    }

}
