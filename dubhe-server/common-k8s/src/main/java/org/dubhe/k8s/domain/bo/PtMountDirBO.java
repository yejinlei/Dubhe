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

package org.dubhe.k8s.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description 挂载路径参数
 * @date 2020-06-30
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class PtMountDirBO {
    /**挂载的路径，绝对路径**/
    private String dir;
    /**是否只读 ture:是 false:否**/
    private boolean readOnly;
    /**是否回收  true:创建pv、pvc进行挂载,删除时同时删除数据 false且request和limit均为空:直接挂载**/
    private boolean recycle;
    /**存储配额 示例：500Mi* 仅在pvc=true时生效*/
    private String request;
    /**存储限额 示例：500Mi 仅在pvc=true时生效**/
    private String limit;

    public PtMountDirBO(String dir){
        this.dir = dir;
    }

    public PtMountDirBO(String dir, String request){
        this.dir = dir;
        this.request = request;
    }

    public PtMountDirBO(String dir, boolean readOnly){
        this.dir = dir;
        this.readOnly = readOnly;
    }

    public PtMountDirBO(String dir, String request,boolean readOnly){
        this.dir = dir;
        this.request = request;
        this.readOnly = readOnly;
    }

    public PtMountDirBO(String dir, String request, String limit,boolean readOnly){
        this.dir = dir;
        this.request = request;
        this.limit = limit;
        this.readOnly = readOnly;
    }
}
