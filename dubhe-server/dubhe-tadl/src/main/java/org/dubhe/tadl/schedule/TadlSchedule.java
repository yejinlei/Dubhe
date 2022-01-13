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
package org.dubhe.tadl.schedule;

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.tadl.constant.RedisKeyConstant;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.dao.ExperimentStageMapper;
import org.dubhe.tadl.domain.entity.ExperimentStage;
import org.dubhe.tadl.machine.constant.ExperimentStageEventMachineConstant;
import org.dubhe.tadl.machine.utils.identify.StateMachineUtil;
import org.dubhe.tadl.service.ExperimentStageService;
import org.dubhe.tadl.service.TadlRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * @description 任务超时定时器
 * @date 2021-03-31
 */
@Component
public class TadlSchedule {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ExperimentStageMapper experimentStageMapper;

    @Autowired
    private TadlRedisService tadlRedisService;

    @Autowired
    private ExperimentStageService experimentStageService;


    /**
     * 每秒执行一次扫描，处理过期的实验阶段
     */
    @Scheduled(cron = " */1 * * * * ? ")
    public void experimentStageExpiredTask() {
        //获取redis的zset有序队列中的第一个元素进行处理，即为最早的到期的实验阶段
        Set<ZSetOperations.TypedTuple<Object>> objects = redisUtils.zRangeByScoreWithScores(RedisKeyConstant.EXPERIMENT_STAGE_EXPIRED_TIME_SET, Long.MIN_VALUE, Long.MAX_VALUE);

        if (CollectionUtils.isEmpty(objects)){
            LogUtil.debug(LogEnum.TADL,"过期队列为空，不做处理");
            return;
        }

        //获取第一个元素进行处理，即为最早的到期的实验阶段
        Iterator<ZSetOperations.TypedTuple<Object>> it = objects.iterator();
        if (it.hasNext()) {
            ZSetOperations.TypedTuple<Object> obj = it.next();
            Double expiredTime = obj.getScore();
            if (Objects.isNull(expiredTime)) {
                LogUtil.error(LogEnum.TADL, "过期队列元素获取错误");
                return;
            }

            long now = System.currentTimeMillis();
            String experimentIdAndStageId = String.valueOf(obj.getValue());
            String[] idArr = experimentIdAndStageId.split(RedisKeyConstant.COLON);
            Long experimentId = Long.parseLong(idArr[0]);
            Long experimentStageId = Long.parseLong(idArr[1]);

            if (now < expiredTime) {
                LogUtil.debug(LogEnum.TADL, "实验id:{},阶段id:{},还有{}到期，不做处理", experimentId, experimentStageId, getGapTime(expiredTime - now));
                return;
            }

            ExperimentStage experimentStage = experimentStageMapper.getExperimentStateByExperimentIdAndStageId(experimentId, experimentStageId);

            if (Objects.isNull(experimentStage)) {
                LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG +" 无法获取对象", experimentId, experimentStageId);
                // 获取不到对象，直接删除
                experimentStageService.removeExpiredTimeToRedis(experimentId,experimentStageId);
                return;
            }

            //实验过期，停止实验
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "已到期，开始停止实验，状态置为失败,",experimentId,experimentStageId);
            //将运行中的任务状态变更为运行失败
            StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experimentStage.getId()},ExperimentStageEventMachineConstant.EXPERIMENT_STAGE_STATE_MACHINE,ExperimentStageEventMachineConstant.TIMEOUT_EXPERIMENT_STAGE_EVENT));
            //异步调用删除运行中的trial任务
            tadlRedisService.deleteRunningTrial(experimentStage.getId());
            //删除redis中的缓存
            tadlRedisService.delRedisExperimentInfo(experimentId);
            LogUtil.info(LogEnum.TADL,"实验id:{},阶段id:{},停止实验成功，状态变更完成",experimentId,experimentIdAndStageId);
            experimentStageService.removeExpiredTimeToRedis(experimentId,experimentStageId);
        }else {
            LogUtil.debug(LogEnum.TADL,"过期队列为空，不做处理");
        }
    }

    private String getGapTime(Double time) {
        long hours = time.longValue() / (1000 * 60 * 60);
        long minutes = (time.longValue() - hours * (1000 * 60 * 60)) / (1000 * 60);
        long second = (time.longValue() - hours * (1000 * 60 * 60) - minutes * (1000 * 60)) / 1000;
        return hours + "h" + minutes + "min" + second + "s";
    }
}
