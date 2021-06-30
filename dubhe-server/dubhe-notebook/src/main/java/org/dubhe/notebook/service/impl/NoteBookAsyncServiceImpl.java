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

package org.dubhe.notebook.service.impl;

import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.abstracts.AbstractPodCallback;
import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;
import org.dubhe.k8s.service.PodCallbackAsyncService;
import org.dubhe.notebook.dao.NoteBookMapper;
import org.dubhe.notebook.domain.entity.NoteBook;
import org.dubhe.notebook.enums.NoteBookStatusEnum;
import org.dubhe.notebook.service.NoteBookService;
import org.dubhe.notebook.service.ProcessNotebookCommand;
import org.dubhe.notebook.utils.NotebookUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description 异步刪除接口实现
 * @date 2020-05-28
 */
@Service(value = "noteBookAsyncServiceImpl")
public class NoteBookAsyncServiceImpl<NotebookK8sPodCallbackCreateDTO> extends AbstractPodCallback implements PodCallbackAsyncService {

    @Autowired
    private NoteBookService noteBookService;

    @Autowired
    private NoteBookMapper noteBookMapper;


    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> boolean doCallback(int times, R notebookK8sPodCallbackCreateDTO) {
        try {
            // 强制转型
            LogUtil.info(LogEnum.NOTE_BOOK, "NoteBookAsyncServiceImpl try {} time. Request: {}", times, notebookK8sPodCallbackCreateDTO.toString());
            NoteBook notebook = noteBookMapper.findByNamespaceAndResourceName(notebookK8sPodCallbackCreateDTO.getNamespace(), notebookK8sPodCallbackCreateDTO.getResourceName());
            if (notebook == null) {
                LogUtil.warn(LogEnum.NOTE_BOOK, "Cannot find notebook! Request: {}", notebookK8sPodCallbackCreateDTO);
                return true;
            }
            NoteBookStatusEnum statusEnum = NoteBookStatusEnum.convert(notebookK8sPodCallbackCreateDTO.getPhase());

            noteBookService.refreshNoteBookStatus(statusEnum, notebook, new ProcessNotebookCommand() {
                @Override
                public void running(NoteBook noteBook) {
                    notebook.setK8sStatusCode(notebookK8sPodCallbackCreateDTO.getPhase());
                    notebook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(notebookK8sPodCallbackCreateDTO.getMessages()));
                    if (StringUtils.isEmpty(notebookK8sPodCallbackCreateDTO.getMessages())){
                        notebook.removeStatusDetail(notebookK8sPodCallbackCreateDTO.getResourceName());
                    }else {
                        notebook.putStatusDetail(notebookK8sPodCallbackCreateDTO.getResourceName(),notebookK8sPodCallbackCreateDTO.getMessages());
                    }
                }
            });

            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.NOTE_BOOK, "NoteBook doCallback error! Exception is {}", e);
            return false;
        }
    }

    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> void callbackFailed(int retryTimes, R notebookK8sPodCallbackCreateDTO) {
        // 强制转型
        LogUtil.info(LogEnum.NOTE_BOOK, "Thread {} try {} times failed! if you want to storage or send failed msg,please impl this.. Request: {}", Thread.currentThread(), retryTimes, notebookK8sPodCallbackCreateDTO.toString());
        // 目前利用定时补偿补充处理，无需做callbackFailed
    }
}
