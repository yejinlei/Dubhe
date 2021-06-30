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
package org.dubhe.admin.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.admin.dao.LogMapper;
import org.dubhe.admin.domain.dto.LogQueryDTO;
import org.dubhe.admin.domain.entity.Log;
import org.dubhe.admin.service.LogService;
import org.dubhe.admin.service.convert.LogConvert;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.utils.DubheFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 日志 实现类
 * @date 2020-06-01
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogConvert logConvert;

    @Autowired
    private LogMapper logMapper;

    /**
     * 分页查询日志
     *
     * @param criteria 日志查询实体
     * @param page     分页实体
     * @return java.lang.Object 日志返回实例
     */
    @Override
    public Object queryAll(LogQueryDTO criteria, Page page) {
        IPage<Dict> dicts = logMapper.selectPage(page, WrapperHelp.getWrapper(criteria));
        String status = "ERROR";
        if (status.equals(criteria.getLogType())) {
            return PageUtil.toPage(dicts, logConvert::toDto);
        }
        return page;
    }

    /**
     * 查询日志列表
     *
     * @param criteria 日志查询条件
     * @return java.util.List<org.dubhe.domain.entity.Log> 日志返回实例
     */
    @Override
    public List<Log> queryAll(LogQueryDTO criteria) {
        return logMapper.selectList(WrapperHelp.getWrapper(criteria));
    }

    /**
     * 分页查询日志
     *
     * @param criteria 日志查询实体
     * @param page     分页实体
     * @return java.lang.Object 日志返回实例
     */
    @Override
    public Object queryAllByUser(LogQueryDTO criteria, Page page) {
        Page<Log> logs = logMapper.selectPage(page, WrapperHelp.getWrapper(criteria));
        return PageUtil.toPage(logs, logConvert::toDto);
    }

    /**
     * 根据id查询错误日志
     *
     * @param id
     * @return java.lang.Object
     */
    @Override
    public Object findByErrDetail(Long id) {
        Log log = logMapper.selectById(id);
        byte[] details = log.getExceptionDetail();
        return Dict.create().set("exception", new String(ObjectUtil.isNotNull(details) ? details : "".getBytes()));
    }

    /**
     * 日志信息导出
     *
     * @param logs     日志导出列表
     * @param response
     */
    @Override
    public void download(List<Log> logs, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Log log : logs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", log.getUsername());
            map.put("IP", log.getRequestIp());
            map.put("描述", log.getDescription());
            map.put("浏览器", log.getBrowser());
            map.put("请求耗时/毫秒", log.getTime());
            map.put("异常详情", new String(ObjectUtil.isNotNull(log.getExceptionDetail()) ? log.getExceptionDetail() : "".getBytes()));
            map.put("创建日期", log.getCreateTime());
            list.add(map);
        }
        DubheFileUtil.downloadExcel(list, response);
    }

    /**
     * 删除所有error日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByError() {
        logMapper.deleteByLogType("ERROR");
    }

    /**
     * 删除所有info日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByInfo() {
        logMapper.deleteByLogType("INFO");
    }
}
