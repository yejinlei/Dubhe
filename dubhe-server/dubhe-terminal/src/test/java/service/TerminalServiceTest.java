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

package service;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.dubhe.terminal.TerminalApplication;
import org.dubhe.terminal.domain.dto.TerminalCreateDTO;
import org.dubhe.terminal.domain.dto.TerminalInfoDTO;
import org.dubhe.terminal.service.TerminalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description TerminalService测试类
 * @date 2020-07-20
 */
@SpringBootTest(classes= TerminalApplication.class)
@RunWith(SpringRunner.class)
public class TerminalServiceTest {
    @Autowired
    private TerminalService terminalService;

    @Test
    public void create() {
        TerminalCreateDTO terminalCreateDTO = new TerminalCreateDTO();
        terminalCreateDTO.setName("terminal-test");
        terminalCreateDTO.setDataSourceName("测试数据集");
        terminalCreateDTO.setDataSourcePath("dataset/2/versionFile/V0001/ofrecord/train");
        terminalCreateDTO.setImageTag("oneflow-0.1.102-py36-0713");
        terminalCreateDTO.setImageName("jupyterlab");
        terminalCreateDTO.setImageUrl("notebook/jupyterlab:oneflow-0.1.102-py36-0713");
        terminalCreateDTO.setTotalNode(2);
        terminalCreateDTO.setDescription("terminal-test");
        terminalCreateDTO.setSameInfo(true);
        terminalCreateDTO.setPorts(Sets.newHashSet(80,443));
        terminalCreateDTO.setInfo(Lists.newArrayList(new TerminalInfoDTO(null,1,0,1024,1024)));
        terminalService.create(terminalCreateDTO);
    }
}
