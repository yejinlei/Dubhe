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

package org.dubhe.biz.redis.utils;

import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description redis工具类
 * @date 2020-03-13
 */
@Component
@SuppressWarnings({"unchecked", "all"})
public class RedisUtils {

    private RedisTemplate<Object, Object> redisTemplate;
    @Value("${jwt.online-key}")
    private String onlineKey;

    public RedisUtils(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // =============================common============================

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils expire key {} time {} error:{}", key, time, e);
            return false;
        }
        return true;
    }

    /**
     * 根据 key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(Object key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 查找匹配key
     *
     * @param pattern key
     * @return List<String> 匹配的key集合
     */
    public List<String> scan(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection rc = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cursor = rc.scan(options);
        List<String> result = new ArrayList<>();
        while (cursor.hasNext()) {
            result.add(new String(cursor.next()));
        }
        try {
            RedisConnectionUtils.releaseConnection(rc, factory, true);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils scan pattern {} error:{}", pattern, e);
        }
        return result;
    }

    /**
     * 分页查询 key
     *
     * @param patternKey key
     * @param page       页码
     * @param size       每页数目
     * @return 匹配到的key集合
     */
    public List<String> findKeysForPage(String patternKey, int page, int size) {
        ScanOptions options = ScanOptions.scanOptions().match(patternKey).build();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection rc = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cursor = rc.scan(options);
        List<String> result = new ArrayList<>(size);
        int tmpIndex = 0;
        int fromIndex = page * size;
        int toIndex = page * size + size;
        while (cursor.hasNext()) {
            if (tmpIndex >= fromIndex && tmpIndex < toIndex) {
                result.add(new String(cursor.next()));
                tmpIndex++;
                continue;
            }
            // 获取到满足条件的数据后,就可以退出了
            if (tmpIndex >= toIndex) {
                break;
            }
            tmpIndex++;
            cursor.next();
        }
        try {
            RedisConnectionUtils.releaseConnection(rc, factory, true);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils findKeysForPage patternKey {} page {} size {} error:{}", patternKey, page, size, e);
        }
        return result;
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils hasKey key {} error:{}", key, e);
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     *
     * @param script 脚本字符串
     * @param key 键
     * @param args 脚本其他参数
     * @return
     */
    public Object executeRedisScript(String script, String key, Object... args) {
        try {
            RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
            return redisTemplate.execute(redisScript, Collections.singletonList(key), args);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "executeRedisScript script {} key {} args {} error:{}", script, key, args, e);
            return MagicNumConstant.ZERO_LONG;
        }
    }

    /**
     *
     * @param script 脚本字符串
     * @param key 键
     * @param args 脚本其他参数
     * @return
     */
    public Object executeRedisObjectScript(String script, String key, Object... args) {
        try {
            RedisScript<Object> redisScript = new DefaultRedisScript<>(script, Object.class);
            redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
            return redisTemplate.execute(redisScript, Collections.singletonList(key), args);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "executeRedisObjectScript script {} key {} args {} error:{}", script, key, args, e);
            return null;
        }
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return key对应的value值
     */
    public Object get(String key) {

        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量获取
     *
     * @param keys key集合
     * @return key集合对应的value集合
     */
    public List<Object> multiGet(List<String> keys) {
        Object obj = redisTemplate.opsForValue().multiGet(Collections.singleton(keys));
        return null;
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils set key {} value {} error:{}", key, value, e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils set key {} value {} time {} error:{}", key, value, time, e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key      键
     * @param value    值
     * @param time     时间
     * @param timeUnit 类型
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils set key {} value {} time {} timeUnit {} error:{}", key, value, time, timeUnit, e);
            return false;
        }
    }

    //===============================Lock=================================

    /**
     * 加锁
     * @param key 键
     * @param requestId 请求id用以释放锁
     * @param expireTime 超时时间（秒）
     * @return
     */
    public boolean getDistributedLock(String key, String requestId, long expireTime) {
        String script = "if redis.call('setNx',KEYS[1],ARGV[1]) == 1 then if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end else return 0 end";
        Object result = executeRedisScript(script, key, requestId, expireTime);
        return result != null && result.equals(MagicNumConstant.ONE_LONG);
    }

    /**
     * 释放锁
     * @param key 键
     * @param requestId 请求id用以释放锁
     * @return
     */
    public boolean releaseDistributedLock(String key, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = executeRedisScript(script, key, requestId);
        return result != null && result.equals(MagicNumConstant.ONE_LONG);
    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);

    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils hmset key {} map {} error:{}", key, map, e);
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils hmset key {} map {} time {} error:{}", key, map, time, e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils hset key {} item {} value {} error:{}", key, item, value, e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils hset key {} item {} value {} time {} error:{}", key, item, value, time, e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils sGet key {} error:{}", key, e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils sHasKey key {} value {} error:{}", key, value, e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils sSet key {} values {} error:{}", key, values, e);
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils sSetAndTime key {} time {} values {} error:{}", key, time, values, e);
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils sGetSetSize key {} error:{}", key, e);
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils setRemove key {} values {} error:{}", key, values, e);
            return 0;
        }
    }

    // ===============================sorted set=================================

    /**
     * 将zSet数据放入缓存
     *
     * @param key
     * @param time
     * @param values
     * @return Boolean
     */
    public Boolean zSet(String key, long time, Object value) {
        try {
            Boolean success = redisTemplate.opsForZSet().add(key, value, System.currentTimeMillis());
            if (success) {
                expire(key, time);
            }
            return success;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils zSet key {} time {} value {} error:{}", key, time, value, e);
            return false;
        }
    }

    /**
     * 将zSet数据放入缓存
     * @param key 健
     * @param score 分数
     * @param value 值
     * @return
     */
    public Boolean zAdd(String key,Long score,Object value){
        try{
            if (StringUtils.isEmpty(key) || score == null || value == null){
                return false;
            }
            return redisTemplate.opsForZSet().add(key, value, score);
        }catch (Exception e){
            LogUtil.error(LogEnum.REDIS, "RedisUtils zAdd key {} score {} value {} error:{}", key, score, value, e);
            return false;
        }
    }

    /**
     * 返回有序集合所有成员，从大到小排序
     *
     * @param key
     * @return Set<Object>
     */
    public Set<Object> zGet(String key) {
        try {
            return redisTemplate.opsForZSet().reverseRange(key, Long.MIN_VALUE, Long.MAX_VALUE);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils zGet key {} error:{}", key, e);
            return null;
        }
    }

    /**
     * 弹出有序集合 score 在 [min,max] 内由小到大从 offset 取 count 个
     * @param key 健
     * @param min score最小值
     * @param max score最大值
     * @param offset 起始下标
     * @param count 偏移量
     * @return
     */
    public List<Object> zRangeByScorePop(String key,double min,double max,long offset,long count){
        try{
            String script = "local elementSet = redis.call('ZRANGEBYSCORE',KEYS[1],ARGV[1],ARGV[2],'LIMIT',ARGV[3],ARGV[4]) if elementSet ~= false and #elementSet ~= 0 then redis.call('ZREM' , KEYS[1] , elementSet[1]) end return elementSet";
            Object result = executeRedisObjectScript(script, key, min, max,offset,count);
            return (List<Object>) result;
        }catch (Exception e){
            LogUtil.error(LogEnum.REDIS, "RedisUtils zRangeByScorePop key {} min {} max {} offset {} count {} error:{}", key,min, max, offset, count, e);
            return new ArrayList<>();
        }
    }

    /**
     * 弹出有序集合 score 在 [min,max] 内由小到大从 0 取 1 个
     * @param key 健
     * @param min score最小值
     * @param max score最大值
     * @return
     */
    public List<Object> zRangeByScorePop(String key,double min, double max){
        return zRangeByScorePop( key,min, max,0,1);
    }

    /**
     * 弹出有序集合 score 在 [0,max] 内由小到大从 offset 取 count 个
     * @param key 健
     * @param max score最大值
     * @return
     */
    public List<Object> zRangeByScorePop(String key,double max){
        return zRangeByScorePop( key,0, max,0,1);
    }

    /**
     * 根据键获取score值为 min 到 max 之间的所有 member 和 score
     * @param key 健
     * @param min score最小值
     * @param max score最大值
     * @return
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeByScoreWithScores(String key, Long min, Long max){
        try {
            return redisTemplate.opsForZSet().rangeWithScores(key, min, max);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "RedisUtils rangeWithScores key {} error:{}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据 key 和 member 移除元素
     * @param key
     * @param member
     * @return
     */
    public Boolean zRem(String key,Object member){
        try{
            if (StringUtils.isEmpty(key) || null == member){
                return false;
            }
            redisTemplate.opsForZSet().remove(key,member);
            return true;
        }catch (Exception e){
            LogUtil.error(LogEnum.REDIS, "RedisUtils zrem key {} member {} error:{}", key, member, e);
            return false;
        }
    }

    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lGetIndex key {} start {} end {} error:{}", key, start, end, e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lGetListSize key {} error:{}", key, e);
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lGetIndex key {} index {} error:{}", key, index, e);
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lSet key {} value {} error:{}", key, value, e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return 是否存储成功
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lSet key {} value {} time {} error:{}", key, value, time, e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return 是否存储成功
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lSet key {} value {} error:{}", key, value, e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return 是否存储成功
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lSet key {} value {} time {} error:{}", key, value, time, e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return 更新数据标识
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lUpdateIndex key {} index {} value {} error:{}", key, index, value, e);
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lRemove key {} count {} value {} error:{}", key, count, value, e);
            return 0;
        }
    }

    /**
     * 队列从左弹出数据
     *
     * @param key
     * @return key对应的value值
     */
    public Object lpop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            LogUtil.error(LogEnum.REDIS, "RedisUtils lRemove key {} error:{}", key, e);
            return null;
        }
    }

    /**
     * 队列从右压入数据
     *
     * @param key
     * @param value
     * @return long
     */
    public long rpush(String key, Object value) {
        try {
            return redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "RedisUtils rpush key {} error:{}", key, e.getMessage());
            return 0L;
        }
    }
}
