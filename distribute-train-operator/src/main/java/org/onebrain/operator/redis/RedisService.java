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

package org.onebrain.operator.redis;

import org.onebrain.operator.utils.FastjsonUtils;
import org.onebrain.operator.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @description redis服务
 * @date 2020-09-03
 */
@Service
public class RedisService {

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 真正key模板
     */
    private static final String REAL_KEY_TEMPLATE = "%s:%s";

    /**
     * 获取真正的key
     * @param prefix 前缀
     * @param key key值
     * @return 放入redis里的key值
     */
    private String getRealKey(AbstractKeyPrefix prefix, String key){
        return String.format(REAL_KEY_TEMPLATE, prefix.getPrefix(), key);
    }

    /**
     * 实现命令：TTL key，以秒为单位，返回给定 key的剩余生存时间(TTL, time to live)。
     * @param prefix 前缀
     * @param key key值
     * @return 返回过期时间秒数
     */
    public long ttl(AbstractKeyPrefix prefix, String key) {
        return redisUtils.ttl(getRealKey(prefix, key));
    }

    /**
     * 实现命令：expire 设置过期时间，单位秒
     * @param prefix 前缀
     * @param key key值
     * @param timeout 期望过期时间
     */
    public void expire(AbstractKeyPrefix prefix, String key, long timeout) {
        redisUtils.expire(getRealKey(prefix, key), timeout);
    }

    /**
     * 实现命令：INCR key，增加key一次
     * @param prefix 前缀
     * @param key key值
     * @param delta 增量
     * @return 计数值
     */
    public long incr(AbstractKeyPrefix prefix, String key, long delta) {
        return redisUtils.incr(getRealKey(prefix, key), delta);
    }

    /**
     * 实现命令： key，减少key一次
     * @param prefix 前缀
     * @param key key值
     * @param delta 增量
     * @return 计数值
     */
    public long decr(AbstractKeyPrefix prefix, String key, long delta) {
        String realKey  = getRealKey(prefix, key);
        if(delta < 0){
            //throw new RuntimeException("递减因子必须大于0");
            del(realKey);
            return 0;
        }
        return redisUtils.decr(realKey, delta);
    }

    /**
     * 实现命令：KEYS pattern，查找所有符合给定模式 pattern的 key
     * @param prefix key前缀
     * @return key集合
     */
    public Set<String> keys(AbstractKeyPrefix prefix) {
        String pattern  = prefix.getPrefix();
        return redisUtils.keys(pattern + ":*");
    }

    /**
     * 实现命令：KEYS pattern，查找所有符合给定模式 pattern的 key
     * @param prefix key前缀
     * @param key key值
     * @return key集合
     */
    public Set<String> keys(AbstractKeyPrefix prefix, String key) {
        String pattern  = prefix.getPrefix();
        return redisUtils.keys(pattern + ":" + key + ":*");
    }

    /**
     * 实现命令：DEL key，删除一个key
     * @param prefix key前缀
     * @param key key值
     */
    public void del(AbstractKeyPrefix prefix, String key) {
        redisUtils.del(getRealKey(prefix, key));
    }

    /**
     * 删除一个key
     * @param realKey 真正的key
     */
    public void del(String realKey) {
        redisUtils.del(realKey);
    }

    /**
     * 实现命令：SET key value，设置一个key-value（将字符串值 value关联到 key）
     * @param prefix key前缀
     * @param key key值
     * @param value 值
     */
    public void set(AbstractKeyPrefix prefix, String key, String value) {
        if(prefix.getExpireSeconds() <= 0){
            redisUtils.set(getRealKey(prefix, key), value);
        }else{
            redisUtils.set(getRealKey(prefix, key), value, prefix.getExpireSeconds());
        }
    }

    /**
     * 实现命令：SET key value，设置一个key-value（将字符串值 value关联到 key）
     * @param prefix key前缀
     * @param key key值
     * @param value 值
     * @param <T> 指定类型
     */
    public <T> void set(AbstractKeyPrefix prefix, String key, T value) {
        if(prefix.getExpireSeconds() <= 0){
            redisUtils.set(getRealKey(prefix, key), FastjsonUtils.convertObjectToJSON(value));
        }else{
            redisUtils.set(getRealKey(prefix, key), FastjsonUtils.convertObjectToJSON(value), prefix.getExpireSeconds());
        }
    }


    /**
     * 实现命令：SET key value EX seconds，设置key-value和超时时间（秒）
     * @param prefix key前缀
     * @param key key值
     * @param value 值
     * @param timeout 过期时间
     */
    public void set(AbstractKeyPrefix prefix, String key, String value, long timeout) {
        redisUtils.set(getRealKey(prefix, key), value, timeout);
    }

    /**
     * 实现命令：SET key value EX seconds，设置key-value和超时时间（秒）
     * @param prefix key前缀
     * @param key key值
     * @param value 值
     * @param timeout 过期时间
     * @param <T> 指定类型
     */
    public <T> void set(AbstractKeyPrefix prefix, String key, T value, long timeout) {
        redisUtils.set(getRealKey(prefix, key), FastjsonUtils.convertObjectToJSON(value), timeout);
    }

    /**
     * 实现命令：SETNX key value，设置一个key-value（将字符串值 value关联到 key）
     * @param prefix key前缀
     * @param key key值
     * @param value 值
     * @return 是否设值成功
     */
    public Boolean setnx(AbstractKeyPrefix prefix, String key, String value){
        if(prefix.getExpireSeconds() <= 0){
            return redisUtils.setnx(getRealKey(prefix, key), value);
        }else{
            return redisUtils.setnx(getRealKey(prefix, key), value, prefix.getExpireSeconds());
        }
    }

    /**
     * 实现命令：SETNX key value，设置一个key-value（将字符串值 value关联到 key）
     * @param prefix key前缀
     * @param key key值
     * @param value 值
     * @param <T> 指定类型
     * @return 是否设值成功
     */
    public <T> Boolean setnx(AbstractKeyPrefix prefix, String key, T value){
        if(prefix.getExpireSeconds() <= 0){
            return redisUtils.setnx(getRealKey(prefix, key), FastjsonUtils.convertObjectToJSON(value));
        }else{
            return redisUtils.setnx(getRealKey(prefix, key), FastjsonUtils.convertObjectToJSON(value), prefix.getExpireSeconds());
        }
    }

    /**
     * 实现命令：SETNX key value EX seconds，设置key-value和超时时间（秒）
     * @param prefix key前缀
     * @param key key值
     * @param value 值
     * @param timeout 过期时间
     * @return 是否设值成功
     */
    public Boolean setnx(AbstractKeyPrefix prefix, String key, String value, long timeout) {
        return redisUtils.setnx(getRealKey(prefix, key), value, timeout);
    }

    /**
     * 实现命令：SETNX key value EX seconds，设置key-value和超时时间（秒）
     * @param prefix key前缀
     * @param key key值
     * @param value 值
     * @param timeout 过期时间
     * @param <T> 指定类型
     * @return 是否设值成功
     */
    public <T> Boolean setnx(AbstractKeyPrefix prefix, String key, T value, long timeout) {
        return redisUtils.setnx(getRealKey(prefix, key), FastjsonUtils.convertObjectToJSON(value), timeout);
    }

    /**
     * 实现命令：GET key，返回 key所关联的字符串值。
     * @param prefix key前缀
     * @param key key值
     * @return 值
     */
    public String get(AbstractKeyPrefix prefix, String key) {
        return redisUtils.get(getRealKey(prefix, key));
    }

    /**
     * 实现命令：GET key，返回 key所关联的字符串值。
     * @param prefix key前缀
     * @param key key值
     * @param <T> 指定类型
     * @return 值
     */
    public <T> T get(AbstractKeyPrefix prefix, String key, Class<T> clazz) {
        return redisUtils.get(getRealKey(prefix, key), clazz);
    }

    /**
     * 根据key获取值
     * @param lastKey 真正的key
     * @param clazz 类型
     * @param <T> 泛型
     * @return
     */
    public <T> T get(String lastKey, Class<T> clazz) {
        return redisUtils.get(lastKey, clazz);
    }

    /**
     * 实现命令：GET key，返回 key所关联的字符串值。
     * @param prefix key前缀
     * @param key key值
     * @return 是否存在
     */
    public Boolean exists(AbstractKeyPrefix prefix, String key) {
        return  redisUtils.exists(getRealKey(prefix, key));
    }


}
