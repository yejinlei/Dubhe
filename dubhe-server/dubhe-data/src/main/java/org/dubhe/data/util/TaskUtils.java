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

package org.dubhe.data.util;

import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

/**
 * @description 任务处理工具类
 * @date 2020-09-10
 */
@Component
public class TaskUtils {

    private RedisTemplate<Object, Object> redisTemplate;

    public TaskUtils(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取zset中的数据和时间
     *
     * @param key redis中key
     * @return Set<ZSetOperations.TypedTuple < Object>> 返回zset中数据
     */
    public Set<ZSetOperations.TypedTuple<Object>> zGetWithScore(String key) {
        try {
            return redisTemplate.opsForZSet().rangeWithScores(key, Long.MIN_VALUE, Long.MAX_VALUE);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "RedisUtils rangeWithScores key {} error:{}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 添加任务到redis
     *
     * @param queueName    任务队列名称
     * @param taskDetails  任务详情
     * @param score        分数
     * @return 任务是否存放成功
     */
    public boolean addTask(String queueName, String taskDetails, String detailKey,int score) {
        DefaultRedisScript<Boolean> addTaskScript = new DefaultRedisScript<>();
        addTaskScript.setResultType(Boolean.class);
        addTaskScript.setLocation(new ClassPathResource("addTask.lua"));
        try {
            return redisTemplate.execute(addTaskScript, Collections.singletonList(detailKey)
                    , queueName, taskDetails, score);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "RedisUtils addTask error:{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 任务完成
     *
     * @param queueName 队列名
     * @param keyId     keyId
     * @param taskType  任务类型
     * @param taskKey   任务key
     * @return boolean 是否完成
     */
    public boolean finishedTask(String queueName, String keyId, String taskType, String taskKey) {
        DefaultRedisScript<Boolean> finishedTaskScript = new DefaultRedisScript<>();
        finishedTaskScript.setResultType(Boolean.class);
        finishedTaskScript.setLocation(new ClassPathResource("finishedTask.lua"));
        try {
            return redisTemplate.execute(finishedTaskScript, Collections.singletonList(queueName)
                    , keyId, taskType, taskKey);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "RedisUtils finishedTask error:{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 重启任务
     *
     * @param keyId           keyId
     * @param processingName  执行中名称
     * @param unprocessedName 未执行名称
     * @param detailName      详情名
     * @param datasetIdKey    数据集ID对应key
     * @return boolean 重启任务是否成功
     */
    public boolean restartTask(String keyId, String processingName, String unprocessedName, String detailName, String datasetIdKey) {
        DefaultRedisScript<Boolean> restartTaskScript = new DefaultRedisScript<>();
        restartTaskScript.setResultType(Boolean.class);
        restartTaskScript.setLocation(new ClassPathResource("restartTask.lua"));
        try {
            return redisTemplate.execute(restartTaskScript, Collections.singletonList(keyId),
                    processingName, unprocessedName, detailName, datasetIdKey);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "RedisUtils restartTask error:{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 将zSet数据放入缓存
     *
     * @param key key
     * @return Boolean 放入数据到缓存是否成功
     */
    public Boolean zAdd(String key, Object value, Long zscore) {
        try {
            return redisTemplate.opsForZSet().add(key, value, zscore);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "RedisUtils zSet key {} value {} error:{}", key, value, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 移除指定key
     *
     * @param key    key
     * @param member 所移除成员
     * @return Object 删除指定key
     */
    public Object zrem(String key, String member) {
        try {
            return redisTemplate.opsForZSet().remove(key, member);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "RedisUtils zrem key {} member {} error:{}", key, member, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 任务重启
     *
     * @param startQueue   算法执行中队列
     * @param pendingQueue 算法未执行队列
     */
    public void restartTask(String startQueue, String pendingQueue) {
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = zGetWithScore(startQueue);
        typedTuples.forEach(value -> {
            String timestampString = new BigDecimal(StringUtils.substringBefore(value.getScore().toString(),"."))
                    .toPlainString();
            long timestamp = Long.parseLong(timestampString);
            String keyId = value.getValue().toString().replaceAll("\"", "");
            long timestampNow = System.currentTimeMillis() / 1000;
            if (timestampNow - timestamp > MagicNumConstant.TWO_HUNDRED) {
                LogUtil.info(LogEnum.BIZ_DATASET, "restart {} task keyId:{}", startQueue, keyId);
            }
        });
    }
}
