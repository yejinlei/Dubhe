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
package org.dubhe.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.dao.LogMapper;
import org.dubhe.domain.dto.LogQueryDTO;
import org.dubhe.domain.entity.Log;
import org.dubhe.service.LogService;
import org.dubhe.service.convert.LogConvert;
import org.dubhe.utils.FileUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.WrapperHelp;
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
 * @Description :日志 实现类
 * @Date 2020-06-01
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
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void save(String username, String browser, String ip, ProceedingJoinPoint joinPoint, Log log){
//
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        Method method = signature.getMethod();
//        io.swagger.annotations.ApiOperation apiOperation = method.getAnnotation(io.swagger.annotations.ApiOperation.class);
//
//        // 方法路径
//        String methodName = joinPoint.getTarget().getClass().getName()+"."+signature.getName()+"()";
//
//        StringBuilder params = new StringBuilder("{");
//        //参数值
//        Object[] argValues = joinPoint.getArgs();
//        //参数名称
//        String[] argNames = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
//        if(argValues != null){
//            for (int i = 0; i < argValues.length; i++) {
//                params.append(" ").append(argNames[i]).append(": ").append(argValues[i]);
//            }
//        }
//        // 描述
//        if (log != null) {
//            log.setDescription(apiOperation.value());
//        }
//        assert log != null;
//        log.setRequestIp(ip);
//
//        String loginPath = "login";
//        if(loginPath.equals(signature.getName())){
//            try {
//                assert argValues != null;
//                username = new JSONObject(argValues[0]).get("username").toString();
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        log.setMethod(methodName);
//        log.setUsername(username);
//        log.setParams(params.toString() + " }");
//        log.setBrowser(browser);
//        logMapper.updateById(log);
//    }

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
     * @return void
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
        FileUtil.downloadExcel(list, response);
    }

    /**
     * 删除所有error日志
     *
     * @param
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByError() {
        logMapper.deleteByLogType("ERROR");
    }

    /**
     * 删除所有info日志
     *
     * @param
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByInfo() {
        logMapper.deleteByLogType("INFO");
    }
}
