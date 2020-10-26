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

package org.dubhe;

import org.junit.Assert;
import org.junit.Test;

import static org.dubhe.utils.HttpUtils.isSuccess;

/**
 * @description HttpUtil
 * @date 2020-04-30
 */
public class HttpUtilsTest {

    @Test
    public void testIsSuccess() {
        Assert.assertTrue(isSuccess("200"));
        Assert.assertFalse(isSuccess("2020"));
        Assert.assertFalse(isSuccess("401"));
        Assert.assertFalse(isSuccess(null));
        Assert.assertFalse(isSuccess(""));
        Assert.assertTrue(isSuccess(200));
    }


}
