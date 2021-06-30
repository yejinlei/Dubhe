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

package org.dubhe.biz.file.utils;

import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.stereotype.Component;

/**
 * @description NFS3 连接池
 * @date 2020-05-18
 */
@Deprecated
@Component
public class NfsPool {
    /**
     * NFS工厂对象
     */
    private NfsFactory nfsFactory;
    /**
     * GenericObjectPool对象
     */
    private final GenericObjectPool<Nfs3> genericObjectPool;
    /**
     * 最大总共连接数
     */
    public static final int MAX_TOTAL = 300;
    /**
     * 最小连接数
     */
    public static final int MIN = 20;
    /**
     * 最大连接数
     */
    public static final int MAX = 300;
    /**
     * 最大等待时间 单位毫秒
     */
    public static final int MAX_WAIT_TIME = 3000;

    /**
     * 初始化连接池
     *
     * @param nfsFactory
     */
    public NfsPool(NfsFactory nfsFactory) {
        this.nfsFactory = nfsFactory;
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(MAX_TOTAL);
        poolConfig.setMinIdle(MIN);
        poolConfig.setMaxIdle(MAX);
        poolConfig.setMaxWaitMillis(MAX_WAIT_TIME);
        this.genericObjectPool = new GenericObjectPool<>(nfsFactory, poolConfig);
    }

    /**
     * 从连接池中取连接
     *
     * @return nfs3
     */
    public Nfs3 getNfs() {
        try {
            LogUtil.info(LogEnum.NFS_UTIL,"NFS线程 活跃数量：{}  ,空闲数量: {} , 等待队列数量 ： {}",genericObjectPool.getNumActive(),genericObjectPool.getNumIdle(),genericObjectPool.getNumWaiters());
            return genericObjectPool.borrowObject();
        } catch (Exception e) {
            LogUtil.error(LogEnum.NFS_UTIL, "获取NFS连接失败: {} ", e);
            return null;
        }
    }

    /**
     * 释放连接到连接池
     *
     * @param nfs3
     */
    public void revertNfs(Nfs3 nfs3) {
        try {
            if(nfs3 != null){
                LogUtil.info(LogEnum.NFS_UTIL,"成功释放对象 : {} ", nfs3);
                genericObjectPool.returnObject(nfs3);
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.NFS_UTIL, " 释放NFS连接失败: ", e);
        }
    }

    /**
     * 销毁公共池
     */
    public void destroyPool() {
        try {
            genericObjectPool.close();
        } catch (Exception e) {
            LogUtil.error(LogEnum.NFS_UTIL, "销毁NFS连接失败: ", e);
        }
    }

}
