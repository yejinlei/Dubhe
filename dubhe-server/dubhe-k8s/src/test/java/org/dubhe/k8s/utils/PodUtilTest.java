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

package org.dubhe.k8s.utils;

import org.junit.Assert;
import org.junit.Test;


/**
 * @description PodUtil
 * @date 2020-09-17
 */
public class PodUtilTest {

    @Test
    public void testIsMaster() {
        Assert.assertTrue(PodUtil.isMaster("123213-master-5456"));
        Assert.assertFalse(PodUtil.isMaster(null));
        Assert.assertFalse(PodUtil.isMaster("123213-slave-5456"));
    }

    @Test
    public void testIsSlave() {
        Assert.assertFalse(PodUtil.isSlave("123213-master-5456"));
        Assert.assertFalse(PodUtil.isSlave(null));
        Assert.assertTrue(PodUtil.isSlave("123213-slave-5456"));
    }


}
