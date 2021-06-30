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

package org.dubhe.notebook.task;

import cn.hutool.core.date.DateBetween;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.handler.ScheduleTaskHandler;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.notebook.domain.entity.NoteBook;
import org.dubhe.notebook.enums.NoteBookStatusEnum;
import org.dubhe.notebook.service.NoteBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
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
        ScheduleTaskHandler.process(() -> {
            LogUtil.info(LogEnum.NOTE_BOOK, " Refresh notebook status ,start at {}", DateUtil.now());
            try {
                refreshStatus(ONE_TIME_PROCESSING_QUANTITY);
            } catch (Exception e) {
                LogUtil.error(LogEnum.NOTE_BOOK, "There is an error when refresh notebook status, exception is {}", e);
            } finally {
                LogUtil.info(LogEnum.NOTE_BOOK, "Refresh notebook status ,end at {}", DateUtil.now());
            }
        });
    }

    /**
     * 刷新状态
     *
     * @param oneTimeProcessingQuantity
     */
    private void refreshStatus(int oneTimeProcessingQuantity) {
        List<NoteBook> noteBookList = noteBookService.getList(new Page(MagicNumConstant.ZERO, oneTimeProcessingQuantity)
                , NoteBookStatusEnum.STARTING
                , NoteBookStatusEnum.STOPPING
                , NoteBookStatusEnum.DELETING
        );
        int noteBookSize = noteBookList.size();
        LogUtil.info(LogEnum.NOTE_BOOK, "The size of refreshing notebook size is ：{}", noteBookSize);
        for (int i = 0; i < noteBookSize; i++) {
            if ((i + 1) % MagicNumConstant.FIVE == MagicNumConstant.ZERO) {
                LogUtil.info(LogEnum.NOTE_BOOK, "The process of refreshing notebook is : {}/{} ", i + 1, noteBookSize);
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
        if (NoteBookStatusEnum.STARTING == statusEnum && NoteBookStatusEnum.STARTING.getCode().equals(noteBook.getStatus())) {
            long gap = new DateBetween(noteBook.getLastStartTime(), new Date()).between(DateUnit.MINUTE);
            // 超时处理
            if (gap > NumberConstant.NUMBER_30) {
                timeout(noteBook);
            }
        }
    }

    /**
     * 超时处理
     *
     * @param noteBook
     */
    private void timeout(NoteBook noteBook) {
        noteBook.setStatus(NoteBookStatusEnum.STOP.getCode());
        noteBook.setK8sStatusInfo("启动超时！");
        noteBookService.updateById(noteBook);
    }
}
