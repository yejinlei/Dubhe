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

package org.dubhe.data.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.data.constant.*;
import org.dubhe.data.dao.FileMapper;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.FileCreateDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.domain.entity.Task;
import org.dubhe.data.domain.vo.FileConvert;
import org.dubhe.data.domain.vo.FileQueryCriteriaVO;
import org.dubhe.data.domain.vo.FileVO;
import org.dubhe.data.domain.vo.ProgressVO;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.DatasetService;
import org.dubhe.data.service.DatasetVersionFileService;
import org.dubhe.data.service.FileService;
import org.dubhe.data.service.store.IStoreService;
import org.dubhe.data.service.store.MinioStoreServiceImpl;
import org.dubhe.data.util.FileUtil;
import org.dubhe.data.util.JavaCvUtil;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static org.dubhe.constant.PermissionConstant.ADMIN_USER_ID;

/**
 * @description 文件信息 服务实现类
 * @date 2020-04-10
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    /**
     * 单个标注任务数量
     */
    @Value("${data.annotation.task.splitSize:16}")
    private Integer taskSplitSize;

    /**
     * 默认标注页面文件列表分页大小
     */
    @Value("${data.file.pageSize:20}")
    private Integer defaultFilePageSize;

    /**
     * 路径名前缀
     */
    @Value("${k8s.nfs-root-path:/nfs/}")
    private String prefixPath;

    /**
     * minIO公钥
     */
    @Value("${minio.accessKey}")
    private String accessKey;

    /**
     * minIO私钥
     */
    @Value("${minio.secretKey}")
    private String secretKey;

    /**
     * 加密字符串
     */
    @Value("${minio.url}")
    private String url;

    /**
     * offset限制
     */
    private static final int OFFSET_DIRECT_QUERY_LIMIT = 10000;

    /**
     * 需要采样文件set
     */
    private static final Set<Integer> STATUS_NEED_SAMPLE = new HashSet<Integer>() {{
        add(FileStatusEnum.INIT.getValue());
    }};

    /**
     * 采样文件查询条件
     */
    private static final FileQueryCriteriaVO NEED_SAMPLE_QUERY = FileQueryCriteriaVO.builder()
            .status(STATUS_NEED_SAMPLE).fileType(DatatypeEnum.VIDEO.getValue()).build();

    /**
     * 线程池
     */
    @Autowired
    private BasePool pool;

    /**
     * 文件转换
     */
    @Autowired
    private FileConvert fileConvert;

    /**
     * 文件工具类
     */
    @Autowired
    private FileUtil fileUtil;

    /**
     * 文件存储服务实现类
     */
    @Resource(type = MinioStoreServiceImpl.class)
    private IStoreService storeService;

    /**
     * 数据集服务实现类
     */
    @Resource
    @Lazy
    private DatasetService datasetService;

    /**
     * 数据集版本文件服务实现类
     */
    @Resource
    @Lazy
    private DatasetVersionFileService datasetVersionFileService;

    /**
     * 文件详情
     *
     * @param fileId 文件id
     * @return FileVO 文件信息
     */
    @Override
    public FileVO get(Long fileId) {
        File fileOne = baseMapper.selectById(fileId);
        Dataset dataset = datasetService.getOneById(fileOne.getDatasetId());
        Set<Long> createUserIds = new HashSet<>();
        createUserIds.add(dataset.getCreateUserId());
        createUserIds.add(ADMIN_USER_ID);
        QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.in("create_user_id", createUserIds)
                .eq("id", fileId);
        File file = baseMapper.selectOne(fileQueryWrapper);
        if (file == null) {
            return null;
        }

        return fileConvert.toDto(file, getAnnotation(file.getDatasetId(), file.getName()));
    }

    /**
     * 获取标注信息
     *
     * @param datasetId 数据集id
     * @param fileName  文件名
     * @return String
     */
    public String getAnnotation(Long datasetId, String fileName) {
        String path = fileUtil.getAnnotationAbsPath(datasetId, fileName);
        return storeService.read(path);
    }

    /**
     * 判断视频数据集是否已存在视频
     *
     * @param datasetId 数据集id
     */
    @Override
    public void isExistVideo(Long datasetId) {
        QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.lambda().eq(File::getDatasetId, datasetId);
        if (getBaseMapper().selectCount(fileQueryWrapper) > MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.VIDEO_EXIST);
        }
    }

    /**
     * 删除文件
     *
     * @param datasetId 数据集id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long datasetId) {
        QueryWrapper<File> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(File::getDatasetId, datasetId);
        remove(queryWrapper);
    }

    /**
     * 数据集标注进度
     *
     * @param datasetIds 数据集id
     * @return Map<Long, ProgressVO> 数据集标注进度map
     */
    @Override
    public Map<Long, ProgressVO> listStatistics(Collection<Long> datasetIds) {
        if (CollectionUtils.isEmpty(datasetIds)) {
            return Collections.emptyMap();
        }
        Map<Long, ProgressVO> res = new HashMap<>(datasetIds.size());
        // 封装数据集版本数据
        datasetIds.stream().forEach(aLong -> {
            Dataset dataset = datasetService.getOneById(aLong);
            List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService
                    .getFilesByDatasetIdAndVersionName(dataset.getId(), dataset.getCurrentVersionName());
            ProgressVO progressVO = ProgressVO.builder().build();
            datasetVersionFiles.stream().forEach(datasetVersionFile -> {
                switch (datasetVersionFile.getAnnotationStatus()) {
                    case 0:
                    case 1:
                        progressVO.setUnfinished(progressVO.getUnfinished() + MagicNumConstant.ONE);
                        break;
                    case 2:
                        progressVO.setAutoFinished(progressVO.getAutoFinished() + MagicNumConstant.ONE);
                        break;
                    case 3:
                        progressVO.setFinished(progressVO.getFinished() + MagicNumConstant.ONE);
                        break;
                    case 4:
                        progressVO.setFinishAutoTrack(progressVO.getFinishAutoTrack() + MagicNumConstant.ONE);
                        break;
                    default:
                }
            });
            res.put(aLong, progressVO);
        });
        return res;
    }

    /**
     * 取过滤后的文件
     *
     * @param datasetIds 数据集id集合
     * @param status     按状态过滤
     * @param need       需要还是不需要，true为需要status中的状态，false为不要其中的状态
     * @return Set<File> file文件集合
     */
    @Override
    public Set<File> toFiles(List<Long> datasetIds, Dataset dataset, Collection<Integer> status, boolean need) {
        if (CollectionUtils.isEmpty(status) && need) {
            return Collections.emptySet();
        }

        Set<File> files = new HashSet<File>() {{
            addAll(getByDatasetIds(datasetIds, dataset));
        }};

        if (CollectionUtils.isEmpty(status)) {
            return files;
        }

        return files.stream()
                .filter(i -> (need == status.contains(i.getStatus()) && DatatypeEnum.IMAGE.getValue().equals(i.getFileType())))
                .collect(Collectors.toSet());
    }

    /**
     * 判断是否存在手动标注中的数据集
     *
     * @param id 数据集id
     * @return boolean 判断是否存在手动标注中的数据集
     */
    @Override
    public boolean hasManualAnnotating(Long id) {
        QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
        //状态等于标注中,排除视频文件
        fileQueryWrapper.lambda().eq(File::getDatasetId, id).eq(File::getStatus, FileStatusEnum.ANNOTATING.getValue()).ne(File::getFileType, DatatypeEnum.VIDEO.getValue());
        return getBaseMapper().selectCount(fileQueryWrapper) > MagicNumConstant.ZERO;
    }

    /**
     * 将整体任务分割
     *
     * @param files 文件集合
     * @param task  任务
     * @return List<TaskSplitBO> 任务集合
     */
    @Override
    public List<TaskSplitBO> split(Collection<File> files, Task task) {
        if (CollectionUtils.isEmpty(files)) {
            return new LinkedList<>();
        }
        LogUtil.info(LogEnum.BIZ_DATASET, "split file. file size:{}", files.size());
        Map<Long, List<File>> groupedFiles = files.stream().collect(Collectors.groupingBy(File::getDatasetId));
        List<TaskSplitBO> ts = groupedFiles.values().stream()
                .flatMap(fs -> CollectionUtil.split(fs, taskSplitSize).stream())
                .map(fs -> TaskSplitBO.from(fs, task)).filter(Objects::nonNull).collect(Collectors.toList());
        LogUtil.info(LogEnum.BIZ_DATASET, "split result. split size:{}", ts.size());
        return ts;
    }

    /**
     * 执行文件更新
     *
     * @param ids            文件id
     * @param fileStatusEnum 文件状态
     * @return int 更新结果
     */
    public int doUpdate(Collection<Long> ids, FileStatusEnum fileStatusEnum) {
        if (CollectionUtils.isEmpty(ids)) {
            return MagicNumConstant.ZERO;
        }
        File newObj = File.builder().status(fileStatusEnum.getValue()).build();
        List<File> files = baseMapper.selectBatchIds(ids);
        Dataset dataset = datasetService.getOneById(files.get(MagicNumConstant.ZERO).getDatasetId());
        Set<Long> createUserIds = new HashSet<>();
        createUserIds.add(dataset.getCreateUserId());
        createUserIds.add(ADMIN_USER_ID);
        QueryWrapper<File> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(File::getId, ids);
        queryWrapper.lambda().in(File::getCreateUserId, createUserIds);
        return baseMapper.update(newObj, queryWrapper);
    }

    /**
     * 更新文件状态
     *
     * @param files          文件集合
     * @param fileStatusEnum 文件状态
     * @return int 更新结果
     */
    @Override
    public int update(Collection<File> files, FileStatusEnum fileStatusEnum) {
        Collection<Long> ids = toIds(files);
        if (CollectionUtils.isEmpty(files)) {
            return MagicNumConstant.ZERO;
        }
        int count = doUpdate(ids, fileStatusEnum);
        if (count == MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH);
        }
        return count;
    }

    /**
     * 文件完成自动标注
     *
     * @param files file文件
     * @return boolean 自动标注结果
     */
    @Override
    public boolean finishAnnotation(Dataset dataset, Set<Long> files) {
        int count = datasetVersionFileService.updateAnnotationStatus(dataset.getId(), dataset.getCurrentVersionName(),
                files, FileStatusEnum.INIT.getValue(), FileStatusEnum.AUTO_ANNOTATION.getValue());
        return count > MagicNumConstant.ZERO;
    }

    /**
     * 通过文件获取id
     *
     * @param files file文件
     * @return Collection<Long> 文件id
     */
    private Collection<Long> toIds(Collection<File> files) {
        if (CollectionUtils.isEmpty(files)) {
            return Collections.emptySet();
        }
        return files.stream().map(File::getId).collect(Collectors.toSet());
    }

    /**
     * 更新文件
     *
     * @param fileId         文件id
     * @param fileStatusEnum 文件状态
     * @return int 更行结果
     */
    public int update(Long fileId, FileStatusEnum fileStatusEnum) {
        File newObj = File.builder()
                .id(fileId)
                .status(fileStatusEnum.getValue())
                .build();
        return baseMapper.updateById(newObj);
    }

    /**
     * 更新文件
     *
     * @param fileId         文件id
     * @param fileStatusEnum 文件状态
     * @param originStatus   文件状态
     * @return boolean 更行结果
     */
    public boolean update(Long fileId, FileStatusEnum fileStatusEnum, FileStatusEnum originStatus) {
        if (getById(fileId) == null) {
            return true;
        }
        UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(File::getId, fileId).eq(File::getStatus, originStatus.getValue())
                .set(File::getId, fileId).set(File::getStatus, fileStatusEnum.getValue());
        return update(updateWrapper);
    }

    /**
     * 保存文件
     *
     * @param fileId 文件id
     * @param files  file文件
     * @return List<Long> 保存的文件id集合
     */
    @Override                 //datasetId
    public List<Long> saveFiles(Long fileId, List<FileCreateDTO> files) {
        Map<String, String> fail = new HashMap<>(files.size());
        List<Long> fileIds = new ArrayList<>();
        List<File> newFiles = new ArrayList<>();
        Long datasetUserId = datasetService.getOneById(fileId).getCreateUserId();
        Long userId = null;
        if (JwtUtils.getCurrentUserDto() != null) {
            userId = JwtUtils.getCurrentUserDto().getId();
        }
        files.stream().map(file -> FileCreateDTO.toFile(file, fileId)).forEach(f -> {
            try {
                newFiles.add(f);
            } catch (DuplicateKeyException e) {
                fail.put(f.getName(), "the file already exists");
                return;
            }
        });
        if (!CollectionUtils.isEmpty(fail)) {
            throw new BusinessException(ErrorEnum.FILE_EXIST, JSON.toJSONString(fail), null);
        }
        baseMapper.saveList(newFiles, userId, datasetUserId);
        newFiles.forEach(f -> fileIds.add(f.getId()));
        return fileIds;
    }

    /**
     * 保存视频文件
     *
     * @param fileId 视频文件id
     * @param files  file文件
     * @param type   文件类型
     * @param pid    文件父id
     * @param userId 用户id
     */
    @Override
    public void saveVideoFiles(Long fileId, List<FileCreateDTO> files, int type, Long pid, Long userId) {
        List<File> list = new ArrayList<>();
        files.stream().map(file -> FileCreateDTO.toFile(file, fileId, type, pid)).forEach(fileCreate -> {
            fileCreate.setCreateUserId(userId);
            list.add(fileCreate);
        });
        saveBatch(list);
    }

    /**
     * 通过数据集ID获取文件(List<Long> datasetIds, Dataset dataset   二选一,如果都传默认使用 dataset)
     *
     * @param datasetIds 数据集id
     * @return List<File> 通过数据集ID获取文件列表
     */
    private List<File> getByDatasetIds(List<Long> datasetIds, Dataset dataset) {
        boolean datasetIdsFlag = !CollectionUtils.isEmpty(datasetIds);
        boolean datasetFlag = !(dataset == null);
        if (datasetIdsFlag || datasetFlag) {
            if (datasetFlag) {
                dataset = datasetService.getOneById(datasetIds.get(MagicNumConstant.ZERO));
            }
        } else {
            return new LinkedList<>();
        }
        QueryWrapper<File> queryWrapper = new QueryWrapper<>();
        if (dataset != null) {
            // 获取数据集当前版本文件列表
            List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService
                    .getListByDatasetIdAndAnnotationStatus(datasetIds.get(MagicNumConstant.ZERO), dataset.getCurrentVersionName(), Arrays.asList(FileStatusEnum.INIT.getValue()));
            queryWrapper.eq("dataset_id", datasetIds.get(MagicNumConstant.ZERO));
            List<Long> fileIds = new ArrayList<>();
            fileIds.add(MagicNumConstant.NEGATIVE_ONE__LONG);
            if (CollectionUtil.isNotEmpty(datasetVersionFiles)) {
                fileIds.addAll(datasetVersionFiles.stream()
                        .map(DatasetVersionFile::getFileId).collect(Collectors.toList()));
            }
            queryWrapper.in("id", fileIds);
            Set<Long> createUserIds = new HashSet<>();
            createUserIds.add(dataset.getCreateUserId());
            createUserIds.add(ADMIN_USER_ID);
            queryWrapper.in("create_user_id", createUserIds);
        }
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 返回文件分页信息
     *
     * @param datasetId     数据集id
     * @param page          分页条件
     * @param queryCriteria 查询文件的条件
     * @return Page  文件分页信息
     */
    public Page list(Long datasetId, Page page, FileQueryCriteriaVO queryCriteria) {
        queryCriteria.setDatasetId(datasetId);
        queryCriteria.setFileType(DatatypeEnum.IMAGE.getValue());
        return listPage(page, queryCriteria);
    }

    /**
     * 文件查询
     *
     * @param datasetId         数据集id
     * @param page              分页条件
     * @param fileQueryCriteria 查询条件
     * @return Map<String, Object> 文件查询列表
     */
    @Override
    public Map<String, Object> listVO(Long datasetId, Page page, FileQueryCriteriaVO fileQueryCriteria) {
        Page<File> filePage = list(datasetId, page, fileQueryCriteria);
        List<FileVO> vos = filePage.getRecords().stream()
                .map(file -> fileConvert.toDto(file, getAnnotation(datasetId, file.getName())))
                .collect(Collectors.toList());
        return org.dubhe.utils.PageUtil.toPage(filePage, vos);
    }

    /**
     * 分页查询
     *
     * @param page          分页条件
     * @param queryCriteria 查询文件的条件
     * @return Page 文件分页信息
     */
    private Page listPage(Page page, FileQueryCriteriaVO queryCriteria) {
        Dataset dataset = datasetService.getOneById(queryCriteria.getDatasetId());
        Set<Long> createUserIds = new HashSet<>();
        createUserIds.add(dataset.getCreateUserId());
        createUserIds.add(ADMIN_USER_ID);
        queryCriteria.setCreateUserIds(createUserIds);
        List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService
                .getListByDatasetIdAndAnnotationStatus(dataset.getId(), dataset.getCurrentVersionName(), queryCriteria.getStatus());
        Map<Long, Integer> fileStatus = new HashMap<>(MagicNumConstant.SIXTEEN);
        if (!CollectionUtils.isEmpty(datasetVersionFiles)) {
            queryCriteria.setIds(datasetVersionFiles.stream().map(datasetVersionFile -> datasetVersionFile.getFileId()).collect(Collectors.toSet()));
            datasetVersionFiles.stream().forEach(datasetVersionFile -> {
                fileStatus.put(datasetVersionFile.getFileId(), datasetVersionFile.getAnnotationStatus());
            });
        }
        queryCriteria.setStatus(null);
        if (CollectionUtils.isEmpty(queryCriteria.getIds())) {
            Set<Long> ids = new HashSet<>();
            ids.add(MagicNumConstant.NEGATIVE_ONE__LONG);
            queryCriteria.setIds(ids);
        }
        Page<File> result = getBaseMapper().selectPage(page, WrapperHelp.getWrapper(queryCriteria));
        result.getRecords().stream().forEach(file -> {
            file.setStatus(fileStatus.get(file.getId()));
        });
        return result;
    }

    /**
     * 创建查询
     *
     * @param datasetId 数据集id
     * @param status    状态
     * @return QueryWrapper<File> 查询条件
     */
    public QueryWrapper<File> buildQuery(Long datasetId, Set<Integer> status) {
        FileQueryCriteriaVO criteria = FileQueryCriteriaVO.builder()
                .datasetId(datasetId).order("id ASC").build();
        return WrapperHelp.getWrapper(criteria);
    }

    /**
     * 获取offset
     *
     * @param datasetId 数据集id
     * @param fileId    文件id
     * @param type      数据集类型
     * @return Integer 获取到offset
     */
    @Override
    public Integer getOffset(Long fileId, Long datasetId, Integer type) {
        Dataset dataset = datasetService.getOneById(datasetId);
        List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService
                .getListByDatasetIdAndAnnotationStatus(datasetId, dataset.getCurrentVersionName(), FileTypeEnum.getStatus(type));
        if (CollectionUtils.isEmpty(datasetVersionFiles)) {
            return null;
        }
        int count = MagicNumConstant.ZERO;
        for (DatasetVersionFile datasetVersionFile : datasetVersionFiles) {
            if (datasetVersionFile.getFileId() <= fileId) {
                count++;
            }
        }
        return count == MagicNumConstant.ZERO ? null : count - MagicNumConstant.ONE;
    }

    /**
     * 获取首个文件
     *
     * @param datasetId 数据集id
     * @param type      数据集类型
     * @return Long 首个文件Id
     */
    @Override
    public Long getFirst(Long datasetId, Integer type) {
        Dataset dataset = datasetService.getOneById(datasetId);
        DatasetVersionFile datasetVersionFile = datasetVersionFileService
                .getFirstByDatasetIdAndVersionNum(datasetId, dataset.getCurrentVersionName(), FileTypeEnum.getStatus(type));
        return datasetVersionFile == null ? null : datasetVersionFile.getFileId();
    }

    /**
     * 获取首个文件
     *
     * @param queryWrapper 条件构造器
     * @return Long 首个文件Id
     */
    public Long getFirst(QueryWrapper<File> queryWrapper) {
        queryWrapper.lambda().select(File::getId).last("limit 1");
        List<File> files = getBaseMapper().selectList(queryWrapper);
        return files.size() > MagicNumConstant.ZERO ? files.get(MagicNumConstant.ZERO).getId() : null;
    }

    /**
     * 这里的ge字段必须要与 #buildQuery中的排序字段同步，否则会造成顺序混乱。先从此接口取offset，再访问listByLimit取相应的数据
     *
     * @param fileId           文件id
     * @param fileQueryWrapper 条件构造器
     * @return Integer 获取到的Offset
     */
    public Integer getOffset(Long fileId, QueryWrapper<File> fileQueryWrapper) {
        File file = getById(fileId);
        if (file == null) {
            throw new BusinessException(ErrorEnum.FILE_ABSENT, "id:" + fileId, null);
        }
        fileQueryWrapper.lambda().select(File::getId).le(File::getId, file.getId());
        int count = getBaseMapper().selectCount(fileQueryWrapper);
        return count == MagicNumConstant.ZERO ? null : count - MagicNumConstant.ONE;
    }

    /**
     * 文件查询，物体检测标注页面使用
     *
     * @param datasetId 数据集id
     * @param offset    Offset
     * @param limit     limit
     * @param page      分页条件
     * @param type      数据集类型
     * @return Page<File> 文件查询分页列表
     */
    @Override
    public Page<File> listByLimit(Long datasetId, Long offset, Integer limit, Integer page, Integer type) {
        if (page == null) {
            page = MagicNumConstant.ONE;
        }
        QueryWrapper<File> fileQueryWrapper = buildQuery(datasetId, FileTypeEnum.getStatus(type));
        Dataset dataset = datasetService.getOneById(datasetId);
        List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService
                .getListByDatasetIdAndAnnotationStatus(dataset.getId(), dataset.getCurrentVersionName(), FileTypeEnum.getStatus(type));
        Map<Long, Integer> fileIds = new HashMap<>(MagicNumConstant.SIXTEEN);
        datasetVersionFiles.stream().forEach(datasetVersionFile -> {
            fileIds.put(datasetVersionFile.getFileId(), datasetVersionFile.getAnnotationStatus());
        });
        fileIds.put(MagicNumConstant.NEGATIVE_ONE__LONG, MagicNumConstant.NEGATIVE_ONE);
        if (fileIds.size() > MagicNumConstant.ZERO) {
            fileQueryWrapper.in("id", fileIds.keySet());
        }
        Set<Long> createUserIds = new HashSet<>();
        createUserIds.add(dataset.getCreateUserId());
        createUserIds.add(ADMIN_USER_ID);
        fileQueryWrapper.in("create_user_id", createUserIds);
        Page<File> files = listByLimit(offset, limit, fileQueryWrapper);
        files.getRecords().stream().forEach(file -> {
            file.setStatus(fileIds.get(file.getId()));
        });
        files.setCurrent(page);
        return files;
    }

    /**
     * 根据条件获取文件
     *
     * @param offset       offset
     * @param limit        limit
     * @param queryWrapper 条件构造器
     * @return Page<File> 文件查询分页结果
     */
    public Page<File> listByLimit(Long offset, Integer limit, QueryWrapper<File> queryWrapper) {
        if (offset == null) {
            offset = getDefaultOffset();
        }
        if (limit == null) {
            limit = defaultFilePageSize;
        }

        Integer total = getBaseMapper().selectCount(queryWrapper);
        Page<File> page = new Page<>();
        page.setTotal(total);

        List<File> files = doListByLimit(offset, limit, queryWrapper);
        page.setRecords(files);
        page.setSize(limit);
        return page;
    }

    /**
     * 默认offset
     *
     * @return Long 默认offset
     */
    public Long getDefaultOffset() {
        return MagicNumConstant.ZERO_LONG;
    }

    /**
     * 根据条件获取文件
     *
     * @param offset       offset
     * @param limit        limit
     * @param queryWrapper 条件构造器
     * @return List<File> 文件列表
     */
    public List<File> doListByLimit(Long offset, int limit, QueryWrapper<File> queryWrapper) {
        if (offset < OFFSET_DIRECT_QUERY_LIMIT) {
            return getBaseMapper().selectListByLimit(offset, limit, queryWrapper);
        }
        queryWrapper.lambda().select(File::getId);
        List<File> files = getBaseMapper().selectListByLimit(offset, limit, queryWrapper);
        Set<Long> ids = files.stream().mapToLong(File::getId).boxed().collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(ids)) {
            return new LinkedList<>();
        }
        return getBaseMapper().selectBatchIds(ids);
    }

    /**
     * 如果ids为空，则返回空
     *
     * @param fileids 文件id集合
     * @return Set<File> 文件集合
     */
    @Override
    public Set<File> get(List<Long> fileids) {
        if (CollectionUtils.isEmpty(fileids)) {
            return new HashSet<>();
        }
        File fileOne = baseMapper.selectById(fileids.get(MagicNumConstant.ZERO));
        if (fileOne == null) {
            return new HashSet<>();
        }
        Dataset dataset = datasetService.getOneById(fileOne.getDatasetId());
        Set<Long> createUserIds = new HashSet<>();
        createUserIds.add(dataset.getCreateUserId());
        createUserIds.add(ADMIN_USER_ID);
        QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.in("id", fileids)
                .in("create_user_id", createUserIds);
        return new HashSet(baseMapper.selectList(fileQueryWrapper));
    }

    /**
     * 获取指定数据集有效状态的文件列表
     *
     * @param datasetId 数据集id
     * @return List<File> 指定数据集有效状态的文件列表
     */
    public List<File> getFilesByDatasetId(Long datasetId) {
        QueryWrapper<File> queryWrapper = new QueryWrapper();
        queryWrapper.eq("dataset_id", datasetId);
        queryWrapper.eq("deleted", MagicNumConstant.ZERO);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 视频采样任务
     */
    @Override
    public void videoSample() {
        List<File> files = list(WrapperHelp.getWrapper(NEED_SAMPLE_QUERY));
        if (files == null) {
            return;
        }
        files.forEach(f -> {
            int updateFlag = getBaseMapper().updateSampleStatus(f.getId(), FileStatusEnum.INIT.getValue());
            if (updateFlag != MagicNumConstant.ZERO) {
                try {
                    pool.getExecutor().submit(() -> videoSample(f));
                } catch (Exception e) {
                    f.setStatus(FileStatusEnum.INIT.getValue());
                    getBaseMapper().updateById(f);
                    LogUtil.error(LogEnum.BIZ_DATASET, "sample task is refused", e);
                }
            }
        });
    }

    /**
     * 视频采样
     *
     * @param file 视频文件
     */
    public void videoSample(File file) {
        String path = file.getUrl();
        path = prefixPath + path;
        try {
            int space = file.getFrameInterval();
            List<String> picNames = JavaCvUtil.getVideoPic(path, space);
            saveVideoPic(picNames, file);
        } catch (Exception e) {
            file.setStatus(FileStatusEnum.INIT.getValue());
            getBaseMapper().updateById(file);
            LogUtil.error(LogEnum.BIZ_DATASET, "VideoSample fail:", e);
        }
    }

    /**
     * 保存采样后文件
     *
     * @param picNames 图片文件名字
     * @param file     视频文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveVideoPic(List<String> picNames, File file) {
        List<FileCreateDTO> fileCreateDTOS = new ArrayList<>();
        picNames.forEach(picName -> {
            picName = StringUtils.substringAfter(picName, prefixPath);
            FileCreateDTO f = FileCreateDTO.builder()
                    .url(picName)
                    .build();
            fileCreateDTOS.add(f);
        });
        saveVideoFiles(file.getDatasetId(), fileCreateDTOS, DatatypeEnum.IMAGE.getValue(), file.getId(), file.getCreateUserId());
        file.setStatus(FileStatusEnum.AUTO_ANNOTATION.getValue());
        getBaseMapper().updateById(file);
        Dataset ds = datasetService.getOneById(file.getDatasetId());
        FileQueryCriteriaVO fileQueryCriteria = FileQueryCriteriaVO.builder()
                .datasetId(file.getDatasetId())
                .fileType(DatatypeEnum.IMAGE.getValue())
                .build();
        List<File> files = list(WrapperHelp.getWrapper(fileQueryCriteria));
        List<DatasetVersionFile> list = new ArrayList<>();
        files.forEach(fileOne -> {
            DatasetVersionFile datasetVersionFile = new DatasetVersionFile(file.getDatasetId(), null, fileOne.getId());
            list.add(datasetVersionFile);
        });
        if (MagicNumConstant.ZERO != list.size()) {
            datasetVersionFileService.insertList(list);
        }
        datasetService.transferStatus(ds, DatasetStatusEnum.INIT);
    }

    /**
     * 批量更新file
     *
     * @param datasetVersionFiles 文件列表
     * @param init                更新结果
     */
    public void updateStatus(List<DatasetVersionFile> datasetVersionFiles, FileStatusEnum init) {
        List<Long> fileIds = datasetVersionFiles
                .stream().map((files) -> files.getFileId())
                .collect(Collectors.toList());
        UpdateWrapper<File> fileUpdateWrapper = new UpdateWrapper();
        fileUpdateWrapper.in("id", fileIds);
        File file = new File();
        file.setStatus(init.getValue());
        baseMapper.update(file, fileUpdateWrapper);
    }

    /**
     * 对minio 的账户密码进行加密操作
     *
     * @return Map<String, String> 加密后minio账户密码
     */
    @Override
    public Map<String, String> getMinIOInfo() throws Throwable {
        Map<String, String> keyPair = RsaEncrypt.genKeyPair();
        String publicKey = RsaEncrypt.getPublicKey(keyPair);
        String privateKey = RsaEncrypt.getPrivateKey(keyPair);
        return new HashMap<String, String>(MagicNumConstant.FOUR) {{
            put("url", RsaEncrypt.encrypt(url, publicKey));
            put("accessKey", RsaEncrypt.encrypt(accessKey, publicKey));
            put("secretKey", RsaEncrypt.encrypt(secretKey, publicKey));
            put("privateKey", privateKey);
        }};
    }

    /**
     * 获取文件对应所有增强文件
     *
     * @param fileId 文件id
     * @return List<File> 文件对应所有增强文件
     */
    @Override
    public List<File> getEnhanceFileList(Long fileId) {
        File file = baseMapper.getOneById(fileId);
        if (ObjectUtil.isNull(file)) {
            throw new BusinessException(ErrorEnum.FILE_ABSENT);
        }
        Dataset dataset = datasetService.getOneById(file.getDatasetId());
        if (ObjectUtil.isNull(dataset)) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        return datasetVersionFileService.getEnhanceFileList(dataset.getId(), dataset.getCurrentVersionName(), fileId);
    }

    @Override
    public File selectById(Long fileId) {
        return baseMapper.selectById(fileId);
    }

    @Override
    public File selectOne(QueryWrapper<File> queryWrapper) {
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<File> listFile(QueryWrapper<File> wrapper) {
        return list(wrapper);
    }

}
