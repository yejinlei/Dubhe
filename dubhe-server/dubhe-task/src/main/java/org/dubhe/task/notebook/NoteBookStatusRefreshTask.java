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

package org.dubhe.task.notebook;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.base.ScheduleTaskHandler;
import org.dubhe.domain.entity.NoteBook;
import org.dubhe.domain.dto.NoteBookQueryDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.NoteBookStatusEnum;
import org.dubhe.service.NoteBookService;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description 定时刷新NoteBook状态 回调补偿方案Task 刷新过程中状态
 * @date 2020-04-27
 */
@Component
public class NoteBookStatusRefreshTask {

    /**
     * 设置一次性批处理数量，避免待处理状态过多导致单次处理压力过大
     */
    private static final int ONE_TIME_PROCESSING_QUANTITY = 100;

    @Autowired
    private NoteBookService noteBookService;

    /**
     * 表示每隔15秒刷新状态
     */
    @Scheduled(cron = "*/15 * * * * ?")
    public void process() {
        ScheduleTaskHandler.process(()->{
            LogUtil.info(LogEnum.NOTE_BOOK, "【回调补偿刷新NoteBook状态Task】 Start at {}", DateUtil.now());
            try {
                refreshStatus(ONE_TIME_PROCESSING_QUANTITY);
            } catch (Exception e) {
                LogUtil.error(LogEnum.NOTE_BOOK, "ERROR!【回调补偿刷新oteBook状态Task】", e);
            } finally {
                LogUtil.info(LogEnum.NOTE_BOOK, "【回调补偿刷新NoteBook状态Task】 END at {}", DateUtil.now());
            }
        });
    }

    /**
     * 刷新状态
     *
     * @param oneTimeProcessingQuantity
     */
    private void refreshStatus(int oneTimeProcessingQuantity) {
        List<NoteBook> noteBookList = noteBookService.getList(new Page(MagicNumConstant.ZERO, oneTimeProcessingQuantity), NoteBookQueryDTO.getToRefreshCriteria());
        int noteBookSize = noteBookList.size();
        LogUtil.info(LogEnum.NOTE_BOOK, "本次待处理NoteBook数量：{}", noteBookSize);
        for (int i = 0; i < noteBookSize; i++) {
            if ((i + 1) % MagicNumConstant.FIVE == MagicNumConstant.ZERO) {
                LogUtil.info(LogEnum.NOTE_BOOK, "NoteBook处理进度 {}/{} ..", i + 1, noteBookSize);
            }
            NoteBook noteBook = noteBookList.get(i);
            refreshNoteBookStatus(noteBook);
        }
    }

    /**
     * 刷新notebook状态
     *
     * @param noteBook
     */
    private void refreshNoteBookStatus(NoteBook noteBook) {
        NoteBookStatusEnum statusEnum = noteBookService.getStatus(noteBook);
        // 刷新处理
        if (noteBookService.refreshNoteBookStatus(statusEnum, noteBook)) {
            return;
        }
        // 超时处理
        timeout(noteBook);
        noteBookService.updateById(noteBook);
    }

    /**
     * 超时处理
     *
     * @param noteBook
     */
    private void timeout(NoteBook noteBook) {
        if (NoteBookStatusEnum.STARTING.getCode().equals(noteBook.getStatus())) {
            noteBook.setStatus(NoteBookStatusEnum.STOP.getCode());
            noteBook.setK8sStatusInfo("启动超时！");
        }
    }
}
