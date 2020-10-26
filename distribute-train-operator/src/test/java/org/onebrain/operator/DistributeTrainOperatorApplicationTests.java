/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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


package org.onebrain.operator;

import org.onebrain.operator.api.pod.PodApi;
import org.onebrain.operator.constants.KubeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@SpringBootTest
public class DistributeTrainOperatorApplicationTests {

    @Autowired
    private PodApi podApi;

//    @Test
    public void contextLoads() throws URISyntaxException {
        final URL url = getClass().getClassLoader().getResource("key/id_rsa");
        File file = new File(url.toURI());
        podApi.copyToPod("default", "distribute-train-test-job-sv2dj", KubeConstants.MASTER_CONTAINER_NAME, file, "/root/.ssh/id_rsa");
    }

}
