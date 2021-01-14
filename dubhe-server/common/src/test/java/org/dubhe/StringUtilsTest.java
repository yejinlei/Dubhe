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

package org.dubhe;

import org.dubhe.utils.StringUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.dubhe.utils.StringUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
/**
 * @description  测试类
 * @date 2020-03-25
 */
public class StringUtilsTest {

    @Test
    public void testToCamelCase() {
        assertNull(toCamelCase(null));
    }

    @Test
    public void testToCapitalizeCamelCase() {
        assertNull(StringUtils.toCapitalizeCamelCase(null));
        assertEquals("HelloWorld", toCapitalizeCamelCase("hello_world"));
    }

    @Test
    public void testGetWeekDay() {
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
        assertEquals(simpleDateformat.format(new Date()), getWeekDay());
    }

    @Test
    public void testGetIp() {
        assertEquals("127.0.0.1", getIp(new MockHttpServletRequest()));
    }

    @Test
    public void truncationString() {
        assertEquals("", StringUtils.truncationString(null,6));
        assertEquals("012345", StringUtils.truncationString("012345",0));
        assertEquals("0", StringUtils.truncationString("012345",1));
        assertEquals("012345", StringUtils.truncationString("012345",6));
    }
}
