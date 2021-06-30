/**
 * Copyright 2019-2020 Zheng Jie
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
 */
package org.dubhe.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.admin.domain.dto.LogQueryDTO;
import org.dubhe.admin.domain.entity.Log;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @description 日志服务 Service
 * @date 2020-06-01
 */
public interface LogService {

    /**
     * 分页查询
     *
     * @param criteria 查询条件
     * @param page     分页参数
     * @return Object 分页查询响应
     */
    Object queryAll(LogQueryDTO criteria, Page page);

    /**
     * 查询全部数据
     *
     * @param criteria 查询条件
     * @return 日志列表
     */
    List<Log> queryAll(LogQueryDTO criteria);

    /**
     * 查询用户日志
     *
     * @param criteria 查询条件
     * @param page     分页参数
     * @return 日志
     */
    Object queryAllByUser(LogQueryDTO criteria, Page page);

    /**
     * 保存日志数据
     * @param username 用户
     * @param browser 浏览器
     * @param ip 请求IP
     * @param joinPoint /
     * @param log 日志实体
     */
    //@Async
    //void save(String username, String browser, String ip, ProceedingJoinPoint joinPoint, Log log);

    /**
     * 查询异常详情
     *
     * @param id 日志ID
     * @return Object 日志详情
     */
    Object findByErrDetail(Long id);

    /**
     * 导出日志
     *
     * @param logs     待导出的数据
     * @param response 导出http响应
     * @throws IOException 导出异常
     */
    void download(List<Log> logs, HttpServletResponse response) throws IOException;

    /**
     * 删除所有错误日志
     */
    void delAllByError();

    /**
     * 删除所有INFO日志
     */
    void delAllByInfo();
}
