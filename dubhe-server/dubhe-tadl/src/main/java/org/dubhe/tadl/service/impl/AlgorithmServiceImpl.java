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
 * @description ???????????????????????????
 * @date 2021-03-22
 */
@Service
public class AlgorithmServiceImpl extends ServiceImpl<AlgorithmMapper, Algorithm> implements AlgorithmService {


    /**
     * ??????????????????
     */
    @Autowired
    private LocalFileUtil localFileUtil;

    /**
     * ??????????????????
     */
    @Autowired
    private AlgorithmStageService algorithmStageService;

    /**
     * ??????????????????
     */
    @Autowired
    private AlgorithmVersionService algorithmVersionService;

    /**
     * minio??????
     */
    @Autowired
    private MinioUtil minioUtil;

    /**
     * minIO??????
     */
    @Value("${minio.bucketName}")
    private String bucketName;

    @Resource
    private K8sNameTool k8sNameTool;
    /**
     * ???????????????
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
     * ???????????????
     *
     * @param zipPath ??????
     * @return ??????????????????
     */
    @Override
    public void unzip(String zipPath) {

        if (StringUtils.isBlank(zipPath)){
            throw new BusinessException("zip????????????????????????");
        }
        String algorithm = zipPath.substring(zipPath.lastIndexOf(SymbolConstant.SLASH) + 1, zipPath.lastIndexOf(TadlConstant.ZIP_SUFFIX));
        // ??????????????????????????????????????????
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
     * ???????????? id ??????????????????
     *
     * @param algorithmId ??????id
     * @return ????????????
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
     * ??????yaml
     *
     * @param algorithm   ????????????
     * @param stageOrder  ????????????
     * @param versionName ??????????????????
     * @return yaml?????????
     */
    @Override
    public String getYaml(String algorithm, Integer stageOrder, String versionName) {
        // ?????? minio ??? Yaml ??????
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
     * ??????minio ???????????? yaml
     *
     * @param algorithmName ????????????
     * @param stage         ???????????????
     * @param versionName   ????????????
     * @return yaml ?????????
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
            LogUtil.error(LogEnum.TADL,"??????minio ???????????? yaml ??????.Error message :{}",e.getMessage());
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param algorithmId ??????id
     * @return ????????????????????????
     */
    @Override
    public AlgorithmNextVersionVO getNextVersionName(Long algorithmId) {
        Algorithm algorithm = baseMapper.getOneById(algorithmId);
        if (ObjectUtils.isEmpty(algorithm)) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_DOES_NOT_EXIST_ERROR);
        }
        // ??????????????????
        if (!StringUtils.isBlank(algorithmVersionService.selectOneById(algorithm.getAlgorithmVersionId()).getVersionName())) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_VERSION_ERROR);
        }
        // ???????????????????????????
        String maxVersionName = algorithmVersionService.getMaxVersionName(algorithmId);

        AlgorithmNextVersionVO nextVersionVO = new AlgorithmNextVersionVO() {{
            setName(algorithm.getName());
            setCurrentVersion(maxVersionName);
            setId(algorithmId);
        }};
        // ????????????????????????
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
     * ????????????
     *
     * @param algorithmVersionCreateDTO ??????????????????DTO
     */
    @Override
    public void publish(AlgorithmVersionCreateDTO algorithmVersionCreateDTO) {
        // ??????????????????
        AlgorithmNextVersionVO nextVersionName = getNextVersionName(algorithmVersionCreateDTO.getId());
        //?????????????????????????????????
        if (!StringUtils.isBlank(algorithmVersionCreateDTO.getCurrentVersion())) {
            if (!algorithmVersionCreateDTO.getCurrentVersion().equals(nextVersionName.getCurrentVersion())) {
                throw new BusinessException(TadlErrorEnum.VERSION_ERROR);
            }
        } else if (!StringUtils.isBlank(nextVersionName.getCurrentVersion())) {
            throw new BusinessException(TadlErrorEnum.VERSION_ERROR);
        }
        // ????????????????????????
        if (!algorithmVersionCreateDTO.getNextVersion().equals(nextVersionName.getNextVersion())) {
            throw new BusinessException(TadlErrorEnum.VERSION_ERROR);
        }
        //??????????????????
        if (!algorithmVersionCreateDTO.getName().equals(nextVersionName.getName())) {
            throw new BusinessException(TadlErrorEnum.VERSION_ERROR);
        }
        doPublish(algorithmVersionCreateDTO);
    }

    /**
     * ????????????
     *
     * @param algorithmVersionCreateDTO ??????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void doPublish(AlgorithmVersionCreateDTO algorithmVersionCreateDTO){
        // ????????????
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

        // ????????????
        AlgorithmVersion algorithmVersion = algorithmVersionService.selectOneById(algorithm.getAlgorithmVersionId());
        algorithmVersion.setUpdateUserId(JwtUtils.getCurUserId());
        algorithmVersion.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        algorithmVersion.setVersionSource(algorithmVersionCreateDTO.getCurrentVersion());
        algorithmVersion.setVersionName(algorithmVersionCreateDTO.getNextVersion());
        algorithmVersionService.insert(algorithmVersion);

        // ????????????
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

        // ????????????????????????
        baseMapper.updateById(new Algorithm() {{
            setId(algorithm.getId());
        }});
    }


    /**
     * ??????????????????
     *
     * @param algorithmId        ??????ID
     * @param algorithmVersionId ??????ID
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
        //????????????  ?????? enas
        String algorithm = zipPath.substring(zipPath.lastIndexOf(SymbolConstant.SLASH) + 1, zipPath.lastIndexOf(TadlConstant.ZIP_SUFFIX));

        //dubhe-cloud-dev/upload-temp/29/20210805140751962qqI3/enas\
        String targetPath = bucketName.concat(SymbolConstant.SLASH).concat(zipPath)
                .replace(TadlConstant.ZIP_SUFFIX, File.separator);

        //dubhe-cloud-dev/upload-temp/29/20210805140751962qqI3/enas/yaml/
        String targetYamlPath = targetPath + TadlConstant.ALGORITHM_YAML;
        //??????yaml????????????????????? ???targetYamlPath ?????????yamPath?????????
        boolean copyResult = localFileUtil.copyPath(
                targetYamlPath,
                //dubhe-cloud-dev/TADL/algorithm/enas/yaml/
                pathUtil.getYamlPath(bucketName, algorithm, StringUtils.EMPTY)
        );
        if (!copyResult){
            throw new BusinessException("Failed to copy yaml");
        }
        //??????????????????????????????
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
     * ????????????
     *
     * @param algorithmCreateDTO ????????????DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AlgorithmCreateDTO algorithmCreateDTO) {
        algorithmCreateDTO.setName(algorithmCreateDTO.getName().toLowerCase());
        //??????????????????????????????
        checkNameExist(algorithmCreateDTO.getName());
        //????????????????????????tadl????????????
        copyAlgorithm(algorithmCreateDTO.getZipPath());
        //??????yaml ???minio
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
        // ????????????
        Algorithm algorithm = new Algorithm(algorithmCreateDTO);
        baseMapper.insert(algorithm);
        //??????????????????
        AlgorithmVersion algorithmVersion = new AlgorithmVersion(algorithmCreateDTO);
        algorithmVersion.setAlgorithmId(algorithm.getId());
        algorithmVersion.setDescription(algorithmCreateDTO.getDescription());
        algorithmVersionService.insert(algorithmVersion);
        //???????????????
        baseMapper.updateById(new Algorithm() {{
            setId(algorithm.getId());
            setAlgorithmVersionId(algorithmVersion.getId());
        }});
        //??????????????????
        algorithmStageService.insertStages(
                AlgorithmStage.getCreateAlgorithmStageList(
                        algorithmCreateDTO.getStage(),
                        algorithm.getId(),
                        algorithmVersion.getId()
                )
        );
    }
    /**
     * ??????????????????????????????
     *
     * @param name   ????????????
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
     * ??????????????????
     *
     * @param content ????????????
     * @return ????????????
     */
    @Override
    public List<AlgorithmVO> query(String content) {
        LambdaQueryWrapper<Algorithm> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Algorithm::getId);
        if (!StringUtils.isEmpty(content)) {
            wrapper.like(Algorithm::getName, content).or().like(Algorithm::getDescription, content);
        }
        // ????????????
        List<AlgorithmVO> algorithmVOList = baseMapper.selectList(wrapper)
                .stream()
                .map(AlgorithmVO::from)
                .peek(v -> {
                    //????????????????????????
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
        // ????????????
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
     * ????????????
     *
     * @param algorithmUpdateDTO ????????????DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AlgorithmUpdateDTO algorithmUpdateDTO) {
        // ???????????????????????????
        AlgorithmVersion algorithmVersionCheck = algorithmVersionService.selectOneById(algorithmUpdateDTO.getAlgorithmVersionId());
        if (ObjectUtils.isEmpty(algorithmVersionCheck)){
            throw new BusinessException("???????????????????????????");
        }
        if (StringUtils.isNotEmpty(algorithmVersionCheck.getVersionName())){
            throw new BusinessException("??????????????????????????????????????????");
        }

        // ??????yaml ???minio
        algorithmUpdateDTO.getStage().forEach(v -> {
            try {
                minioUtil.writeString(
                        bucketName,
                        pathUtil.getYamlPath(
                                StringUtils.EMPTY,
                                algorithmUpdateDTO.getName().toLowerCase(),
                                //?????????????????????????????????
                                StringUtils.EMPTY
                        ).replaceFirst(SymbolConstant.SLASH, StringUtils.EMPTY) + StageEnum.getStageName(v.getStageOrder()) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX,
                        v.getYaml()
                );
            } catch (Exception e) {
                throw new BusinessException(BaseErrorCodeEnum.ERROR);
            }
        });
        // ???????????????
        Algorithm algorithm = new Algorithm(algorithmUpdateDTO);
        baseMapper.updateById(algorithm);
        // ?????????????????????
        AlgorithmVersion algorithmVersion = new AlgorithmVersion(algorithmUpdateDTO);
        algorithmVersion.setId(algorithm.getAlgorithmVersionId());
        algorithmVersionService.updateAlgorithmVersionById(algorithmVersion);
        // ?????????????????????
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

        // ????????????id????????????????????????
        if (algorithmVersionDeleteDTO.getAlgorithmVersionId() == null || algorithmVersionDeleteDTO.getAlgorithmVersionId() == 0){
            // ???????????????????????????????????????
            algorithmStageService.deleteByAlgorithmId(algorithmVersionDeleteDTO.getAlgorithmId());
            // ??????????????????????????????
            algorithmVersionService.deleteByAlgorithmId(algorithmVersionDeleteDTO.getAlgorithmId());
            // ?????????????????????
            baseMapper.deleteById(algorithmVersionDeleteDTO.getAlgorithmId());
            // ??????Minio?????????,?????????TADL/algorithm/??????????????????/yaml/????????????
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

        // ??????????????????
        algorithmVersionService.updateAlgorithmVersion(new LambdaUpdateWrapper<AlgorithmVersion>(){{
            eq(AlgorithmVersion::getId,algorithmVersion.getId())
                    .set(AlgorithmVersion::getDeleted,Boolean.TRUE);
        }});
        // ??????????????????
        algorithmStageService.updateAlgorithmStage(new LambdaUpdateWrapper<AlgorithmStage>(){{
            eq(AlgorithmStage::getAlgorithmVersionId,algorithmVersion.getId())
                    .set(AlgorithmStage::getDeleted,Boolean.TRUE);
        }});

        // ???????????????????????????
        AlgorithmVersion lastAlgorithmVersion = algorithmVersionService.selectOne(new LambdaQueryWrapper<AlgorithmVersion>()
                .eq(AlgorithmVersion::getAlgorithmId, algorithm.getId())
                        .isNull(AlgorithmVersion::getVersionName)
                        .eq(AlgorithmVersion::getDeleted, false)
        );
        // ???????????????????????????id???????????????
        baseMapper.updateById(new Algorithm(){{
            setId(lastAlgorithmVersion.getAlgorithmId());
            setAlgorithmVersionId(lastAlgorithmVersion.getId());
        }});
        // ??????Minio?????????,?????????TADL/algorithm/??????????????????/yaml/????????????
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
                .recycleDelayDate(recycleConfig.getTadlValid())  //??????3???
                .recycleNote(RecycleTool.generateRecycleNote("??????????????????", algorithm.getName() + ":" + algorithmVersion.getVersionName(), algorithmVersion.getId()))
                .recycleCustom(RecycleResourceEnum.TADL_ALGORITHM_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.TADL_ALGORITHM_RECYCLE_FILE.getClassName())
                .remark(String.valueOf(algorithmVersion.getId()))
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleCondition(recyclePath)
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("??????????????????", algorithm.getName() + ":" + algorithmVersion.getVersionName(), algorithmVersion.getId()))
                .remark(String.valueOf(algorithmVersion.getId()))
                .build()
        );
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    private void createRecycleTask(String recyclePath,Algorithm algorithm) {
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_TADL.getValue())
                .recycleDelayDate(0)  // ????????????
                .recycleNote(RecycleTool.generateRecycleNote("??????????????????", algorithm.getName(), algorithm.getId()))
                .recycleCustom(RecycleResourceEnum.TADL_ALGORITHM_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.TADL_ALGORITHM_RECYCLE_FILE.getClassName())
                .remark(String.valueOf(algorithm.getId()))
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleCondition(recyclePath)
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("??????????????????", algorithm.getName(), algorithm.getId()))
                .remark(String.valueOf(algorithm.getId()))
                .build()
        );
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    /**
     * ???????????? ??????/?????? ????????????
     *
     * @param algorithmId        ??????id
     * @param stageOrder         ????????????
     * @param algorithmVersionId ????????????
     * @return ????????????????????????
     */
    @Override
    public AlgorithmVO query(Integer algorithmId, Integer stageOrder, Long algorithmVersionId) {
        LambdaQueryWrapper<AlgorithmStage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AlgorithmStage::getStageOrder);
        wrapper.eq(AlgorithmStage::getAlgorithmId, algorithmId);
        //???????????????????????????
        if (stageOrder != null) {
            wrapper.eq(AlgorithmStage::getStageOrder, stageOrder);
        }
        wrapper.eq(AlgorithmStage::getAlgorithmVersionId, algorithmVersionId);
        //??????????????????
        Algorithm algorithm = baseMapper.selectById(algorithmId);
        //????????????????????????
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
