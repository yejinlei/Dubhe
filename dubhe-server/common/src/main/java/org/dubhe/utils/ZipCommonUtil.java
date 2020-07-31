/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.utils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.dubhe.enums.LogEnum;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @description zip Util
 * @date 2020-5-22
 */
@Component
public class ZipCommonUtil {
    public static boolean unzipLocal(String source) {
        if (source == null) {
            return false;
        }
        String[] sourceArr = source.split(Pattern.quote(File.separator));
        int length = sourceArr.length;
        if (length <= 1) {
            return false;
        }
        List<String> destList = new ArrayList<>(Arrays.asList(sourceArr).subList(0, length - 1));
        String destination = String.join(File.separator, destList);

        try {
            ZipFile zipFile = new ZipFile(source);
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "解压失败: ", e);
            return false;
        }
        return true;
    }
}
