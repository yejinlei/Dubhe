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
package org.dubhe.tadl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.enums.BaseErrorCodeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.dto.FileDTO;
import org.dubhe.biz.file.utils.LocalFileUtil;
import org.dubhe.biz.file.utils.MinioUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleResourceEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.dao.AlgorithmMapper;
import org.dubhe.tadl.domain.dto.*;
import org.dubhe.tadl.domain.entity.Algorithm;
import org.dubhe.tadl.domain.entity.AlgorithmStage;
import org.dubhe.tadl.domain.entity.AlgorithmVersion;
import org.dubhe.tadl.domain.vo.AlgorithmNextVersionVO;
import org.dubhe.tadl.domain.vo.AlgorithmStageVO;
import org.dubhe.tadl.domain.vo.AlgorithmVO;
import org.dubhe.tadl.domain.vo.AlgorithmVersionVO;
import org.dubhe.tadl.enums.StageEnum;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.dubhe.tadl.service.AlgorithmService;
import org.dubhe.tadl.service.AlgorithmStageService;
import org.dubhe.tadl.service.AlgorithmVersionService;
import org.dubhe.tadl.utils.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description 算法管理服务实现类
 * @date 2021-03-22
 */
@Service
public class AlgorithmServiceImpl extends ServiceImpl<AlgorithmMapper, Algorithm> implements AlgorithmService {


    /**
     * 文件操作工具
     */
    @Autowired
    private LocalFileUtil localFileUtil;

    /**
     * 算法阶段服务
     */
    @Autowired
    private AlgorithmStageService algorithmStageService;

    /**
     * 算法版本服务
     */
    @Autowired
    private AlgorithmVersionService algorithmVersionService;

    /**
     * minio工具
     */
    @Autowired
    private MinioUtil minioUtil;

    /**
     * minIO桶名
     */
    @Value("${minio.bucketName}")
    private String bucketName;

    @Resource
    private K8sNameTool k8sNameTool;
    /**
     * 路径工具类
     */
    @Autowired
    private PathUtil pathUtil;

    @Autowired
    private RecycleConfig recycleConfig;

    @Autowired
    private RecycleService recycleService;

    @Autowired
    private AlgorithmMapper algorithmMapper;

    /**
     * 解压算法包
     *
     * @param zipPath 路径
     * @return 解压是否成功
     */
    @Override
    public void unzip(String zipPath) {

        if (StringUtils.isBlank(zipPath)){
            throw new BusinessException("zip压缩包路径不存在");
        }
        String algorithm = zipPath.substring(zipPath.lastIndexOf(SymbolConstant.SLASH) + 1, zipPath.lastIndexOf(TadlConstant.ZIP_SUFFIX));
        // 判断算法文件下是否存在该算法
        if (!ObjectUtils.isEmpty(baseMapper.selectOne(new LambdaQueryWrapper<Algorithm>().eq(Algorithm::getName, algorithm)))) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_ALREADY_EXISTS_ERROR);
        }
        String localPath =bucketName.concat(SymbolConstant.SLASH).concat(zipPath);
        String targetPath = localPath.replace(TadlConstant.ZIP_SUFFIX, File.separator);
        boolean unzipResult = localFileUtil.unzipLocalPath(localPath, targetPath);
        if (!unzipResult) {
            throw new BusinessException("Failed to decompress the zip archive");
        }

    }

    /**
     * 根据算法 id 获取算法对象
     *
     * @param algorithmId 算法id
     * @return 算法对象
     */
    @Override
    public Algorithm selectOneById(Long algorithmId) {
        Algorithm algorithm = baseMapper.selectById(algorithmId);
        if (ObjectUtils.isEmpty(algorithm)) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_DOES_NOT_EXIST_ERROR);
        }
        return algorithm;
    }

    /**
     * 获取yaml
     *
     * @param algorithm   算法名称
     * @param stageOrder  算法阶段
     * @param versionName 算法版本名称
     * @return yaml字符串
     */
    @Override
    public String getYaml(String algorithm, Integer stageOrder, String versionName) {
        // 读取 minio 中 Yaml 文件
        if (StringUtils.isNotEmpty(algorithm)
                && stageOrder != null
        ) {
            return getYamlFromMinIO(
                    algorithm,
                    stageOrder,
                    versionName
            );
        }
        throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
    }

    /**
     * 获取minio 中算法的 yaml
     *
     * @param algorithmName 算法名称
     * @param stage         阶段排序值
     * @param versionName   版本名称
     * @return yaml 字符串
     */
    public String getYamlFromMinIO(String algorithmName, Integer stage, String versionName) {
        try {
            return minioUtil.readString(
                    bucketName,
                    pathUtil.getYamlPath(
                            StringUtils.EMPTY,
                            algorithmName.toLowerCase(),
                            versionName
                    ) + StageEnum.getStageName(stage) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX
            );
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL,"获取minio 中算法的 yaml 异常.Error message :{}",e.getMessage());
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }

    /**
     * 获取算法将要发布的版本号
     *
     * @param algorithmId 算法id
     * @return 下一个版本号名称
     */
    @Override
    public AlgorithmNextVersionVO getNextVersionName(Long algorithmId) {
        Algorithm algorithm = baseMapper.getOneById(algorithmId);
        if (ObjectUtils.isEmpty(algorithm)) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_DOES_NOT_EXIST_ERROR);
        }
        // 当前版本信息
        if (!StringUtils.isBlank(algorithmVersionService.selectOneById(algorithm.getAlgorithmVersionId()).getVersionName())) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_VERSION_ERROR);
        }
        // 当前算法最大的版本
        String maxVersionName = algorithmVersionService.getMaxVersionName(algorithmId);

        AlgorithmNextVersionVO nextVersionVO = new AlgorithmNextVersionVO() {{
            setName(algorithm.getName());
            setCurrentVersion(maxVersionName);
            setId(algorithmId);
        }};
        // 为空则取初始版本
        if (StringUtils.isBlank(maxVersionName)) {
            nextVersionVO.setNextVersion(TadlConstant.DEFAULT_VERSION);
        } else {
            int versionName = Integer.parseInt(maxVersionName.substring(1)) + NumberConstant.NUMBER_1;
            nextVersionVO.setNextVersion(TadlConstant.DATASET_VERSION_PREFIX + StringUtils.stringFillIn(
                    Integer.toString(versionName),
                    NumberConstant.NUMBER_4,
                    NumberConstant.NUMBER_0)
            );
        }
        return nextVersionVO;
    }


    /**
     * 版本发布
     *
     * @param algorithmVersionCreateDTO 算法版本创建DTO
     */
    @Override
    public void publish(AlgorithmVersionCreateDTO algorithmVersionCreateDTO) {
        // 校验版本信息
        AlgorithmNextVersionVO nextVersionName = getNextVersionName(algorithmVersionCreateDTO.getId());
        //版本信息都为空或着相同
        if (!StringUtils.isBlank(algorithmVersionCreateDTO.getCurrentVersion())) {
            if (!algorithmVersionCreateDTO.getCurrentVersion().equals(nextVersionName.getCurrentVersion())) {
                throw new BusinessException(TadlErrorEnum.VERSION_ERROR);
            }
        } else if (!StringUtils.isBlank(nextVersionName.getCurrentVersion())) {
            throw new BusinessException(TadlErrorEnum.VERSION_ERROR);
        }
        // 下一版本信息相同
        if (!algorithmVersionCreateDTO.getNextVersion().equals(nextVersionName.getNextVersion())) {
            throw new BusinessException(TadlErrorEnum.VERSION_ERROR);
        }
        //算法名称相同
        if (!algorithmVersionCreateDTO.getName().equals(nextVersionName.getName())) {
            throw new BusinessException(TadlErrorEnum.VERSION_ERROR);
        }
        doPublish(algorithmVersionCreateDTO);
    }

    /**
     * 发布版本
     *
     * @param algorithmVersionCreateDTO 版本创建对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void doPublish(AlgorithmVersionCreateDTO algorithmVersionCreateDTO){
        // 复制文件
        for (StageEnum stageEnum : StageEnum.values()) {
            try {
                String yaml = getYamlFromMinIO(algorithmVersionCreateDTO.getName(), stageEnum.getStageOrder(), StringUtils.EMPTY);
                minioUtil.writeString(
                        bucketName,
                        pathUtil.getYamlPath(
                                StringUtils.EMPTY,
                                algorithmVersionCreateDTO.getName().toLowerCase(),
                                algorithmVersionCreateDTO.getNextVersion()
                        ).replaceFirst(SymbolConstant.SLASH, StringUtils.EMPTY) + stageEnum.getName() + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX,
                        yaml
                );
            } catch (Exception e) {
                throw new BusinessException(TadlErrorEnum.FILE_OPERATION_ERROR);
            }
        }
        Algorithm algorithm = baseMapper.selectById(algorithmVersionCreateDTO);

        // 写版本表
        AlgorithmVersion algorithmVersion = algorithmVersionService.selectOneById(algorithm.getAlgorithmVersionId());
        algorithmVersion.setUpdateUserId(JwtUtils.getCurUserId());
        algorithmVersion.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        algorithmVersion.setVersionSource(algorithmVersionCreateDTO.getCurrentVersion());
        algorithmVersion.setVersionName(algorithmVersionCreateDTO.getNextVersion());
        algorithmVersionService.insert(algorithmVersion);

        // 写阶段表
        List<AlgorithmStage> algorithmStages = algorithmStageService.selectList(new LambdaQueryWrapper<AlgorithmStage>() {{
            eq(AlgorithmStage::getAlgorithmId, algorithmVersionCreateDTO.getId());
            eq(AlgorithmStage::getAlgorithmVersionId, algorithm.getAlgorithmVersionId());
        }});
        algorithmStages.forEach(stage -> {
            stage.setUpdateUserId(JwtUtils.getCurUserId());
            stage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            stage.setAlgorithmVersionId(algorithmVersion.getId());
        });
        algorithmStageService.insertStages(algorithmStages);

        // 更新算法版本信息
        baseMapper.updateById(new Algorithm() {{
            setId(algorithm.getId());
        }});
    }


    /**
     * 算法版本切换
     *
     * @param algorithmId        算法ID
     * @param algorithmVersionId 版本ID
     */
    @Override
    public void versionSwitch(Long algorithmId, Long algorithmVersionId) {
        Algorithm algorithm = baseMapper.getOneById(algorithmId);
        if (ObjectUtils.isEmpty(algorithm)) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_DOES_NOT_EXIST_ERROR);
        }
        AlgorithmVersion algorithmVersion = algorithmVersionService.selectOneById(algorithmVersionId);
        if (!algorithmId.equals(algorithmVersion.getAlgorithmId())) {
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
        baseMapper.updateById(new Algorithm(){{
            setId(algorithmId);
            setAlgorithmVersionId(algorithmVersionId);
        }});
    }



    @Override
    public void copyAlgorithm(String zipPath) {
        //算法名称  例如 enas
        String algorithm = zipPath.substring(zipPath.lastIndexOf(SymbolConstant.SLASH) + 1, zipPath.lastIndexOf(TadlConstant.ZIP_SUFFIX));

        //dubhe-cloud-dev/upload-temp/29/20210805140751962qqI3/enas\
        String targetPath = bucketName.concat(SymbolConstant.SLASH).concat(zipPath)
                .replace(TadlConstant.ZIP_SUFFIX, File.separator);

        //dubhe-cloud-dev/upload-temp/29/20210805140751962qqI3/enas/yaml/
        String targetYamlPath = targetPath + TadlConstant.ALGORITHM_YAML;
        //复制yaml文件到指定目录 从targetYamlPath 复制到yamPath目录下
        boolean copyResult = localFileUtil.copyPath(
                targetYamlPath,
                //dubhe-cloud-dev/TADL/algorithm/enas/yaml/
                pathUtil.getYamlPath(bucketName, algorithm, StringUtils.EMPTY)
        );
        if (!copyResult){
            throw new BusinessException("Failed to copy yaml");
        }
        //复制文件夹到指定目录
        boolean packageCopyPath = localFileUtil.copyPath(
                //dubhe-cloud-dev/upload-temp/29/20210805140751962qqI3/enas
                targetPath,
                //dubhe-cloud-dev/TADL/algorithm/enas\
                pathUtil.getAlgorithmPath(bucketName, algorithm + File.separator));
        if (!packageCopyPath){
            throw new BusinessException("Failed to copy "+algorithm+" package" );
        }


    }

    @Override
    public String readYaml(AlgorithmYamlQueryDTO algorithmYamlQueryDTO) {
        if (StringUtils.isBlank(algorithmYamlQueryDTO.getZipPath())){
         return   getYaml(algorithmYamlQueryDTO.getAlgorithm(),algorithmYamlQueryDTO.getStageOrder(),algorithmYamlQueryDTO.getVersionName());
        }
        String zipPath = algorithmYamlQueryDTO.getZipPath();
        try {
            //enas
            String algorithm = zipPath.substring(zipPath.lastIndexOf(SymbolConstant.SLASH) + 1, zipPath.lastIndexOf(TadlConstant.ZIP_SUFFIX));

            //upload-temp/29/20210805140751962qqI3/enas\yaml/
            String fullFilePath = zipPath.replace(TadlConstant.ZIP_SUFFIX, File.separator)+TadlConstant.ALGORITHM_YAML;

            return minioUtil.readString(bucketName,
                    //upload-temp/29/20210805140751962qqI3/enas\TADL/pytorch/enas/yaml/base.yaml
                    fullFilePath + StageEnum.getStageName(algorithmYamlQueryDTO.getStageOrder()) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX
            );
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL,"Failed to read yaml file.Error message :{}",e.getMessage());
            throw new BusinessException("Failed to read yaml file");
        }
    }

    @Override
    public Algorithm getOneById(Long algorithmId) {
        Algorithm algorithm = algorithmMapper.getOneById(algorithmId);
        if (ObjectUtils.isEmpty(algorithm)) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_DOES_NOT_EXIST_ERROR);
        }
        return algorithm;
    }

    /**
     * 创建算法
     *
     * @param algorithmCreateDTO 创建算法DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AlgorithmCreateDTO algorithmCreateDTO) {
        algorithmCreateDTO.setName(algorithmCreateDTO.getName().toLowerCase());
        //检验算法名称是否重复
        checkNameExist(algorithmCreateDTO.getName());
        //从临时目录拷贝到tadl算法路径
        copyAlgorithm(algorithmCreateDTO.getZipPath());
        //校验yaml 写minio
        algorithmCreateDTO.getStage().forEach(v -> {
            try {
                minioUtil.writeString(
                        bucketName,
                        pathUtil.getYamlPath(
                                StringUtils.EMPTY,
                                algorithmCreateDTO.getName().toLowerCase(),
                                null
                        ).replaceFirst(SymbolConstant.SLASH, StringUtils.EMPTY) + StageEnum.getStageName(v.getStageOrder()) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX,
                        v.getYaml()
                );
            } catch (Exception e) {
                throw new BusinessException(BaseErrorCodeEnum.ERROR);
            }
        });
        // 写算法表
        Algorithm algorithm = new Algorithm(algorithmCreateDTO);
        baseMapper.insert(algorithm);
        //写算法版本表
        AlgorithmVersion algorithmVersion = new AlgorithmVersion(algorithmCreateDTO);
        algorithmVersion.setAlgorithmId(algorithm.getId());
        algorithmVersion.setDescription(algorithmCreateDTO.getDescription());
        algorithmVersionService.insert(algorithmVersion);
        //更新算法表
        baseMapper.updateById(new Algorithm() {{
            setId(algorithm.getId());
            setAlgorithmVersionId(algorithmVersion.getId());
        }});
        //写算法阶段表
        algorithmStageService.insertStages(
                AlgorithmStage.getCreateAlgorithmStageList(
                        algorithmCreateDTO.getStage(),
                        algorithm.getId(),
                        algorithmVersion.getId()
                )
        );
    }
    /**
     * 校验算法名称是否存在
     *
     * @param name   实验名称
     */
    private void checkNameExist(String name) {
        int count = baseMapper.selectCount(new LambdaQueryWrapper<Algorithm>()
                .eq(Algorithm::getName, name)
          );
        if (count > NumberConstant.NUMBER_0) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_NAME_EXIST);
        }
    }

    /**
     * 算法列表查询
     *
     * @param content 搜索内容
     * @return 算法列表
     */
    @Override
    public List<AlgorithmVO> query(String content) {
        LambdaQueryWrapper<Algorithm> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Algorithm::getId);
        if (!StringUtils.isEmpty(content)) {
            wrapper.like(Algorithm::getName, content).or().like(Algorithm::getDescription, content);
        }
        // 算法列表
        List<AlgorithmVO> algorithmVOList = baseMapper.selectList(wrapper)
                .stream()
                .map(AlgorithmVO::from)
                .peek(v -> {
                    //所有算法版本列表
                    LambdaQueryWrapper<AlgorithmVersion> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(AlgorithmVersion::getAlgorithmId, v.getId());
                    queryWrapper.eq(AlgorithmVersion::getDeleted,false);
                    v.setAlgorithmVersionVOList(
                            algorithmVersionService.selectList(queryWrapper)
                                    .stream()
                                    .map(AlgorithmVersionVO::from)
                                    .collect(Collectors.toList()));
                })
                .collect(Collectors.toList());
        // 设置描述
        algorithmVOList.forEach(algorithmVO -> {
            for (AlgorithmVersionVO algorithmVersionVO : algorithmVO.getAlgorithmVersionVOList()){
                if (algorithmVO.getAlgorithmVersionId().equals(algorithmVersionVO.getId())){
                    algorithmVO.setDescription(algorithmVersionVO.getDescription());
                }
            }
        });
        return algorithmVOList;
    }

    /**
     * 更新算法
     *
     * @param algorithmUpdateDTO 更新算法DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AlgorithmUpdateDTO algorithmUpdateDTO) {
        // 校验是否是最新版本
        AlgorithmVersion algorithmVersionCheck = algorithmVersionService.selectOneById(algorithmUpdateDTO.getAlgorithmVersionId());
        if (ObjectUtils.isEmpty(algorithmVersionCheck)){
            throw new BusinessException("无法找到此算法信息");
        }
        if (StringUtils.isNotEmpty(algorithmVersionCheck.getVersionName())){
            throw new BusinessException("该算法不是最新版本，无法更新");
        }

        // 校验yaml 写minio
        algorithmUpdateDTO.getStage().forEach(v -> {
            try {
                minioUtil.writeString(
                        bucketName,
                        pathUtil.getYamlPath(
                                StringUtils.EMPTY,
                                algorithmUpdateDTO.getName().toLowerCase(),
                                //最新的版本即版本号为空
                                StringUtils.EMPTY
                        ).replaceFirst(SymbolConstant.SLASH, StringUtils.EMPTY) + StageEnum.getStageName(v.getStageOrder()) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX,
                        v.getYaml()
                );
            } catch (Exception e) {
                throw new BusinessException(BaseErrorCodeEnum.ERROR);
            }
        });
        // 更新算法表
        Algorithm algorithm = new Algorithm(algorithmUpdateDTO);
        baseMapper.updateById(algorithm);
        // 更新算法版本表
        AlgorithmVersion algorithmVersion = new AlgorithmVersion(algorithmUpdateDTO);
        algorithmVersion.setId(algorithm.getAlgorithmVersionId());
        algorithmVersionService.updateAlgorithmVersionById(algorithmVersion);
        // 更新算法阶段表
        algorithmStageService.updateAlgorithmStage(
                AlgorithmStage.getUpdateAlgorithmStageList(
                        algorithmUpdateDTO.getStage(),
                        algorithm.getId(),
                        algorithm.getAlgorithmVersionId()
                )
        );

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(AlgorithmVersionDeleteDTO algorithmVersionDeleteDTO) {
        Algorithm algorithm = baseMapper.getOneById(algorithmVersionDeleteDTO.getAlgorithmId());

        // 未传版本id，则删除整个算法
        if (algorithmVersionDeleteDTO.getAlgorithmVersionId() == null || algorithmVersionDeleteDTO.getAlgorithmVersionId() == 0){
            // 删除该算法下的所有算法阶段
            algorithmStageService.deleteByAlgorithmId(algorithmVersionDeleteDTO.getAlgorithmId());
            // 删除该算法的所有版本
            algorithmVersionService.deleteByAlgorithmId(algorithmVersionDeleteDTO.getAlgorithmId());
            // 删除该算法信息
            baseMapper.deleteById(algorithmVersionDeleteDTO.getAlgorithmId());
            // 删除Minio下文件,路径为TADL/algorithm/具体算法名称/yaml/版本名称
            try{
                String filePath = pathUtil.getAlgorithmPath("", algorithm.getName());
                List<FileDTO> fileDTOList = minioUtil.fileList(bucketName,filePath,false);
                for (FileDTO fileDTO : fileDTOList){
                    minioUtil.del(bucketName,fileDTO.getPath());
                }
            }catch (Exception e){
                LogUtil.error(LogEnum.TADL,"The algorithm delete operation is abnormal.The exception message:{}",e.getMessage());
                throw new BusinessException("RecycleTask error");
            }
            return;
        }

        AlgorithmVersion algorithmVersion = algorithmVersionService.selectOneById(algorithmVersionDeleteDTO.getAlgorithmVersionId());
        if (Objects.isNull(algorithmVersion)){
            throw new BusinessException("can not find version:" + algorithmVersionDeleteDTO.getAlgorithmVersionId() + " record");
        }

        // 删除算法版本
        algorithmVersionService.updateAlgorithmVersion(new LambdaUpdateWrapper<AlgorithmVersion>(){{
            eq(AlgorithmVersion::getId,algorithmVersion.getId())
                    .set(AlgorithmVersion::getDeleted,Boolean.TRUE);
        }});
        // 删除算法阶段
        algorithmStageService.updateAlgorithmStage(new LambdaUpdateWrapper<AlgorithmStage>(){{
            eq(AlgorithmStage::getAlgorithmVersionId,algorithmVersion.getId())
                    .set(AlgorithmStage::getDeleted,Boolean.TRUE);
        }});

        // 查询算法的最新版本
        AlgorithmVersion lastAlgorithmVersion = algorithmVersionService.selectOne(new LambdaQueryWrapper<AlgorithmVersion>()
                .eq(AlgorithmVersion::getAlgorithmId, algorithm.getId())
                        .isNull(AlgorithmVersion::getVersionName)
                        .eq(AlgorithmVersion::getDeleted, false)
        );
        // 变更算法的算法版本id为最新版本
        baseMapper.updateById(new Algorithm(){{
            setId(lastAlgorithmVersion.getAlgorithmId());
            setAlgorithmVersionId(lastAlgorithmVersion.getId());
        }});
        // 删除Minio下文件,路径为TADL/algorithm/具体算法名称/yaml/版本名称
        try{
            String filePath =k8sNameTool.getAbsolutePath(pathUtil.getYamlPath(StringUtils.EMPTY,algorithm.getName(),algorithmVersion.getVersionName()));
            createRecycleTask(filePath,algorithm,algorithmVersion);
        }catch (Exception e){
            LogUtil.error(LogEnum.TADL,"The algorithm delete operation is abnormal.The exception message:{}",e.getMessage());
            throw new BusinessException("RecycleTask error");
        }

    }


    private void createRecycleTask(String recyclePath,Algorithm algorithm,AlgorithmVersion algorithmVersion) {
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_TADL.getValue())
                .recycleDelayDate(recycleConfig.getTadlValid())  //默认3天
                .recycleNote(RecycleTool.generateRecycleNote("删除算法文件", algorithm.getName() + ":" + algorithmVersion.getVersionName(), algorithmVersion.getId()))
                .recycleCustom(RecycleResourceEnum.TADL_ALGORITHM_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.TADL_ALGORITHM_RECYCLE_FILE.getClassName())
                .remark(String.valueOf(algorithmVersion.getId()))
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleCondition(recyclePath)
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("删除算法文件", algorithm.getName() + ":" + algorithmVersion.getVersionName(), algorithmVersion.getId()))
                .remark(String.valueOf(algorithmVersion.getId()))
                .build()
        );
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    private void createRecycleTask(String recyclePath,Algorithm algorithm) {
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_TADL.getValue())
                .recycleDelayDate(0)  // 立即删除
                .recycleNote(RecycleTool.generateRecycleNote("删除算法文件", algorithm.getName(), algorithm.getId()))
                .recycleCustom(RecycleResourceEnum.TADL_ALGORITHM_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.TADL_ALGORITHM_RECYCLE_FILE.getClassName())
                .remark(String.valueOf(algorithm.getId()))
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleCondition(recyclePath)
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("删除算法文件", algorithm.getName(), algorithm.getId()))
                .remark(String.valueOf(algorithm.getId()))
                .build()
        );
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    /**
     * 查询算法 三个/一个 阶段数据
     *
     * @param algorithmId        算法id
     * @param stageOrder         算法阶段
     * @param algorithmVersionId 算法版本
     * @return 算法三个阶段数据
     */
    @Override
    public AlgorithmVO query(Integer algorithmId, Integer stageOrder, Long algorithmVersionId) {
        LambdaQueryWrapper<AlgorithmStage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AlgorithmStage::getStageOrder);
        wrapper.eq(AlgorithmStage::getAlgorithmId, algorithmId);
        //判断是否只查某阶段
        if (stageOrder != null) {
            wrapper.eq(AlgorithmStage::getStageOrder, stageOrder);
        }
        wrapper.eq(AlgorithmStage::getAlgorithmVersionId, algorithmVersionId);
        //获取算法对象
        Algorithm algorithm = baseMapper.selectById(algorithmId);
        //获取算法版本对象
        AlgorithmVersion algorithmVersion = algorithmVersionService.selectOneById(algorithmVersionId);
        List<AlgorithmStageVO> algorithmStageVOList = algorithmStageService.selectList(wrapper)
                .stream()
                .map(AlgorithmStageVO::from)
                .peek(v ->
                        v.setYaml(
                                getYaml(
                                        algorithm.getName().toLowerCase(),
                                        v.getStageOrder(),
                                        algorithmVersion.getVersionName()
                                )
                        )
                ).collect(Collectors.toList());
        String baseYaml = getYaml(algorithm.getName(), StageEnum.BASE.getStageOrder(), algorithmVersion.getVersionName());
        AlgorithmVO algorithmVO = AlgorithmVO.from(algorithm);
        algorithmVO.setYaml(baseYaml);
        algorithmVO.setStage(algorithmStageVOList);
        return algorithmVO;
    }
}
