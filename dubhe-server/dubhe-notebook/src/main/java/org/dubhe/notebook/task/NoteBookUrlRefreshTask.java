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

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.handler.ScheduleTaskHandler;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.notebook.domain.entity.NoteBook;
import org.dubhe.notebook.service.NoteBookService;
import org.dubhe.notebook.utils.NotebookUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @description 定时刷新notebook url
 * @date 2020-06-30
 */
@Component
@Slf4j
public class NoteBookUrlRefreshTask {

    /**
     * 设置一次性批处理数量，避免待处理状态过多导致单次处理压力过大
     */
    private static final int ONE_TIME_PROCESSING_QUANTITY = 100;

    @Autowired
    private NoteBookService noteBookService;

    /**
     * 表示每隔2秒刷新URL
     */
    @Scheduled(cron = "*/2 * * * * ?")
    public void process() {
        ScheduleTaskHandler.process(() -> {
            LogUtil.info(LogEnum.NOTE_BOOK, "【刷新NoteBook URL 任务】 Start at {}", DateUtil.now());
            try {
                refreshUrl(ONE_TIME_PROCESSING_QUANTITY);
            } catch (Exception e) {
                LogUtil.error(LogEnum.NOTE_BOOK, "ERROR!【刷新NoteBook URL 任务】", e);
            } finally {
                LogUtil.info(LogEnum.NOTE_BOOK, "【刷新NoteBook URL 任务】 END at {}", DateUtil.now());
            }
        });
    }

    /**
     * 刷新URL
     *
     * @param oneTimeProcessingQuantity
     */
    private void refreshUrl(int oneTimeProcessingQuantity) {
        List<NoteBook> noteBookList = noteBookService.getRunNotUrlList(new Page(MagicNumConstant.ZERO, oneTimeProcessingQuantity));
        if (!CollectionUtils.isEmpty(noteBookList)) {
            int noteBookSize = noteBookList.size();
            LogUtil.info(LogEnum.NOTE_BOOK, "本次待处理NoteBook数量：{}", noteBookSize);
            for (int i = 0; i < noteBookSize; i++) {
                if ((i + 1) % MagicNumConstant.FIVE == MagicNumConstant.ZERO) {
                    LogUtil.info(LogEnum.NOTE_BOOK, "NoteBook处理进度 {}/{}", (i + 1), noteBookSize);
                }
                NoteBook noteBook = noteBookList.get(i);
                refreshNoteBookUrl(noteBook);
            }
        }
    }

    /**
     * 刷新notebook
     *
     * @param noteBook
     */
    private void refreshNoteBookUrl(NoteBook noteBook) {
        String jupyterUrlWithToken = noteBookService.getJupyterUrl(noteBook);
        if (NotebookUtil.checkUrlContainsToken(jupyterUrlWithToken)) {
            noteBook.setUrl(jupyterUrlWithToken);
            noteBookService.updateById(noteBook);
        }
    }

}
