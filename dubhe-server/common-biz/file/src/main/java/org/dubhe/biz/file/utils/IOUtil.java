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

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @description IO流操作工具类
 * @date 2020-10-14
 */
public class IOUtil {

    /**
     * 循环的依次关闭流
     *
     * @param closeableList 要被关闭的流集合
     */
    public static void close(Closeable... closeableList) {
        for (Closeable closeable : closeableList) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (IOException e) {
                LogUtil.error(LogEnum.IO_UTIL, "关闭流异常，异常信息：{}", e);
            }
        }
    }

    /**
     * 将input流转换为文件
     *
     * @param is 输入流 调用方负责关闭
     * @param targetFile 目标文件
     */
    public static void copy(InputStream is, File targetFile) {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] b = new byte[1024];
            int readCount = is.read(b);
            while (readCount != -1) {
                // 写入数据
                fos.write(b, 0, readCount);
                readCount = is.read(b);
            }
            is.close();
            fos.flush();
        } catch (IOException e) {
            LogUtil.error(LogEnum.IO_UTIL,"copy file error:【{}】", e);
        }
    }
}
