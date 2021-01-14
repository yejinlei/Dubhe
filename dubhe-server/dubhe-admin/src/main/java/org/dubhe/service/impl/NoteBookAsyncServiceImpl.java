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

package org.dubhe.service.impl;

import org.dubhe.dao.NoteBookMapper;
import org.dubhe.domain.entity.NoteBook;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.NoteBookStatusEnum;
import org.dubhe.dto.callback.BaseK8sPodCallbackCreateDTO;
import org.dubhe.dto.callback.NotebookK8sPodCallbackCreateDTO;
import org.dubhe.service.NoteBookService;
import org.dubhe.service.PodCallbackAsyncService;
import org.dubhe.service.abstracts.AbstractPodCallback;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.NotebookUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description 异步刪除接口实现
 * @date 2020-05-28
 */
@Service(value = "noteBookAsyncServiceImpl")
public class NoteBookAsyncServiceImpl extends AbstractPodCallback implements PodCallbackAsyncService {

    @Autowired
    private NoteBookService noteBookService;

    @Autowired
    private NoteBookMapper noteBookMapper;


    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> boolean doCallback(int times, R k8sPodCallbackCreateDTO) {
        try {
            // 强制转型
            NotebookK8sPodCallbackCreateDTO req = (NotebookK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
            LogUtil.info(LogEnum.NOTE_BOOK, "NoteBookAsyncServiceImpl try {} time.Request: {}", times, req.toString());
            NoteBook notebook = noteBookMapper.findByNamespaceAndResourceName(req.getNamespace(), req.getResourceName(),NoteBookStatusEnum.DELETE.getCode());
            if (notebook == null) {
                LogUtil.warn(LogEnum.NOTE_BOOK, "Cannot find notebook! Request: {}", Thread.currentThread(), times, req.toString());
                return true;
            }
            NoteBookStatusEnum statusEnum = NoteBookStatusEnum.convert(k8sPodCallbackCreateDTO.getPhase());
            if (noteBookService.refreshNoteBookStatus(statusEnum, notebook)) {
                return true;
            }
            notebook.setK8sStatusCode(req.getPhase());
            notebook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(req.getMessages()));
            noteBookService.updateById(notebook);
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.NOTE_BOOK, "NoteBook doCallback error!{}", e);
            return false;
        }
    }

    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> void callbackFailed(int retryTimes, R k8sPodCallbackCreateDTO) {
        // 强制转型
        NotebookK8sPodCallbackCreateDTO req = (NotebookK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.NOTE_BOOK, "Thread {}try {} times FAILED! if you want to storage or send failed msg,please impl this.. Request: {}", Thread.currentThread(), retryTimes, req.toString());
        // 目前利用定时补偿补充处理，无需做callbackFailed
    }
}
