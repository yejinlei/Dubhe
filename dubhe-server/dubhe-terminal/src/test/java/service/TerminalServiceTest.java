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

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.k8s.api.TerminalApi;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.bo.TerminalBO;
import org.dubhe.terminal.TerminalApplication;
import org.dubhe.terminal.dao.TerminalMapper;
import org.dubhe.terminal.domain.dto.TerminalCreateDTO;
import org.dubhe.terminal.domain.dto.TerminalDetailDTO;
import org.dubhe.terminal.domain.dto.TerminalInfoDTO;
import org.dubhe.terminal.domain.entity.Terminal;
import org.dubhe.terminal.domain.vo.TerminalVO;
import org.dubhe.terminal.service.TerminalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description TerminalService测试类
 * @date 2020-07-20
 */
@SpringBootTest(classes= TerminalApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class TerminalServiceTest {
    @Autowired
    private TerminalService terminalService;

    @Autowired
    private TerminalApi terminalApi;

    @Autowired
    private TerminalMapper terminalMapper;

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
        terminalCreateDTO.setInfo(Lists.newArrayList(new TerminalInfoDTO(null,1,0,1024,1024,"nvidia","v100","nvidia.com/gpu")));
        terminalService.create(terminalCreateDTO);
    }

    @Test
    public void createTerminal(){
        TerminalBO bo = new TerminalBO();
        bo.setNamespace("namespace-6");
        bo.setResourceName("terminal-rn-4pvma0");
        bo.setReplicas(1);
        bo.setGpuNum(0);
        bo.setMemNum(1024);
        bo.setCpuNum(1000);
        bo.setUseGpu(false);
        bo.setImage("harbor.dubhe.ai/train/oneflow:cudnn7-py36-of010-yolov3");

        PtMountDirBO ptMountDirBO = new PtMountDirBO("/nfs/dubhe-cloud-dev/terminal/6/workspace");
        ptMountDirBO.setDir("/nfs/dubhe-cloud-dev/terminal/6/workspace");
        ptMountDirBO.setReadOnly(false);
        ptMountDirBO.setRecycle(false);
        ptMountDirBO.setRequest("102400Mi");
        ptMountDirBO.setLimit("102400Mi");
        Map<String, PtMountDirBO> fsMounts = new HashMap<>();
        fsMounts.put("/workspace",ptMountDirBO);

        bo.setFsMounts(fsMounts);
        bo.setBusinessLabel("terminal");
        bo.setTaskIdentifyLabel("4b4e74d035504637ac83438ab1efe7ff");
        bo.setPorts(Sets.newHashSet(22));
        bo.setImagePullPolicy("Always");


        System.out.println(JSON.toJSONString(terminalApi.create(bo)));
    }

    @Test
    public void derminal(){
        terminalApi.delete("namespace-6","terminal-rn-4pvma0");
    }



    @Test
    public void listWithK8sStatus() {
        LambdaQueryWrapper<Terminal> wrapper = new LambdaQueryWrapper<>();
        Long testUserId=3l;
        wrapper.eq(Terminal::getCreateUserId, testUserId);
        List<Terminal> terminals = terminalMapper.selectList(wrapper);
        terminalService.refreshTerminalStatus(terminals.stream().map(obj->obj.getId()).collect(Collectors.toList()));
        List<TerminalVO> treminals =terminalService.listVO(terminals);

    }

    @Test
    public void updateTerminalDescription(){
       terminalService.update(new TerminalDetailDTO((long) 424,"1234"));
    }
}
