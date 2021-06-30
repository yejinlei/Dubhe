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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.stereotype.Service;

/**
 * @description 文件存储
 * @date 2020-05-09
 */
@Service
public class FileStoreServiceImpl implements IStoreService {

    /**
     * read
     *
     * @param path 文件路径
     * @return String 读取的文件
     */
    @Override
    public String read(String path) {
        try {
            return FileUtil.readUtf8String(path);
        } catch (IORuntimeException e) {
            return null;
        } catch (Exception e) {
            LogUtil.warn(LogEnum.BIZ_DATASET, "read annotation error.", e);
            return null;
        }
    }

    /**
     * write
     *
     * @param path 文件路径
     * @param content 文件目录
     * @return 更新结果
     */
    @Override
    public boolean write(String path, Object content) {
        FileUtil.writeUtf8String(String.valueOf(content), path);
        return true;
    }

    /**
     * delete
     *
     * @param fullFileOrDirPath 全文件路径
     * @return 更新结果
     */
    @Override
    public boolean delete(String fullFileOrDirPath) {
        return FileUtil.del(fullFileOrDirPath);
    }

}
