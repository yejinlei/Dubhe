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

package org.dubhe.config;

import org.dubhe.constant.GatewayConstant;
import org.dubhe.constant.NumberConstant;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * @description redis stream监听启动类
 * @date 2020-12-18
 */
@Component
public class RedisStreamConsumerRunner implements ApplicationRunner, DisposableBean {

    @Resource
    RedisConnectionFactory redisConnectionFactory;

    @Resource
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    RedisStreamListener redisStreamListener;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Value("${serving.group}")
    private String servingGroup;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer;

    /**
     * 启动容器开始监听
     *
     * @param args
     */
    @Override
    public void run(ApplicationArguments args) {
        //先删除再创建group
        StreamOperations<String, Object, Object> streamOperations = stringRedisTemplate.opsForStream();
        streamOperations.destroyGroup(GatewayConstant.SERVING_STREAM, servingGroup);
        streamOperations.createGroup(GatewayConstant.SERVING_STREAM, servingGroup);
        // 创建配置对象
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions = StreamMessageListenerContainerOptions
                .builder()
                .executor(threadPoolTaskExecutor)
                .errorHandler(Throwable::printStackTrace)
                .batchSize(NumberConstant.NUMBER_10)
                .pollTimeout(Duration.ZERO)
                .serializer(new StringRedisSerializer())
                .build();
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer = StreamMessageListenerContainer
                .create(redisConnectionFactory, streamMessageListenerContainerOptions);
        streamMessageListenerContainer.receive(Consumer.from(servingGroup, "consumer"),
                StreamOffset.create(GatewayConstant.SERVING_STREAM, ReadOffset.lastConsumed()), redisStreamListener);
        this.streamMessageListenerContainer = streamMessageListenerContainer;
        // 启动监听
        this.streamMessageListenerContainer.start();
    }

    /**
     * 停止监听
     */
    @Override
    public void destroy() {
        this.streamMessageListenerContainer.stop();
    }
}
