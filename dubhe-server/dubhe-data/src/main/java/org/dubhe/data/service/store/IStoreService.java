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

package org.dubhe.data.service.store;

/**
 * @description 文件存储
 * @date 2020-05-09
 */
public interface IStoreService {

    /**
     * read
     *
     * @param path 文件路径
     * @return 路径
     */
    String read(String path);

    /**
     * write
     *
     * @param path 文件路径
     * @param content 文件目录
     * @return 更新结果
     */
    boolean write(String path, Object content);

    /**
     * delete
     *
     * @param fullFileOrDirPath 全文件路径
     * @return 更新结果
     */
    boolean delete(String fullFileOrDirPath);

}
