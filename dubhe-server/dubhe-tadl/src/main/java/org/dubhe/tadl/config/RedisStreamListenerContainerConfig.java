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
package org.dubhe.tadl.config;

import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.tadl.domain.dto.ExperimentAndTrailDTO;
import org.dubhe.tadl.listener.RedisStreamListener;
import org.dubhe.tadl.constant.RedisKeyConstant;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @description redis stream listener config
 * @date 2021-03-05
 */
@Configuration
public class RedisStreamListenerContainerConfig implements ApplicationRunner, DisposableBean {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private RedisStreamListener redisStreamListener;

    private StreamMessageListenerContainer<String, ObjectRecord<String, ExperimentAndTrailDTO>> streamMessageListenerContainer;

    @Bean
    public ThreadPoolTaskScheduler initTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(20);
        return threadPoolTaskScheduler;
    }


    @Override
    public void run(ApplicationArguments args) {
        // 创建配置对象
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, ExperimentAndTrailDTO>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        // 一次性最多拉取多少条消息
                        .batchSize(10)
                        // 执行消息轮询的执行器
                        .executor(initTaskScheduler())
                        // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                        .pollTimeout(Duration.ZERO)
                        .targetType(ExperimentAndTrailDTO.class)
                        .build();
        // 根据配置对象创建监听容器对象
        streamMessageListenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);
        // 启动监听
        streamMessageListenerContainer.start();
    }

    @Override
    public void destroy() {
        streamMessageListenerContainer.stop();
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
    }

    /**
     * 查找Stream信息，如果不存在，则创建Stream
     */
    public Mono<StreamInfo.XInfoStream> prepareStreamAndGroup(ReactiveStreamOperations<String, ?, ?> ops, String stream, String group) {
        return ops.info(stream).onErrorResume(err -> ops.createGroup(stream, group).flatMap(s -> ops.info(stream)));
    }

    /**
     * 绑定指定消费者
     *
     * @param mono
     * @param streamName
     * @param group
     */
    public void prepareDisposable(Mono<StreamInfo.XInfoStream> mono, String streamName, String group) {
        mono.subscribe(stream -> streamMessageListenerContainer.receive(Consumer.from(group, RedisKeyConstant.CONSUMER),
                StreamOffset.create(streamName, ReadOffset.lastConsumed()),
                redisStreamListener));
    }

    /**
     * 创建Redis Stream 并绑定指定消费者
     *
     * @param ops
     * @param stream
     * @param group
     */
    public void buildRedisStream(ReactiveStreamOperations<String, ?, ?> ops, String stream, String group) {
        if (!StringUtils.isEmpty(stream) && !StringUtils.isEmpty(group)) {
            Mono<StreamInfo.XInfoStream> mono = prepareStreamAndGroup(ops, stream, group);
            prepareDisposable(mono, stream, group);
        }
    }
}
