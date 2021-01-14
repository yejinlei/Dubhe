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

import io.micrometer.core.instrument.util.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.constant.GatewayConstant;
import org.dubhe.constant.NumberConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.enums.ServingRouteEventEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description redis stream监听消费消息
 * @date 2020-12-18
 */
@Component
@Setter
@Slf4j
public class RedisStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    private GatewayServiceHandler gatewayServiceHandler;

    @Value("${serving.group}")
    private String servingGroup;

    /**
     * 消费消息
     *
     * @param message
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> body = message.getValue();
        log.info("stream message, messageId={}, stream={}, body={}", message.getId(), message.getStream(), body);
        receiveMessage(body.get(servingGroup));
        // 通过StringRedisTemplate手动确认消息
        Long acknowledge = stringRedisTemplate.opsForStream().acknowledge(GatewayConstant.SERVING_STREAM, servingGroup, message.getId());
        if (Objects.nonNull(acknowledge) && acknowledge > NumberConstant.NUMBER_0) {
            log.info("ack is ok , then delete record by id={}", message.getId());
            // 确认消息被消费后，从stream队列中移除该记录
            stringRedisTemplate.opsForStream().delete(GatewayConstant.SERVING_STREAM, message.getId());
        }
    }

    /**
     * 处理订阅的服务路由状态topic的消息，包含对路由内容的解析和实际处理。
     * 报文主要格式为 _SAVE:{routeId1}, {routeId2}&&_DELETE:{routeId3}, {routeId4}
     * 其中_SAVE:和_DELETE:为具体的事件类型，分别代表新增路由和移除路由, 事件类型后接具体模型部署服务的数据库主键，不同主键用(,)分开。
     * &&为不同事件的分割标识，消息体中可仅包含一种事件也可以包含多种事件。
     *
     * @param message 消息
     */
    public void receiveMessage(String message) {
        // 去除引号
        String content = message.replace(SymbolConstant.MARK, SymbolConstant.BLANK);
        if (StringUtils.isBlank(content)) {
            return;
        }
        // 分割不同事件
        String[] eventArray = content.split(SymbolConstant.EVENT_SEPARATOR);
        try {
            for (String event : eventArray) {
                String[] strings = event.split(SymbolConstant.COLON);
                if (strings.length != NumberConstant.NUMBER_2) {
                    break;
                }
                ServingRouteEventEnum eventType = ServingRouteEventEnum.getByCode(strings[NumberConstant.NUMBER_0]);
                List<Long> routeIdList = Arrays.stream(strings[NumberConstant.NUMBER_1].split(SymbolConstant.COMMA))
                        .map(Long::valueOf).collect(Collectors.toList());
                // 进行业务处理
                this.handleEvent(routeIdList, eventType);
            }
        } catch (NumberFormatException e) {
            log.error("错误的消息:{}", message);
        }
    }

    /**
     * 处理相关路由事件
     *
     * @param routeIdList 相关路由信息的数据库主键列表
     * @param eventType   事件类型
     */
    private void handleEvent(List<Long> routeIdList, ServingRouteEventEnum eventType) {
        switch (eventType) {
            case SAVE:
                for (Long routeId : routeIdList) {
                    gatewayServiceHandler.saveRouteByRouteId(routeId);
                }
                break;
            case DELETE:
                for (Long routeId : routeIdList) {
                    gatewayServiceHandler.deleteRouteByRouteId(routeId);
                }
                break;
            default:
                break;
        }
    }
}