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

package org.dubhe.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 读文件工具类
 * @date 2020-5-6
 */
public class ReadFile {

    public static List<String> read(String path, Integer start, Integer length){
        if (start < 0) {
            start = 0;
        }
        List<String> ansList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            for (int i = 0; i < start+length; i++) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                if (start <= i && i < start+length) {
                    ansList.add(line);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return ansList;
        }
        return ansList;
    }

}
