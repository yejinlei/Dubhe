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

package org.dubhe.data.service;


import cn.hutool.core.lang.UUID;
import org.dubhe.BaseTest;
import org.dubhe.data.domain.dto.AutoAnnotationCreateDTO;
import org.dubhe.data.domain.dto.DatasetCreateDTO;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.domain.vo.ProgressVO;
import org.dubhe.data.service.http.AnnotationHttpService;
import org.dubhe.data.service.impl.DatasetServiceImpl;
import org.dubhe.data.service.impl.FileServiceImpl;
import org.dubhe.data.service.impl.TaskServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

/**
 * @description 任务业务层测试类
 * @date 2020-04-20
 */
public class TaskServiceServiceImplTest extends BaseTest {

    @Autowired
    public TaskServiceImpl taskService;
    @Autowired
    private DatasetServiceImpl datasetService;
    @Autowired
    private FileServiceImpl fileService;
    @MockBean
    private AnnotationHttpService annotationHttpService;

    public static final int FILE_SIZE = 100;

    public static long dsId = 0;
    public static Collection<String> TASK_IDS = new HashSet<>();

    @Before
    public void init() {
        Answer<String> answer = invocation -> {
            String id = UUID.fastUUID().toString();
            TASK_IDS.add(id);
            return id;
        };

        Mockito.when(annotationHttpService.annotate(Mockito.any())).then(answer);

        dsId = initDataset();
        addFile(dsId);
    }

    private void addFile(long datasetId) {
        List<File> files = new LinkedList<>();
        for (int i = 0; i < FILE_SIZE; i++) {
            File f = File.builder().datasetId(datasetId).name(String.valueOf(i)).build();
            files.add(f);
        }
        fileService.saveBatch(files);
    }

    private long initDataset() {
        DatasetCreateDTO d1 = new DatasetCreateDTO();
        d1.setName(UUID.fastUUID().toString());
        return datasetService.create(d1);
    }

    @Test
    public void create() {
        Long[] ids = new Long[]{dsId};
        List<Long> idList = Arrays.asList(dsId);

        taskService.auto(AutoAnnotationCreateDTO.builder()
                .datasetIds(ids)
                .build());

        Map<Long, ProgressVO> progress = fileService.listStatistics(idList);
        System.out.println(progress);
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        if (progress.get(dsId).getUnfinished() == 0) {
            return;
        }
    }

    @Test
    public void fail() {
        taskService.fail();
    }

}
