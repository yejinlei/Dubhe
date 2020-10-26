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

package org.dubhe.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.dao.PtDevEnvsMapper;
import org.dubhe.domain.PtDevEnvs;
import org.dubhe.domain.dto.PtDevEnvsDTO;
import org.dubhe.domain.dto.PtDevEnvsQueryCriteria;
import org.dubhe.service.PtDevEnvsService;
import org.dubhe.service.convert.PtDevEnvsConvert;
import org.dubhe.utils.FileUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.WrapperHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @description  开发环境
 * @date 2020-03-17
 */
@Service
@CacheConfig(cacheNames = "ptDevEnvs")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class PtDevEnvsServiceImpl implements PtDevEnvsService {

    @Autowired
    private PtDevEnvsMapper ptDevEnvsMapper;
    private PtDevEnvsConvert ptDevEnvsConvert;


    @Override
    @Cacheable
    public Map<String, Object> queryAll(PtDevEnvsQueryCriteria criteria, Page page) {
        IPage<PtDevEnvs> ptDevEnvss = ptDevEnvsMapper.selectPage(page, WrapperHelp.getWrapper(criteria));
        return PageUtil.toPage(ptDevEnvss, ptDevEnvsConvert::toDto);
    }

    @Override
    @Cacheable
    public List<PtDevEnvsDTO> queryAll(PtDevEnvsQueryCriteria criteria) {
        return ptDevEnvsConvert.toDto(ptDevEnvsMapper.selectList(WrapperHelp.getWrapper(criteria)));
    }

    @Override
    @Cacheable(key = "#p0")
    public PtDevEnvsDTO findById(Long id) {
        PtDevEnvs ptDevEnvs = ptDevEnvsMapper.selectById(id);
        return ptDevEnvsConvert.toDto(ptDevEnvs);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public PtDevEnvsDTO create(PtDevEnvs resources) {
        ptDevEnvsMapper.insert(resources);
        return ptDevEnvsConvert.toDto(resources);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void update(PtDevEnvs resources) {
        PtDevEnvs ptDevEnvs = ptDevEnvsMapper.selectById(resources.getId());
        ptDevEnvs.copy(resources);
        ptDevEnvsMapper.updateById(ptDevEnvs);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            ptDevEnvsMapper.deleteById(id);
        }
    }

    @Override
    public void download(List<PtDevEnvsDTO> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PtDevEnvsDTO ptDevEnvs : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(" name", ptDevEnvs.getName());
            map.put(" remark", ptDevEnvs.getRemark());
            map.put(" podnum", ptDevEnvs.getPodNum());
            map.put(" gpunum", ptDevEnvs.getGpuNum());
            map.put(" memnum", ptDevEnvs.getMemNum());
            map.put(" cpunum", ptDevEnvs.getCpuNum());
            map.put(" duration", ptDevEnvs.getDuration());
            map.put(" startTime", ptDevEnvs.getStartTime());
            map.put(" closeTime", ptDevEnvs.getCloseTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
