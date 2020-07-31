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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @description
 * @create: 2020-05-07
 */
public class ReadFileTest {

    @Test
    public void testRead(){
        String path = "/Users/lizhe/hello2.log";
        List<String> ans = Arrays.asList("lin1", "l2", "l3", "l4");
        List<String> ans2 = ans.subList(1, 3);
        assertEquals(ans, ReadFile.read(path,-2, 10));
        assertEquals(ans2, ReadFile.read(path, 1, 2));
    }
}
