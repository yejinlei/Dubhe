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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.dao.NoteBookMapper;
import org.dubhe.dao.NoteBookModelMapper;
import org.dubhe.domain.dto.NoteBookListQueryDTO;
import org.dubhe.domain.dto.NoteBookQueryDTO;
import org.dubhe.domain.dto.NoteBookStatusDTO;
import org.dubhe.domain.dto.SourceNoteBookDTO;
import org.dubhe.domain.entity.NoteBook;
import org.dubhe.domain.entity.NoteBookModel;
import org.dubhe.domain.vo.NoteBookVO;
import org.dubhe.enums.*;
import org.dubhe.exception.NotebookBizException;
import org.dubhe.harbor.api.HarborApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.JupyterResourceApi;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.vo.PtJupyterDeployVO;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.service.HarborProjectService;
import org.dubhe.service.NoteBookService;
import org.dubhe.service.convert.NoteBookConvert;
import org.dubhe.service.convert.PtJupyterResourceConvert;
import org.dubhe.utils.HttpUtils;
import org.dubhe.utils.K8sNameTool;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.NotebookUtil;
import org.dubhe.utils.NumberUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.WrapperHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description notebook服务实现
 * @date 2020-04-28
 */
@Service
public class NoteBookServiceImpl implements NoteBookService {

    @Autowired
    private NoteBookMapper noteBookMapper;

    @Autowired
    private NoteBookModelMapper noteBookModelMapper;

    @Autowired
    private NoteBookConvert noteBookConvert;

    @Autowired
    private JupyterResourceApi jupyterResourceApi;

    @Autowired
    private PodApi podApi;

    @Autowired
    private NamespaceApi namespaceApi;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private HarborApi harborApi;

    @Autowired
    private HarborProjectService harborProjectService;

    @Value("${delay.notebook.delete}")
    private Integer notebookDelayDeleteTime;

    private static final String BLANK = SymbolConstant.BLANK;

    /**
     * 分页查询所有 notebook 记录
     *
     * @param page
     * @param noteBookListQueryDTO
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> getNoteBookList(Page page, NoteBookListQueryDTO noteBookListQueryDTO) {
        QueryWrapper<NoteBook> queryWrapper = WrapperHelp.getWrapper(noteBookListQueryDTO);
        queryWrapper.ne(true, NoteBook.COLUMN_STATUS, NoteBookStatusEnum.DELETE.getCode())
                .ne(true, "deleted", NoteBookStatusEnum.STOP.getCode());
        if (noteBookListQueryDTO.getStatus() != null){
            if (noteBookListQueryDTO.getStatus().equals(NoteBookStatusEnum.RUN.getCode())){
                //运行中的notebook必须有url
                queryWrapper.eq(NoteBook.COLUMN_STATUS, NoteBookStatusEnum.RUN.getCode())
                        .ne(NoteBook.COLUMN_URL,SymbolConstant.BLANK);
            }else if (noteBookListQueryDTO.getStatus().equals(NoteBookStatusEnum.STARTING.getCode())){
                //启动中的notebook还包括运行中但没有url
                queryWrapper.and((qw)->
                        qw.eq(NoteBook.COLUMN_STATUS, NoteBookStatusEnum.RUN.getCode()).eq(NoteBook.COLUMN_URL, SymbolConstant.BLANK)
                                .or()
                                .eq(NoteBook.COLUMN_STATUS,NoteBookStatusEnum.STARTING.getCode())
                );
            }else {
                // 其他状态照常
                queryWrapper.eq(NoteBook.COLUMN_STATUS, NoteBookStatusEnum.RUN.getCode());
            }
        }
        queryWrapper.orderBy(true, false, "id");
        IPage<NoteBook> noteBookPage = noteBookMapper.selectPage(page, queryWrapper);
        return PageUtil.toPage(noteBookPage, noteBookConvert::toDto);
    }

    /**
     * 查询所有 notebook 记录
     *
     * @param page
     * @param noteBookQueryDTO
     * @return List<NoteBook>
     */
    @Override
    public List<NoteBook> getList(Page page, NoteBookQueryDTO noteBookQueryDTO) {
        return noteBookMapper.selectPage(page, WrapperHelp.getWrapper(noteBookQueryDTO)).getRecords();
    }

    /**
     * 获取镜像路径
     *
     * @param bizEnum
     * @return String
     */
    private String getDefaultImage(BizEnum bizEnum) {
        if (bizEnum == null) {
            throw new NotebookBizException("业务模块未识别！无法获取默认镜像。");
        }
        List<String> projectList = harborProjectService.getHarborProjects(bizEnum.getCreateResource());
        if (CollUtil.isEmpty(projectList)) {
            throw new NotebookBizException("此模块" + bizEnum.getBizName() + "未配置Project！无法获取默认镜像。");
        }
        List<String> imageList = harborApi.searchImageNames(projectList);
        if (CollUtil.isEmpty(imageList)) {
            throw new NotebookBizException("此模块" + bizEnum.getBizName() + "未配置镜像！");
        }
        return imageList.get(MagicNumConstant.ZERO);
    }

    /**
     * 新增加 notebook
     *
     * @param noteBook
     * @return NoteBookVO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteBookVO createNoteBook(NoteBook noteBook) {
        if (noteBookMapper.findByNameAndStatus(noteBook.getNoteBookName(),NoteBookStatusEnum.DELETE.getCode()) != null) {
            throw new NotebookBizException("Notebook名称已使用过！请重新提交。");
        }
        if (StringUtils.isEmpty(noteBook.getName())) {
            noteBook.setName(k8sNameTool.getK8sName());
        }
        noteBook.setK8sNamespace(k8sNameTool.generateNamespace(noteBook.getCreateUserId()));
        noteBook.setK8sResourceName(k8sNameTool.generateResourceName(BizEnum.NOTEBOOK, noteBook.getName()));
        if (StringUtils.isBlank(noteBook.getK8sPvcPath())) {
            //20200618 修改为 使用训练路劲
            noteBook.setK8sPvcPath(k8sNameTool.getNfsPath(BizNfsEnum.ALGORITHM, noteBook.getCreateUserId()));
        }
        noteBook.setK8sMountPath(NotebookUtil.getK8sMountPath());
        if (start(noteBook)) {
            noteBook.setStatus(NoteBookStatusEnum.STARTING.getCode());
        } else {
            noteBook.setStatus(NoteBookStatusEnum.STOP.getCode());
        }
        noteBookMapper.insert(noteBook);
        return noteBookConvert.toDto(noteBook);
    }

    /**
     * 初始化namespace
     *
     * @param noteBook
     * @param labels
     * @return boolean
     */
    private boolean initNameSpace(NoteBook noteBook, Map<String, String> labels) {
        try {
            BizNamespace result = namespaceApi.create(noteBook.getK8sNamespace(), labels);
            noteBook.setK8sStatusCode(result.getCode() == null ? BLANK : result.getCode());
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(result));
            return (HttpUtils.isSuccess(result.getCode())
                    || K8sResponseEnum.EXISTS.getCode().equals(result.getCode()));
        } catch (Exception e) {
            LogUtil.error(LogEnum.NOTE_BOOK, "createNoteBook调用jupyterResourceApi.createWithPvc异常！{}", e);
            noteBook.setK8sStatusCode(BLANK);
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
            return false;
        }
    }

    /**
     * 验证 notebook 是否可删除
     *
     * @param noteBookIds
     * @return List<NoteBook>
     */
    @Override
    public List<NoteBook> validateDeleteNoteBook(Set<Long> noteBookIds) {
        for (Long noteBookId : noteBookIds) {
            NumberUtil.isNumber(noteBookId);
        }
        List<Integer> deleteTypeList = NoteBookStatusEnum.getCanDeleteStatus();
        List<NoteBook> noteBookList = noteBookMapper.selectBatchIds(noteBookIds);
        for (NoteBook noteBook : noteBookList) {
            if (deleteTypeList.contains(noteBook.getStatus())) {
                throw new NotebookBizException("不可删除正在运行的notebook！");
            }
        }
        return noteBookList;
    }

    /**
     * 删除notebook异步方法
     *
     * @param noteBookList
     */
    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void deleteNoteBooks(List<NoteBook> noteBookList) {
        if (CollUtil.isNotEmpty(noteBookList)) {
            for (NoteBook noteBook : noteBookList) {
                if (noteBook.getStatus().equals(NoteBookStatusEnum.STOP.getCode())) {
                    deleteNoteBook(noteBook);
                }
            }
        }
    }

    /**
     * 删除notebook实现逻辑
     *
     * @param noteBook
     * @return String
     */
    private String deleteNoteBook(NoteBook noteBook) {
        if (noteBook == null) {
            throw new NotebookBizException(NotebookUtil.NOTEBOOK_NOT_EXISTS);
        }
        String returnStr;
        NoteBookStatusEnum statusEnum = getStatus(noteBook);
        if (NoteBookStatusEnum.STOP == statusEnum) {
            noteBook.setK8sStatusCode(BLANK);
            noteBook.setK8sStatusInfo(BLANK);
            noteBook.setUrl(BLANK);
            returnStr = this.deletePvc(noteBook);
        } else {
            try {
                PtBaseResult result = jupyterResourceApi.delete(noteBook.getK8sNamespace(), noteBook.getK8sResourceName());
                noteBook.setK8sStatusCode(result.getCode() == null ? BLANK : result.getCode());
                noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(result));
                if (HttpUtils.isSuccess(result.getCode())) {
                    noteBook.setStatus(NoteBookStatusEnum.DELETING.getCode());
                    // 添加超时时间点
                    noteBook.setLastOperationTimeout(NotebookUtil.getTimeoutSecondLong());
                    noteBook.setUrl(BLANK);
                    returnStr = NoteBookStatusEnum.DELETING.getDescription();
                } else if (K8sResponseEnum.REPEAT.getCode().equals(result.getCode())) {
                    // 重复提交停止指令，无需再次停止，直接删除PVC文件
                    noteBook.setUrl(BLANK);
                    returnStr = deletePvc(noteBook);
                } else {
                    // 其他失败编码 -> 删除失败,保留原状态
                    returnStr = "删除失败";
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.NOTE_BOOK, "deleteNoteBook调用jupyterResourceApi.delete异常！{}", e);
                noteBook.setK8sStatusCode(BLANK);
                noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
                returnStr = "删除失败";
            }
        }
        this.updateById(noteBook);
        return returnStr;
    }

    /**
     * 启动notebook
     *
     * @param noteBookId
     * @return String
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startNoteBook(Long noteBookId) {
        NumberUtil.isNumber(noteBookId);
        NoteBook noteBook = noteBookMapper.selectById(noteBookId);
        return startNoteBook(noteBook);
    }

    /**
     * 具体启动notebook实现
     *
     * @param noteBook
     * @return String
     */
    private String startNoteBook(NoteBook noteBook) {
        if (noteBook == null) {
            throw new NotebookBizException(NotebookUtil.NOTEBOOK_NOT_EXISTS);
        }
        if (NoteBookStatusEnum.RUN.getCode().equals(noteBook.getStatus())) {
            return "notebook " + NoteBookStatusEnum.RUN.getDescription();
        } else if (NoteBookStatusEnum.STARTING.getCode().equals(noteBook.getStatus())) {
            return "notebook " + NoteBookStatusEnum.STARTING.getDescription();
        } else if (!NoteBookStatusEnum.STOP.getCode().equals(noteBook.getStatus())) {
            throw new NotebookBizException("notebook【" + noteBook.getName() + "】当前状态：" + NoteBookStatusEnum.getDescription(noteBook.getStatus()) + ",无法再次启动。");
        }
        String returnStr;
        if (start(noteBook)) {
            noteBook.setStatus(NoteBookStatusEnum.STARTING.getCode());
            returnStr = NoteBookStatusEnum.STARTING.getDescription();
        } else {
            // 重启notebook状态沿用历史状态
            returnStr = "启动" + NotebookUtil.FAILED;
        }
        this.updateById(noteBook);
        return returnStr;
    }

    /**
     * 更新notebook
     *
     * @param noteBook
     * @return NoteBook
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteBook updateById(NoteBook noteBook) {
        noteBook.setUpdateTime(null);
        noteBook.setUpdateUserId(NotebookUtil.getCurUserId());
        noteBookMapper.updateById(noteBook);
        return noteBook;
    }

    /**
     * 启动notebook
     *
     * @param noteBook
     * @return true 启动成功；false 启动失败
     */
    private boolean start(NoteBook noteBook) {
        // 添加启动时间
        noteBook.setLastStartTime(new Date());
        // 添加超时时间点
        noteBook.setLastOperationTimeout(NotebookUtil.getTimeoutSecondLong());
        if (initNameSpace(noteBook, null)) {
            try {
                //20200618 修改为 创建时不创建PVC
                PtJupyterDeployVO result = jupyterResourceApi.create(PtJupyterResourceConvert.toPtJupyterResourceBo(noteBook, k8sNameTool, notebookDelayDeleteTime));
                noteBook.setK8sStatusCode(result.getCode() == null ? BLANK : result.getCode());
                noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(result));
                return HttpUtils.isSuccess(result.getCode());
            } catch (Exception e) {
                LogUtil.error(LogEnum.NOTE_BOOK, "notebook调用jupyterResourceApi.createWithPvc异常！{}", e);
                noteBook.setK8sStatusCode(BLANK);
                noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 停止notebook
     *
     * @param noteBookId
     * @return String
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String stopNoteBook(Long noteBookId) {
        NumberUtil.isNumber(noteBookId);
        NoteBook noteBook = noteBookMapper.selectById(noteBookId);
        if (noteBook == null) {
            throw new NotebookBizException(NotebookUtil.NOTEBOOK_NOT_EXISTS);
        }
        if (!NoteBookStatusEnum.RUN.getCode().equals(noteBook.getStatus())) {
            throw new NotebookBizException("notebook正在运行,不能停止");
        }
        String returnStr;
        NoteBookStatusEnum statusEnum = getStatus(noteBook);
        if (NoteBookStatusEnum.STOP == statusEnum) {
            noteBook.setK8sStatusCode(BLANK);
            noteBook.setK8sStatusInfo(BLANK);
            noteBook.setUrl(BLANK);
            returnStr = "已停止";
        } else {
            try {
                PtBaseResult result = jupyterResourceApi.delete(noteBook.getK8sNamespace(), noteBook.getK8sResourceName());
                noteBook.setK8sStatusCode(result.getCode() == null ? BLANK : result.getCode());
                noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(result));
                if (HttpUtils.isSuccess(result.getCode())) {
                    noteBook.setStatus(NoteBookStatusEnum.STOPPING.getCode());
                    // 添加超时时间点
                    noteBook.setLastOperationTimeout(NotebookUtil.getTimeoutSecondLong());
                    noteBook.setUrl(BLANK);
                    returnStr = NoteBookStatusEnum.STOPPING.getDescription();
                } else if (K8sResponseEnum.REPEAT.getCode().equals(result.getCode())) {
                    // 重复提交停止指令，无需再次停止，直接标记停止
                    noteBook.setStatus(NoteBookStatusEnum.STOP.getCode());
                    noteBook.setUrl(BLANK);
                    returnStr = NoteBookStatusEnum.STOP.getDescription();
                } else {
                    // 其他失败编码 -> 停止失败,保留原状态
                    returnStr = "停止" + NotebookUtil.FAILED;
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.NOTE_BOOK, "停止notebook调用jupyterResourceApi.delete异常！{}", e);
                noteBook.setK8sStatusCode(BLANK);
                noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
                returnStr = "停止" + NotebookUtil.FAILED;
            }
        }
        this.updateById(noteBook);
        return returnStr;
    }

    /**
     * 开启notebook
     *
     * @param noteBookId
     * @return String
     */
    @Override
    public String openNoteBook(Long noteBookId) {
        NumberUtil.isNumber(noteBookId);
        NoteBook noteBook = noteBookMapper.selectById(noteBookId);
        if (noteBook == null) {
            throw new NotebookBizException(NotebookUtil.NOTEBOOK_NOT_EXISTS);
        } else if (NoteBookStatusEnum.RUN.getCode().equals(noteBook.getStatus())) {
            if (NotebookUtil.checkUrlContainsToken(noteBook.getUrl())) {
                return noteBook.getUrl();
            } else {
                // 补偿:已启动notebook获取可访问地址
                String jupyterUrlWithToken = this.getJupyterUrl(noteBook);
                if (NotebookUtil.checkUrlContainsToken(jupyterUrlWithToken)) {
                    noteBook.setUrl(jupyterUrlWithToken);
                    this.updateById(noteBook);
                    return noteBook.getUrl();
                } else {
                    throw new NotebookBizException("notebook已启动 获取URL失败！");
                }
            }
        } else {
            throw new NotebookBizException("notebook 尚未启动成功,无法打开。");
        }
    }

    /**
     * 获取jupyter 地址
     *
     * @param noteBook
     * @return String
     */
    @Override
    public String getJupyterUrl(NoteBook noteBook) {
        try {
            return podApi.getUrlByResourceName(noteBook.getK8sNamespace(), noteBook.getK8sResourceName());
        } catch (Exception e) {
            LogUtil.error(LogEnum.NOTE_BOOK, "notebook nameSpace:{} resourceName:{} 获取URL失败！", noteBook.getK8sNamespace(), noteBook.getK8sResourceName(), e);
            noteBook.setK8sStatusCode(BLANK);
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
            return null;
        }
    }

    /**
     * 获取notebook状态
     *
     * @param noteBook
     * @return NoteBookStatusEnum
     */
    @Override
    public NoteBookStatusEnum getStatus(NoteBook noteBook) {
        try {
            BizPod result = podApi.getWithResourceName(noteBook.getK8sNamespace(), noteBook.getK8sResourceName());
            noteBook.setK8sStatusCode(result.getCode() == null ? BLANK : result.getCode());
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(result));
            if (K8sResponseEnum.NOT_FOUND.getCode().equals(result.getCode())) {
                // 结果不存在当已停止
                return NoteBookStatusEnum.STOP;
            } else if (!HttpUtils.isSuccess(result.getCode())) {
                LogUtil.warn(LogEnum.NOTE_BOOK, "notebook nameSpace:{} resourceName:{} 查询失败！", noteBook.getK8sNamespace(), noteBook.getK8sResourceName());
                return null;
            }
            return NoteBookStatusEnum.convert(result.getPhase());
        } catch (Exception e) {
            LogUtil.error(LogEnum.NOTE_BOOK, "notebook nameSpace:{} resourceName:{} 查询异常！{}", noteBook.getK8sNamespace(), noteBook.getK8sResourceName(), e);
            noteBook.setK8sStatusCode(BLANK);
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
            return null;
        }
    }

    /**
     * 第三方创建notebook
     *
     * @param bizNfsEnum
     * @param sourceNoteBookDTO
     * @return NoteBookVO
     */
    @Override
    public NoteBookVO createNoteBookByThirdParty(BizNfsEnum bizNfsEnum, SourceNoteBookDTO sourceNoteBookDTO) {
        String k8sPvcPath = sourceNoteBookDTO.getSourceFilePath();
        NoteBook noteBook = noteBookMapper.selectOne(WrapperHelp.getWrapper(new NoteBookQueryDTO(
                NoteBookStatusEnum.DELETE.getCode(),
                k8sPvcPath,
                sourceNoteBookDTO.getCurUserId())
        ));
        if (noteBook == null) {
            return this.createNoteBook(initSourceReqNoteBook(bizNfsEnum, sourceNoteBookDTO, k8sPvcPath));
        } else {
            if (!NoteBookStatusEnum.RUN.getCode().equals(noteBook.getStatus())) {
                this.startNoteBook(noteBook);
            }
            return noteBookConvert.toDto(noteBook);
        }
    }

    /**
     * 初始化第三方请求的notebook
     *
     * @param bizNfsEnum
     * @param sourceNoteBookDTO
     * @param k8sPvcPath
     * @return NoteBook
     */
    private NoteBook initSourceReqNoteBook(BizNfsEnum bizNfsEnum, SourceNoteBookDTO sourceNoteBookDTO, String k8sPvcPath) {
        NoteBook noteBook = new NoteBook();
        noteBook.setCreateUserId(sourceNoteBookDTO.getCurUserId());
        noteBook.setUserId(sourceNoteBookDTO.getCurUserId());
        noteBook.setCreateResource(bizNfsEnum.getCreateResource());
        noteBook.setDescription(bizNfsEnum.getBizName());
        noteBook.setName(k8sNameTool.getK8sName());
        String notebookName = NotebookUtil.generateName(bizNfsEnum, sourceNoteBookDTO.getSourceId());
        if (noteBookMapper.findByNameAndStatus(notebookName,NoteBookStatusEnum.DELETE.getCode()) != null) {
            // 重名随机符号拼接
            notebookName += RandomUtil.randomString(MagicNumConstant.TWO);
        }

        noteBook.setNoteBookName(notebookName);
        noteBook.setCpuNum(MagicNumConstant.ONE);
        noteBook.setGpuNum(MagicNumConstant.ZERO);
        noteBook.setMemNum(MagicNumConstant.ONE);
        noteBook.setDiskMemNum(MagicNumConstant.ONE);
        noteBook.setAlgorithmId(sourceNoteBookDTO.getSourceId());

        noteBook.setK8sPvcPath(k8sPvcPath);
        noteBook.setK8sImageName(getDefaultImage(BizEnum.NOTEBOOK));
        return noteBook;
    }

    /**
     * 获取地址
     *
     * @param noteBookId
     * @return String
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getAddress(Long noteBookId) {
        NumberUtil.isNumber(noteBookId);
        NoteBook noteBook = noteBookMapper.selectById(noteBookId);
        if (noteBook == null) {
            throw new NotebookBizException(NotebookUtil.NOTEBOOK_NOT_EXISTS);
        } else if (NoteBookStatusEnum.RUN.getCode().equals(noteBook.getStatus())) {
            if (NotebookUtil.checkUrlContainsToken(noteBook.getUrl())) {
                return noteBook.getUrl();
            } else {
                // 补偿:已启动notebook获取可访问地址
                String jupyterUrlWithToken = this.getJupyterUrl(noteBook);
                if (NotebookUtil.checkUrlContainsToken(jupyterUrlWithToken)) {
                    noteBook.setUrl(jupyterUrlWithToken);
                    this.updateById(noteBook);
                    return noteBook.getUrl();
                }
            }
        }
        return null;
    }

    /**
     * 删除PVC
     *
     * @param noteBook
     * @return String
     */
    @Override
    public String deletePvc(NoteBook noteBook) {
        noteBook.setStatus(NoteBookStatusEnum.DELETE.getCode());
        noteBook.setDeleted(true);
        return NoteBookStatusEnum.DELETE.getDescription();
    }

    /**
     * 获取notebook所有状态
     *
     * @return List<NoteBookStatusDTO>
     */
    @Override
    public List<NoteBookStatusDTO> getNoteBookStatus() {
        List<NoteBookStatusDTO> noteBookStatusDtoList = new ArrayList<>();
        for (NoteBookStatusEnum noteBookStatusEnum : NoteBookStatusEnum.values()) {
            if (noteBookStatusEnum != NoteBookStatusEnum.DELETE) {
                NoteBookStatusDTO noteBookStatusDTO = new NoteBookStatusDTO();
                noteBookStatusDTO.setStatusCode(noteBookStatusEnum.getCode());
                noteBookStatusDTO.setStatusName(noteBookStatusEnum.getDescription());
                noteBookStatusDtoList.add(noteBookStatusDTO);
            }
        }
        return noteBookStatusDtoList;
    }

    /**
     * 获取正在运行的notebook数量
     *
     * @return int
     */
    @Override
    public int getNoteBookRunNumber() {
        return noteBookMapper.selectRunNoteBookNum(NoteBookStatusEnum.RUN.getCode());
    }

    /**
     * 获取notebook配置的模式数据
     *
     * @return Map<String, List < NoteBookModel>>
     */
    @Override
    public Map<String, List<NoteBookModel>> getNoteBookModel() {
        List<NoteBookModel> noteBookModelList = noteBookModelMapper.selectAllNoteBookModel();
        return CollUtil.isEmpty(noteBookModelList) ?
                new HashMap<>(MagicNumConstant.EIGHT) :
                noteBookModelList.stream().collect(Collectors.groupingBy(NoteBookModel::getModelType));
    }

    /**
     * 刷新notebook状态
     *
     * @param statusEnum
     * @param noteBook
     * @return boolean
     */
    @Override
    public boolean refreshNoteBookStatus(NoteBookStatusEnum statusEnum, NoteBook noteBook) {
        if (NoteBookStatusEnum.RUN == statusEnum) {
            if (NoteBookStatusEnum.STARTING.getCode().equals(noteBook.getStatus())) {
                // append jupyter url+token
                noteBook.setUrl(this.getJupyterUrl(noteBook));
                // 仅启动中可切换为启动状态
                noteBook.setStatus(statusEnum.getCode());
            } else {
                return true;
            }
        } else if (NoteBookStatusEnum.STOP == statusEnum) {
            if (NoteBookStatusEnum.DELETING.getCode().equals(noteBook.getStatus())) {
                this.deletePvc(noteBook);
                // deletePVC方法内部已设置状态，无需这边设置
            } else {
                noteBook.setStatus(statusEnum.getCode());
            }
            noteBook.setUrl(BLANK);
        }
        return false;
    }

    /**
     * 支持双向更新，如果不为0 ，则是绑定关联关系，反之解除绑定关系
     *
     * @param noteBookId
     * @param algorithmId
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTrainIdByNoteBookId(Long noteBookId, Long algorithmId) {
        if (noteBookId != null) {
            NumberUtil.isNumber(noteBookId);
            NoteBook noteBook = noteBookMapper.selectById(noteBookId);
            if (noteBook != null) {
                noteBook.setAlgorithmId(algorithmId == null ? MagicNumConstant.ZERO : algorithmId);
                noteBookMapper.updateById(noteBook);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取notebook详情
     *
     * @param noteBookIds
     * @return List<NoteBookVO>
     */
    @Override
    public List<NoteBookVO> getNotebookDetail(Set<Long> noteBookIds) {
        QueryWrapper<NoteBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",noteBookIds);
        queryWrapper.ne(NoteBook.COLUMN_STATUS,NoteBookStatusEnum.DELETE.getCode());
        List<NoteBook> noteBookList =  noteBookMapper.selectList(queryWrapper);
        return noteBookConvert.toDto(noteBookList);
    }

    /**
     * 获取正在运行却没有URL的notebook
     *
     * @param page
     * @return List<NoteBook>
     */
    @Override
    public List<NoteBook> getRunNotUrlList(Page page) {
        return noteBookMapper.selectRunNotUrlList(page,NoteBookStatusEnum.RUN.getCode());
    }
}
