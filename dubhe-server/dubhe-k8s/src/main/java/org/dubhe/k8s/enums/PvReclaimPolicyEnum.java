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

package org.dubhe.k8s.enums;

/**
 * @description PV回收策略枚举
 * @date 2020-07-01
 */
public enum PvReclaimPolicyEnum {
    /**
     * 删除PV时，删除数据，只有 NFS 和 HostPath 支持
     */
    RECYCLE("Recycle"),
    /**
     * 不清理, 保留 Volume（需要手动清理）
     */
    RETAIN("Retain"),
    /**
     * 删除存储资源，比如删除 AWS EBS 卷（只有 AWS EBS, GCE PD, Azure Disk 和 Cinder 支持）
     */
    DELETE("Delete"),
    ;

    private String policy;

    PvReclaimPolicyEnum(String policy) {
        this.policy = policy;
    }

    public String getPolicy(){
        return policy;
    }
}
