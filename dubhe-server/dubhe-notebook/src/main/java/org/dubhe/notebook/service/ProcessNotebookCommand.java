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

package org.dubhe.notebook.service;

import org.dubhe.notebook.domain.entity.NoteBook;

/**
 * @description 处理notebook生命周期的回调函数
 * @date 2020-04-28
 */
public class ProcessNotebookCommand {

    /**
     * notebook由启动中切换为启动成功
     * 数据库更新之前获取notebook信息进行业务处理
     * @param noteBook
     */
    public void running(NoteBook noteBook) {
    }

    /**
     * notebook由停止中切换为停止成功
     * 数据库更新数据之前获取notebook信息进行业务处理
     * @param noteBook
     */
    public void stop(NoteBook noteBook){

    }

    /**
     * notebook由删除中切换为删除成功
     * 数据库删除数据之前获取notebook信息进行业务处理
     * @param noteBook
     */
    public void delete(NoteBook noteBook) {
    }


}
