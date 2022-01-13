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

package org.dubhe.notebook.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateBetween;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.constant.HarborProperties;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.NoteBookAlgorithmQueryDTO;
import org.dubhe.biz.base.dto.NoteBookAlgorithmUpdateDTO;
import org.dubhe.biz.base.dto.PtImageQueryUrlDTO;
import org.dubhe.biz.base.dto.SysUserConfigDTO;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.biz.base.enums.ImageSourceEnum;
import org.dubhe.biz.base.enums.ImageTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.HttpUtils;
import org.dubhe.biz.base.utils.NumberUtil;
import org.dubhe.biz.base.utils.ResultUtil;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.DatasetVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.k8s.api.JupyterResourceApi;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.vo.PtJupyterDeployVO;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.notebook.client.DatasetClient;
import org.dubhe.notebook.client.ImageClient;
import org.dubhe.notebook.config.NoteBookConfig;
import org.dubhe.notebook.constants.NoteBookErrorConstant;
import org.dubhe.notebook.convert.NoteBookConvert;
import org.dubhe.notebook.convert.PtJupyterResourceConvert;
import org.dubhe.notebook.dao.NoteBookMapper;
import org.dubhe.notebook.domain.dto.NoteBookCreateDTO;
import org.dubhe.notebook.domain.dto.NoteBookListQueryDTO;
import org.dubhe.notebook.domain.dto.SourceNoteBookDTO;
import org.dubhe.notebook.domain.entity.NoteBook;
import org.dubhe.notebook.enums.NoteBookStatusEnum;
import org.dubhe.notebook.service.NoteBookService;
import org.dubhe.notebook.service.ProcessNotebookCommand;
import org.dubhe.notebook.utils.NotebookUtil;
import org.dubhe.biz.base.vo.NoteBookVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
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
    private UserContextService userContextService;

    @Value("${user.config.notebook-delay-delete-time}")
    private Integer defaultNotebookDelayDeleteTime;


    @Autowired
    private ImageClient imageClient;

    @Autowired
    private HarborProperties harborProperties;

    @Autowired
    @Qualifier("hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    @Autowired
    private DatasetClient datasetClient;

    @Autowired
    private NoteBookConfig noteBookConfig;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ResourceCache resourceCache;

    @Value("Task:Notebook:"+"${spring.profiles.active}_notebook_id_")
    private String notebookIdPrefix;

    /**
     * 分页查询所有 notebook 记录
     *
     * @param page                 分页参数
     * @param noteBookListQueryDTO 查询参数
     * @return Map<String, Object> 分页查询结果
     */
    @Override
    public Map<String, Object> getNoteBookList(Page page, NoteBookListQueryDTO noteBookListQueryDTO) {
        QueryWrapper<NoteBook> queryWrapper = WrapperHelp.getWrapper(noteBookListQueryDTO);
        queryWrapper.ne(NoteBook.COLUMN_STATUS, NoteBookStatusEnum.DELETED.getCode())
                .ne("deleted", NoteBookStatusEnum.STOP.getCode());
        if (noteBookListQueryDTO.getStatus() != null) {
            if (noteBookListQueryDTO.getStatus().equals(NoteBookStatusEnum.RUN.getCode())) {
                //运行中的notebook必须有url
                queryWrapper.eq(NoteBook.COLUMN_STATUS, NoteBookStatusEnum.RUN.getCode())
                        .ne(NoteBook.COLUMN_URL, SymbolConstant.BLANK);
            } else if (noteBookListQueryDTO.getStatus().equals(NoteBookStatusEnum.STARTING.getCode())) {
                //启动中的notebook还包括运行中但没有url
                queryWrapper.and((qw) ->
                        qw.eq(NoteBook.COLUMN_STATUS, NoteBookStatusEnum.RUN.getCode()).eq(NoteBook.COLUMN_URL, SymbolConstant.BLANK)
                                .or()
                                .eq(NoteBook.COLUMN_STATUS, NoteBookStatusEnum.STARTING.getCode())
                );
            } else {
                // 其他状态照常
                queryWrapper.eq(NoteBook.COLUMN_STATUS, noteBookListQueryDTO.getStatus());
            }
        }
        queryWrapper.orderBy(true, false, "id");
        IPage<NoteBook> noteBookPage = noteBookMapper.selectPage(page, queryWrapper);
        return PageUtil.toPage(noteBookPage, noteBookConvert::toDto);
    }

    /**
     * 查询所有notebook记录
     *
     * @param page                分页参数
     * @param noteBookStatusEnums notebook状态枚举
     * @return notebook集合
     */
    @Override
    public List<NoteBook> getList(Page page, NoteBookStatusEnum... noteBookStatusEnums) {

        LambdaQueryWrapper<NoteBook> queryWrapper = new LambdaQueryWrapper<>();
        List<Integer> status = Arrays.asList(noteBookStatusEnums).stream().map(x -> x.getCode()).collect(Collectors.toList());
        queryWrapper.in(noteBookStatusEnums != null, NoteBook::getStatus, status);

        return noteBookMapper.selectPage(page, queryWrapper).getRecords();
    }

    /**
     * 获取默认镜像路径
     *
     * @return String 镜像路径
     */
    private String getDefaultImage() {
        PtImageQueryUrlDTO imageQueryUrlDTO = new PtImageQueryUrlDTO();
        imageQueryUrlDTO.setProjectType(ImageTypeEnum.NOTEBOOK.getType())
                .setImageResource(ImageSourceEnum.PRE.getCode());
        DataResponseBody<String> responseBody = imageClient.getImageUrl(imageQueryUrlDTO);
        if (!responseBody.succeed()) {
            LogUtil.error(LogEnum.NOTE_BOOK, "dubhe-image service call failed, responseBody is 【{}】", responseBody);
            throw new BusinessException("镜像服务调用失败");
        }

        String imageUrl = responseBody.getData();
        if (StringUtils.isBlank(imageUrl)) {
            LogUtil.error(LogEnum.NOTE_BOOK, "There is no default notebook image !");
            throw new BusinessException(ImageTypeEnum.NOTEBOOK.getCode() + "未配置默认镜像！");
        }

        return harborProperties.getAddress() + StrUtil.SLASH + imageUrl;
    }

    /**
     * 检测名称是否存在
     *
     * @param noteBookName notebook名称
     * @return true存在此名称 false 不存在此名称
     */
    public boolean existsName(String noteBookName) {

        LambdaQueryWrapper<NoteBook> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.eq(NoteBook::getName, noteBookName);

        int res = noteBookMapper.selectCount(queryWrapper);

        return res > 0;
    }

    /**
     * 新增加 notebook
     *
     * @param createDTO notebook创建参数
     * @return NoteBookVO notebook vo对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteBookVO createNoteBook(NoteBookCreateDTO createDTO) {
        NoteBook noteBook = new NoteBook();

        if (createDTO.getDataSourceId() != null) {
            DataResponseBody<DatasetVO> responseBody = datasetClient.get(createDTO.getDataSourceId());
            if (responseBody.succeed()) {
                if (responseBody.getData() != null) {
                    noteBook.setDataSourceName(responseBody.getData().getName());
                } else {
                    throw new BusinessException("无此数据集信息！");
                }
            } else {
                throw new BusinessException("数据集服务不可用！");
            }
        }

        BeanUtils.copyProperties(createDTO, noteBook);
        return createNoteBook(noteBook);
    }

    /**
     * 新增加 notebook
     *
     * @param noteBook notebook
     * @return NoteBookVO notebook vo
     */
    @Transactional(rollbackFor = Exception.class)
    public NoteBookVO createNoteBook(NoteBook noteBook) {

        long curUserId = userContextService.getCurUserId();

        String noteBookName = noteBook.getNoteBookName();
        if (existsName(noteBook.getNoteBookName())) {
            LogUtil.error(LogEnum.NOTE_BOOK, "The name 【{}】 of notebook already exists ", noteBookName);
            throw new BusinessException("Notebook名称已使用过！请重新提交。");
        }

        String dataSourcePath = noteBook.getDataSourcePath();
        if (StringUtils.isNotEmpty(dataSourcePath)) {
            if (fileStoreApi.fileOrDirIsExist(fileStoreApi.getBucket() + File.separator + dataSourcePath)) {
                noteBook.setDataSourcePath(dataSourcePath);
            } else {
                LogUtil.error(LogEnum.NOTE_BOOK, "Data source path 【{}】 doesn't exist!", dataSourcePath);
                throw new BusinessException("此数据集路径不存在！");
            }
        }

        noteBook.setName(k8sNameTool.getK8sName());

        noteBook.setK8sNamespace(k8sNameTool.generateNamespace(curUserId));

        noteBook.setK8sResourceName(k8sNameTool.generateResourceName(BizEnum.NOTEBOOK, noteBook.getName()));
        if (StringUtils.isBlank(noteBook.getK8sPvcPath())) {
            noteBook.setK8sPvcPath(k8sNameTool.getPath(BizPathEnum.ALGORITHM, curUserId));
        }
        noteBook.setCreateResource(BizPathEnum.NOTEBOOK.getCreateResource());
        noteBook.setK8sMountPath(NotebookUtil.getK8sMountPath());
        String taskIdentify = StringUtils.getUUID();
        if (start(noteBook, taskIdentify)) {
            noteBook.setStatus(NoteBookStatusEnum.STARTING.getCode());
        } else {
            noteBook.setStatus(NoteBookStatusEnum.STOP.getCode());
        }
        noteBookMapper.insert(noteBook);
        resourceCache.addTaskCache(taskIdentify,noteBook.getId(), noteBookName, notebookIdPrefix);
        return noteBookConvert.toDto(noteBook);
    }

    /**
     * 初始化namespace
     *
     * @param noteBook notebook
     * @param labels   标签集合
     * @return boolean 初始化结果 true 成功 false 失败
     */
    private boolean initNameSpace(NoteBook noteBook, Map<String, String> labels) {
        try {
            BizNamespace result = namespaceApi.create(noteBook.getK8sNamespace(), labels);
            noteBook.setK8sStatusCode(result.getCode() == null ? SymbolConstant.BLANK : result.getCode());
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(result));
            return (HttpUtils.isSuccess(result.getCode())
                    || K8sResponseEnum.EXISTS.getCode().equals(result.getCode()));
        } catch (Exception e) {
            LogUtil.error(LogEnum.NOTE_BOOK, "createNoteBook调用jupyterResourceApi.createNamespace异常！{}", e);
            noteBook.setK8sStatusCode(SymbolConstant.BLANK);
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
            return false;
        }
    }

    /**
     * 验证 notebook 是否可删除
     *
     * @param noteBookIds notebook id 集合
     * @return List<NoteBook> 被删除的notebook集合
     */
    @Override
    public List<NoteBook> validateDeletableNoteBook(Set<Long> noteBookIds) {
        for (Long noteBookId : noteBookIds) {
            NumberUtil.isNumber(noteBookId);
        }

        List<NoteBook> noteBookList = noteBookMapper.selectBatchIds(noteBookIds);
        for (NoteBook noteBook : noteBookList) {
            if (!NoteBookStatusEnum.deletable(noteBook.getStatus())) {
                throw new BusinessException("不可删除正在运行的notebook！");
            }
        }
        return noteBookList;
    }

    /**
     * 批量删除notebook
     *
     * @param noteBookIds notebook id 集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNoteBooks(Set<Long> noteBookIds) {
        List<NoteBook> noteBookList = validateDeletableNoteBook(noteBookIds);
        if (CollUtil.isNotEmpty(noteBookList)) {
            for (NoteBook noteBook : noteBookList) {
                noteBook.setStatus(NoteBookStatusEnum.DELETING.getCode());
                noteBookMapper.updateById(noteBook);
                String taskIdentify = (String) redisUtils.get(notebookIdPrefix + String.valueOf(noteBook.getId()));
                if (StringUtils.isNotEmpty(taskIdentify)){
                    redisUtils.del(taskIdentify, notebookIdPrefix + String.valueOf(noteBook.getId()));
                }
            }
        }
    }

    /**
     * 启动notebook
     *
     * @param noteBookId notebook id
     * @return String 删除结果提示
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
     * @param noteBook notebook
     * @return String 启动结果提示
     */
    private String startNoteBook(NoteBook noteBook) {
        if (noteBook == null) {
            throw new BusinessException(NotebookUtil.NOTEBOOK_NOT_EXISTS);
        }
        if (NoteBookStatusEnum.RUN.getCode().equals(noteBook.getStatus())) {
            return "notebook " + NoteBookStatusEnum.RUN.getDescription();
        } else if (NoteBookStatusEnum.STARTING.getCode().equals(noteBook.getStatus())) {
            return "notebook " + NoteBookStatusEnum.STARTING.getDescription();
        } else if (!NoteBookStatusEnum.STOP.getCode().equals(noteBook.getStatus())) {
            throw new BusinessException("notebook【" + noteBook.getName() + "】当前状态：" + NoteBookStatusEnum.getDescription(noteBook.getStatus()) + ",无法再次启动。");
        }
        String returnStr;
        String taskIdentify = resourceCache.getTaskIdentify(noteBook.getId(), noteBook.getNoteBookName(), notebookIdPrefix);
        if (start(noteBook, taskIdentify)) {
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
     * @param noteBook 即将更新的notebook
     * @return NoteBook 更新后的notebook
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteBook updateById(NoteBook noteBook) {
        noteBook.setUpdateTime(null);
        noteBook.setUpdateUserId(NotebookUtil.getCurUserId(userContextService));
        if (StringUtils.isBlank(noteBook.getStatusDetail())) {
            noteBook.setStatusDetail(SymbolConstant.BRACKETS);
        }
        noteBookMapper.updateById(noteBook);
        return noteBook;
    }

    /**
     * 启动notebook
     *
     * @param noteBook notebook
     * @return true 启动成功；false 启动失败
     */
    private boolean start(NoteBook noteBook, String taskIdentify) {
        Long curUserId = userContextService.getCurUserId();
        if (StringUtils.isBlank(noteBook.getPipSitePackagePath())) {
            String pipSitePackagePath = StringConstant.PIP_SITE_PACKAGE + SymbolConstant.SLASH + curUserId + SymbolConstant.SLASH + noteBook.getName() + SymbolConstant.SLASH;
            noteBook.setPipSitePackagePath(pipSitePackagePath);
        }
        // 添加启动时间
        noteBook.setLastStartTime(new Date());
        // 添加超时时间点
        noteBook.setLastOperationTimeout(NotebookUtil.getTimeoutSecondLong());
        if (initNameSpace(noteBook, null)) {
            try {
                // 获取Notebook延迟删除时间，单位小时转化为分钟
                int notebookDelayDeleteTime = getNotebookDelayDeleteTime() * 60;
                //创建时不创建PVC
                PtJupyterDeployVO result = jupyterResourceApi.create(PtJupyterResourceConvert.toPtJupyterResourceBo(noteBook, k8sNameTool, notebookDelayDeleteTime, taskIdentify));
                noteBook.setK8sStatusCode(result.getCode() == null ? SymbolConstant.BLANK : result.getCode());
                noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(result));
                if (!result.isSuccess()) {
                    noteBook.putStatusDetail(noteBook.getK8sResourceName(), result.getMessage());
                }
                return HttpUtils.isSuccess(result.getCode());
            } catch (Exception e) {
                LogUtil.error(LogEnum.NOTE_BOOK, "There is an error when create jupyter resource, the exception is 【{}】", e);
                noteBook.setK8sStatusCode(SymbolConstant.BLANK);
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
     * @param noteBookId notebook id
     * @return String 停止notebook结果提示语
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String stopNoteBook(Long noteBookId) {
        NumberUtil.isNumber(noteBookId);
        NoteBook noteBook = noteBookMapper.selectById(noteBookId);
        ResultUtil.notNull(noteBook, NoteBookErrorConstant.NOTEBOOK_NOT_EXISTS);
        ResultUtil.isEquals(NoteBookStatusEnum.RUN.getCode(), noteBook.getStatus(),
                NoteBookErrorConstant.INVALID_NOTEBOOK_STATUS);

        String returnStr;
        NoteBookStatusEnum statusEnum = getStatus(noteBook);
        if (NoteBookStatusEnum.STOP == statusEnum) {
            noteBook.setK8sStatusCode(SymbolConstant.BLANK);
            noteBook.setK8sStatusInfo(SymbolConstant.BLANK);
            noteBook.setUrl(SymbolConstant.BLANK);
            noteBook.setStatus(NoteBookStatusEnum.STOP.getCode());
            returnStr = "已停止";
        } else {
            try {
                PtBaseResult result = jupyterResourceApi.delete(noteBook.getK8sNamespace(), noteBook.getK8sResourceName());
                noteBook.setK8sStatusCode(result.getCode() == null ? SymbolConstant.BLANK : result.getCode());
                noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(result));
                if (HttpUtils.isSuccess(result.getCode())) {
                    noteBook.setStatus(NoteBookStatusEnum.STOPPING.getCode());
                    // 添加超时时间点
                    noteBook.setLastOperationTimeout(NotebookUtil.getTimeoutSecondLong());
                    noteBook.setUrl(SymbolConstant.BLANK);
                    returnStr = NoteBookStatusEnum.STOPPING.getDescription();
                } else if (K8sResponseEnum.REPEAT.getCode().equals(result.getCode())) {
                    // 重复提交停止指令，无需再次停止，直接标记停止
                    noteBook.setStatus(NoteBookStatusEnum.STOP.getCode());
                    noteBook.setUrl(SymbolConstant.BLANK);
                    returnStr = NoteBookStatusEnum.STOP.getDescription();
                } else {
                    // 其他失败编码 -> 停止失败,保留原状态
                    returnStr = "停止" + NotebookUtil.FAILED;
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.NOTE_BOOK, "停止notebook调用jupyterResourceApi.delete异常！{}", e);
                noteBook.setK8sStatusCode(SymbolConstant.BLANK);
                noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
                returnStr = "停止" + NotebookUtil.FAILED;
            }
        }
        this.updateById(noteBook);
        return returnStr;
    }

    /**
     * @see NoteBookService#batchStopNoteBooks()
     */
    @Override
    public void batchStopNoteBooks() {
        List<NoteBook> noteBooks = noteBookMapper.selectRunningList();
        if (CollectionUtils.isEmpty(noteBooks)) {
            return;
        }
        noteBooks.forEach(noteBook -> stopNoteBook(noteBook.getId()));
    }

    /**
     * 开启notebook
     *
     * @param noteBookId notebook id
     * @return String 开启notebook结果提示语
     */
    @Override
    public String openNoteBook(Long noteBookId) {
        NumberUtil.isNumber(noteBookId);
        NoteBook noteBook = noteBookMapper.selectById(noteBookId);
        if (noteBook == null) {
            throw new BusinessException(NotebookUtil.NOTEBOOK_NOT_EXISTS);
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
                    throw new BusinessException("notebook已启动 获取URL失败！");
                }
            }
        } else {
            throw new BusinessException("notebook 尚未启动成功,无法打开。");
        }
    }

    /**
     * 获取jupyter 地址
     *
     * @param noteBook notebook
     * @return String jupyter地址
     */
    @Override
    public String getJupyterUrl(NoteBook noteBook) {
        try {
            return podApi.getUrlByResourceName(noteBook.getK8sNamespace(), noteBook.getK8sResourceName());
        } catch (Exception e) {
            LogUtil.error(LogEnum.NOTE_BOOK, "notebook nameSpace 【{}】 resourceName 【{}】 获取URL失败！", noteBook.getK8sNamespace(), noteBook.getK8sResourceName(), e);
            noteBook.setK8sStatusCode(SymbolConstant.BLANK);
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
            return null;
        }
    }

    /**
     * 获取notebook状态
     *
     * @param noteBook notebook
     * @return NoteBookStatusEnum notebook状态
     */
    @Override
    public NoteBookStatusEnum getStatus(NoteBook noteBook) {
        try {
            BizPod result = podApi.getWithResourceName(noteBook.getK8sNamespace(), noteBook.getK8sResourceName());
            noteBook.setK8sStatusCode(result.getCode() == null ? SymbolConstant.BLANK : result.getCode());
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(result));
            if (K8sResponseEnum.NOT_FOUND.getCode().equals(result.getCode())) {

                long gap = new DateBetween(noteBook.getLastStartTime(), new Date()).between(DateUnit.MINUTE);
                // 超时处理
                if (gap < NumberConstant.NUMBER_2) {
                    return null;
                }
                // 结果不存在当已停止
                return NoteBookStatusEnum.STOP;
            } else if (!HttpUtils.isSuccess(result.getCode())) {
                LogUtil.warn(LogEnum.NOTE_BOOK, "Fail to get status ,notebook nameSpace is 【{}】, resourceName is 【{}】 ！", noteBook.getK8sNamespace(), noteBook.getK8sResourceName());
                return null;
            }
            return NoteBookStatusEnum.convert(result.getPhase());
        } catch (Exception e) {
            LogUtil.error(LogEnum.NOTE_BOOK, "Fail to get status ,notebook nameSpace is 【{}】, resourceName is 【{}】 ！Exception is 【{}】", noteBook.getK8sNamespace(), noteBook.getK8sResourceName(), e);
            noteBook.setK8sStatusCode(SymbolConstant.BLANK);
            noteBook.setK8sStatusInfo(NotebookUtil.getK8sStatusInfo(e));
            return null;
        }
    }

    /**
     * 第三方创建notebook
     *
     * @param bizPathEnum       业务路径枚举
     * @param sourceNoteBookDTO 第三方创建NoteBook请求对象
     * @return NoteBookVO notebook返前端数据
     */
    @Override
    public NoteBookVO createNoteBookByThirdParty(BizPathEnum bizPathEnum, SourceNoteBookDTO sourceNoteBookDTO) {
        String k8sPvcPath = sourceNoteBookDTO.getSourceFilePath();
        Long curUserId = userContextService.getCurUserId();
        LambdaQueryWrapper<NoteBook> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(NoteBook::getK8sPvcPath, k8sPvcPath);
        queryWrapper.eq(NoteBook::getOriginUserId, curUserId);
        queryWrapper.last(" limit 1 ");
        NoteBook noteBook = noteBookMapper.selectOne(queryWrapper);
        if (noteBook == null) {
            NoteBook newNoteBook = initSourceReqNoteBook(bizPathEnum, sourceNoteBookDTO, k8sPvcPath);
            return this.createNoteBook(newNoteBook);
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
     * @param bizPathEnum       业务路径枚举
     * @param sourceNoteBookDTO 第三方创建NoteBook请求对象
     * @param k8sPvcPath        k8s pvc路径
     * @return NoteBook notebook
     */
    private NoteBook initSourceReqNoteBook(BizPathEnum bizPathEnum, SourceNoteBookDTO sourceNoteBookDTO, String k8sPvcPath) {
        NoteBook noteBook = new NoteBook();


        noteBook.setCreateResource(bizPathEnum.getCreateResource());
        noteBook.setDescription(bizPathEnum.getBizName());
        noteBook.setName(k8sNameTool.getK8sName());
        String notebookName = NotebookUtil.generateName(bizPathEnum, sourceNoteBookDTO.getSourceId());
        if (existsName(notebookName)) {
            // 重名随机符号拼接
            notebookName += RandomUtil.randomString(MagicNumConstant.TWO);
        }

        noteBook.setNoteBookName(notebookName);
        noteBook.setCpuNum(noteBookConfig.getCpuNum());
        noteBook.setGpuNum(noteBookConfig.getGpuNum());
        noteBook.setMemNum(noteBookConfig.getMemNum());
        noteBook.setDiskMemNum(noteBookConfig.getDiskMemNum());
        noteBook.setAlgorithmId(sourceNoteBookDTO.getSourceId());

        noteBook.setK8sPvcPath(k8sPvcPath);
        noteBook.setK8sImageName(getDefaultImage());
        return noteBook;
    }

    /**
     * 获取地址
     *
     * @param noteBookId notebook id
     * @return String url地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getAddress(Long noteBookId) {
        NumberUtil.isNumber(noteBookId);
        NoteBook noteBook = noteBookMapper.selectById(noteBookId);
        if (noteBook == null) {
            throw new BusinessException(NotebookUtil.NOTEBOOK_NOT_EXISTS);
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
     * 获取正在运行的notebook数量
     *
     * @return int notebook数量
     */
    @Override
    public int getNoteBookRunNumber() {
        return noteBookMapper.selectRunNoteBookNum(NoteBookStatusEnum.RUN.getCode());
    }

    /**
     * 刷新notebook状态
     *
     * @param statusEnum notebook 状态枚举
     * @param noteBook   notebook
     * @return boolean true 刷新成功 false 刷新失败
     */
    @Override
    public boolean refreshNoteBookStatus(NoteBookStatusEnum statusEnum, NoteBook noteBook) {
        return refreshNoteBookStatus(statusEnum, noteBook, new ProcessNotebookCommand());
    }

    /**
     * 刷新notebook状态
     *
     * @param statusEnum             notebook 状态枚举
     * @param noteBook               notebook
     * @param processNotebookCommand 处理notebook生命周期的回调函数
     * @return boolean true 刷新成功 false 刷新失败
     */
    @Override
    public boolean refreshNoteBookStatus(NoteBookStatusEnum statusEnum, NoteBook noteBook, ProcessNotebookCommand processNotebookCommand) {
        if (statusEnum == null || noteBook == null) {
            return false;
        }
        if (statusEnum.getCode().equals(noteBook.getStatus())) {
            return false;
        }

        // 启动notebook (启动中->启动)
        if (NoteBookStatusEnum.RUN == statusEnum) {
            if (NoteBookStatusEnum.STARTING.getCode().equals(noteBook.getStatus())) {
                noteBook.setUrl(this.getJupyterUrl(noteBook));
                noteBook.setStatus(NoteBookStatusEnum.RUN.getCode());
                processNotebookCommand.running(noteBook);
                updateById(noteBook);
                return true;
            }
        } else if (NoteBookStatusEnum.STOP == statusEnum) {
            //删除notebook (删除中->删除)
            if (NoteBookStatusEnum.DELETING.getCode().equals(noteBook.getStatus())) {
                processNotebookCommand.delete(noteBook);
                noteBookMapper.deleteById(noteBook.getId());
                return true;
            }
            noteBook.setUrl(SymbolConstant.BLANK);
            noteBook.setStatus(NoteBookStatusEnum.STOP.getCode());
            noteBook.setStatusDetail(SymbolConstant.BLANK);
            jupyterResourceApi.delete(noteBook.getK8sNamespace(),noteBook.getK8sResourceName());
            processNotebookCommand.stop(noteBook);
            updateById(noteBook);
            return true;
        }
        return false;
    }

    /**
     * 获取notebook详情
     *
     * @param noteBookIds notebook id 集合
     * @return List<NoteBookVO> notebook vo 集合
     */
    @Override
    public List<NoteBookVO> getNotebookDetail(Set<Long> noteBookIds) {
        QueryWrapper<NoteBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", noteBookIds);
        List<NoteBook> noteBookList = noteBookMapper.selectList(queryWrapper);
        return noteBookConvert.toDto(noteBookList);
    }

    /**
     * 获取notebook详情
     *
     * @param noteBookId notebook id
     * @return List<NoteBookVO> notebook vo 集合
     */
    @Override
    public NoteBookVO getNotebookDetail(Long noteBookId) {
        NoteBook noteBook = noteBookMapper.selectById(noteBookId);
        return noteBookConvert.toDto(noteBook);
    }



    /**
     * 获取正在运行却没有URL的notebook
     *
     * @param page 分页信息
     * @return List<NoteBook> notebook集合
     */
    @Override
    public List<NoteBook> getRunNotUrlList(Page page) {
        return noteBookMapper.selectRunNotUrlList(page, NoteBookStatusEnum.RUN.getCode());
    }

    /**
     * 修改notebook算法ID
     *
     * @param noteBookAlgorithmListQueryDTO 算法更新notebook对象
     * @return 更新notebook数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateNoteBookAlgorithm(NoteBookAlgorithmUpdateDTO noteBookAlgorithmListQueryDTO) {
        if (CollectionUtils.isEmpty(noteBookAlgorithmListQueryDTO.getNotebookIdList())) {
            return 0;
        }
        return noteBookMapper.updateNoteBookAlgorithm(noteBookAlgorithmListQueryDTO.getNotebookIdList(), noteBookAlgorithmListQueryDTO.getAlgorithmId());
    }

    /**
     * 根据算法ID查询notebook Id
     *
     * @param noteBookAlgorithmQueryDTO 算法查询notebook对象
     * @return notebook id集合
     */
    @Override
    public List<Long> getNoteBookIdByAlgorithm(NoteBookAlgorithmQueryDTO noteBookAlgorithmQueryDTO) {
        if (CollectionUtils.isEmpty(noteBookAlgorithmQueryDTO.getAlgorithmIdList())) {
            return Collections.emptyList();
        }
        return noteBookMapper.getNoteBookIdByAlgorithm(noteBookAlgorithmQueryDTO.getAlgorithmIdList());
    }

    /**
     * 获取 Notebook 延时删除时间
     */
    private int getNotebookDelayDeleteTime() {

        UserContext curUser = userContextService.getCurUser();
        SysUserConfigDTO userConfig = curUser.getUserConfig();
        // 查询该用户是否配置 Notebook 延时删除时间
        Integer notebookDelayDeleteTime = userConfig.getNotebookDelayDeleteTime();
        if (userConfig.getNotebookDelayDeleteTime() != null) {
            return notebookDelayDeleteTime;
        }

        // 若该用户未配置 Notebook 延时删除时间，使用默认配置时间
        return defaultNotebookDelayDeleteTime;
    }
}
