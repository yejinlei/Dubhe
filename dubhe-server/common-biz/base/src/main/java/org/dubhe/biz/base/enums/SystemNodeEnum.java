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
package org.dubhe.biz.base.enums;

/**
 * @description 节点枚举类
 * @date 2020-07-08
 */
public enum SystemNodeEnum {
    /**
     * 网络资源
     */
    NETWORK("NetworkUnavailable", "网络资源不足"),
    /**
     * 内存资源
     */
    MEMORY("MemoryPressure", "内存资源不足"),
    /**
     * 磁盘资源
     */
    DISK("DiskPressure", "磁盘资源不足"),
    /**
     * 进程资源
     */
    PROCESS("PIDPressure", "进程资源不足");

    private String type;
    private String message;

    SystemNodeEnum(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 根据type查询message信息
     *
     * @param systemNodeType 节点类型
     * @return String message信息
     */
    public static String findMessageByType(String systemNodeType) {
        SystemNodeEnum[] systemNodeEnums = SystemNodeEnum.values();
        for (SystemNodeEnum systemNodeEnum : systemNodeEnums) {
            if (systemNodeType.equals(systemNodeEnum.getType())) {
                return systemNodeEnum.getMessage();
            }
        }
      return null;
    }
}
