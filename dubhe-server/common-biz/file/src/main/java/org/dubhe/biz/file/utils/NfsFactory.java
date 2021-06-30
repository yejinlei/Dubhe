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
import com.emc.ecs.nfsclient.rpc.CredentialUnix;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.dubhe.biz.file.config.NfsConfig;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @description NFS工厂类
 * @date 2020-05-13
 */
@Deprecated
@Component
public class NfsFactory implements PooledObjectFactory<Nfs3> {

    private final NfsConfig nfsConfig;

    public NfsFactory(NfsConfig nfsConfig) {
        this.nfsConfig = nfsConfig;
    }

    @Override
    public PooledObject<Nfs3> makeObject() {
        Nfs3 nfs3 = null;
        try {
            nfs3 = new Nfs3(nfsConfig.getNfsIp(), nfsConfig.getRootDir(), new CredentialUnix(0, 0, null), 3);
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "创建NFS对象失败: ", e);
        }
        return new DefaultPooledObject<>(nfs3);
    }

    @Override
    public void destroyObject(PooledObject<Nfs3> pooledObject) {
        LogUtil.info(LogEnum.NFS_UTIL, "销毁NFS对象: ", pooledObject.getObject());
    }

    @Override
    public boolean validateObject(PooledObject<Nfs3> pooledObject) {
        LogUtil.info(LogEnum.NFS_UTIL, "验证NFS对象: ", pooledObject.getObject());
        return true;
    }

    @Override
    public void activateObject(PooledObject<Nfs3> pooledObject) {
        LogUtil.info(LogEnum.NFS_UTIL, "激活NFS对象: ", pooledObject.getObject());

    }

    @Override
    public void passivateObject(PooledObject<Nfs3> pooledObject) {
        LogUtil.info(LogEnum.NFS_UTIL, "钝化NFS对象: ", pooledObject.getObject());
    }
}
