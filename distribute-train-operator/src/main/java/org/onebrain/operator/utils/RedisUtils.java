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

package org.onebrain.operator.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @description 封装redis简单的key-value操作
 * @date 2020-09-23
 */
@Component
public class RedisUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 实现命令：TTL key，以秒为单位，返回给定 key的剩余生存时间(TTL, time to live)。
     * @param key key值
     * @return 返回过期时间秒数
     */
    public long ttl(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 实现命令：expire 设置过期时间，单位秒
     * @param key key值
     * @param timeout 期望过期时间
     */
    public void expire(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 实现命令：INCR key，增加key一次
     * @param key key值
     * @param delta 增量
     * @return 计数值
     */
    public long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 实现命令： key，减少key一次
     * @param key key值
     * @param delta 增量
     * @return 计数值
     */
    public long decr(String key, long delta) {
        if(delta < 0){
            //throw new RuntimeException("递减因子必须大于0");
            del(key);
            return 0;
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    /**
     * 实现命令：KEYS pattern，查找所有符合给定模式 pattern的 key
     * @return key集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 实现命令：DEL key，删除一个key
     * @param key key值
     */
    public void del(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 实现命令：SET key value，设置一个key-value（将字符串值 value关联到 key）
     * @param key key值
     * @param value 值
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 实现命令：SET key value，设置一个key-value（将字符串值 value关联到 key）
     * @param key key值
     * @param value 值
     * @param <T> 指定类型
     */
    public <T> void set(String key, T value) {
        redisTemplate.opsForValue().set(key, FastjsonUtils.convertObjectToJSON(value));
    }

    /**
     * 实现命令：SET key value EX seconds，设置key-value和超时时间（秒）
     * @param key key值
     * @param value 值
     * @param timeout 过期时间
     */
    public void set(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 实现命令：SET key value EX seconds，设置key-value和超时时间（秒）
     * @param key key值
     * @param value 值
     * @param timeout 过期时间
     * @param <T> 指定类型
     */
    public <T> void set(String key, T value, long timeout) {
        redisTemplate.opsForValue().set(key, FastjsonUtils.convertObjectToJSON(value), timeout, TimeUnit.SECONDS);
    }

    /**
     * 实现命令：SETNX key value，设置一个key-value（将字符串值 value关联到 key）
     * @param key key值
     * @param value 值
     * @return 是否设值成功
     */
    public Boolean setnx(String key, String value){
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 实现命令：SETNX key value，设置一个key-value（将字符串值 value关联到 key）
     * @param key key值
     * @param value 值
     * @param <T> 指定类型
     * @return 是否设值成功
     */
    public <T> Boolean setnx(String key, T value){
        return redisTemplate.opsForValue().setIfAbsent(key, FastjsonUtils.convertObjectToJSON(value));
    }

    /**
     * 实现命令：SETNX key value EX seconds，设置key-value和超时时间（秒）
     * @param key key值
     * @param value 值
     * @param timeout 过期时间
     * @return 是否设值成功
     */
    public Boolean setnx(String key, String value, long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 实现命令：SETNX key value EX seconds，设置key-value和超时时间（秒）
     * @param key key值
     * @param value 值
     * @param timeout 过期时间
     * @param <T> 指定类型
     * @return 是否设值成功
     */
    public <T> Boolean setnx(String key, T value, long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key, FastjsonUtils.convertObjectToJSON(value), timeout, TimeUnit.SECONDS);
    }

    /**
     * 实现命令：GET key，返回 key所关联的字符串值。
     * @param key key值
     * @return 值
     */
    public String get(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     *
     * 根据key获取值
     * @param key 真正的key
     * @param clazz 类型
     * @param <T> 泛型
     * @return
     */
    public <T> T get(String key, Class<T> clazz) {
        String value = (String) redisTemplate.opsForValue().get(key);
        return (T) FastjsonUtils.convertJsonToObject(value, clazz);
    }

    /**
     * 实现命令：GET key，返回 key所关联的字符串值。
     * @param key key值
     * @return 是否存在
     */
    public Boolean exists(String key) {
        return  redisTemplate.hasKey(key);
    }

    /****----------------------------------Hash----------------------------------------****/

    /**
     * 实现命令：HSET key field value，将哈希表 key中的域 field的值设为 value
     *
     * @param key key
     * @param field 域
     * @param value 值
     */
    public void hset(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 实现命令：HGET key field，返回哈希表 key中给定域 field的值
     *
     * @param key key
     * @param field 域
     * @return
     */
    public String hget(String key, String field) {
        return (String) redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 实现命令：HDEL key field [field ...]，删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
     *
     * @param key key
     * @param fields 域
     */
    public void hdel(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 实现命令：HGETALL key，返回哈希表 key中，所有的域和值。
     *
     * @param key
     * @return 域和值
     */
    public Map<Object, Object> hgetall(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /****----------------------------------List----------------------------------------****/

    /**
     * 实现命令：LPUSH key value，将一个值 value插入到列表 key的表头
     *
     * @param key
     * @param value
     * @return 执行 LPUSH命令后，列表的长度。
     */
    public long lpush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 实现命令：LPOP key，移除并返回列表 key的头元素。
     *
     * @param key
     * @return 列表key的头元素。
     */
    public String lpop(String key) {
        return (String) redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 实现命令：RPUSH key value，将一个值 value插入到列表 key的表尾(最右边)。
     *
     * @param key
     * @param value
     * @return 执行 LPUSH命令后，列表的长度。
     */
    public long rpush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }
}
