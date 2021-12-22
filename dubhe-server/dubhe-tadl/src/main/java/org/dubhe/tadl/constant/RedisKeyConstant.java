/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this Trial except in compliance with the License.
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
package org.dubhe.tadl.constant;

/**
 * @description redis中key定义
 * @date 2021-07-29
 */
public class RedisKeyConstant {
    /**
     * 冒号
     */
    public static final String COLON = ":";

    /**
     * 下划线
     */
    public static final String UNDERLINE = "_";

    /**
     * 项目名称
     */
    public static final String TADL = "tadl:";

    /**
     * 实验
     */
    public static final String EXPERIMENT = "experiment:";

    /**
     * 实验阶段
     */
    public static final String EXPERIMENT_STAGE = "experiment_stage:";

    /**
     * 实验trial
     */
    public static final String TRIAL = "trial:";

    /**
     * Stream Stage Key
     * tadl:experiment_stage:trial:run_param:
     */
    private final static String STREAM_STAGE_KEY = TADL + EXPERIMENT_STAGE + TRIAL + "run_param:";

    /**
     * Stream Group
     * tadl:experiment_stage:group
     */
    private final static String STREAM_GROUP_KEY = TADL + EXPERIMENT_STAGE + "group:";

    /**
     * consumer
     */
    public final static String CONSUMER = "consumer";

    /**
     * paused key
     */
    private final static String EXPERIMENT_PAUSED_KEY = TADL + EXPERIMENT + "paused:";

    /**
     * 实验阶段过期时间zset队列
     * tadl:experiment_stage:expired_time_set
     */
    public static final String EXPERIMENT_STAGE_EXPIRED_TIME_SET = TADL + EXPERIMENT_STAGE + "expired_time_set";

    /**
     * 生成 组合 阶段 Stream key
     * 用于存储k8s相关的，trial运行参数
     * @param indexId 索引ID
     * @param stageId 阶段ID
     * @return String
     */
    public static String buildStreamStageKey(long indexId, Long stageId) {
        return STREAM_STAGE_KEY + indexId + UNDERLINE + stageId;
    }

    /**
     * 生成 组合 阶段 Stream group key
     *
     * @param indexId 索引ID
     * @param stageId 阶段ID
     * @return String
     */
    public static String buildStreamGroupStageKey(long indexId, Long stageId) {
        return STREAM_GROUP_KEY + indexId + COLON + stageId;
    }

    /**
     * 生成组合的 pausedKey
     * @param experimentId 实验id
     * @return String
     */
    public static String buildPausedKey(Long experimentId) {
        return EXPERIMENT_PAUSED_KEY + experimentId;
    }

    /**
     * 生成组合的 deletedKey
     * @param experimentId 实验id
     * @return String
     */
    public static String buildDeletedKey(Long experimentId, Long stageId, Long trialId) {
        return EXPERIMENT_PAUSED_KEY + experimentId + UNDERLINE + stageId + UNDERLINE + trialId;
    }
}
