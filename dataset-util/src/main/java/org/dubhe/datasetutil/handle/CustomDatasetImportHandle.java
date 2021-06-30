package org.dubhe.datasetutil.handle;

import lombok.extern.slf4j.Slf4j;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.common.constant.AnnotateTypeEnum;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.common.constant.DataStateCodeConstant;
import org.dubhe.datasetutil.common.constant.DatatypeEnum;
import org.dubhe.datasetutil.common.exception.BusinessException;
import org.dubhe.datasetutil.common.util.*;
import org.dubhe.datasetutil.domain.entity.Dataset;
import org.dubhe.datasetutil.service.DatasetService;
import org.dubhe.datasetutil.service.DatasetVersionService;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description 自定义数据集导入
 * @date 2021-03-23
 */
@Slf4j
@Component
@EnableAspectJAutoProxy(exposeProxy = true)
public class CustomDatasetImportHandle {

    @Autowired
    DatasetService datasetService;
    @Autowired
    DatasetVersionService datasetVersionService;
    @Autowired
    MinioUtil minioUtil;

    /**
     * 自定义数据集导入
     * 1.修改数据集状态为已完成
     * 2.创建版本数据
     * 3.文件导入到版本目录
     *
     * @param args 参数 (1)数据集ID (2)文件路径
     */
    public void execute(Object[] args) throws Exception {
        valid(args);
        ((CustomDatasetImportHandle) AopContext.currentProxy()).sqlExecute(args);
        fileExecute(args);
        log.warn("");
        PrintUtils.printLine("  Success: 执行成功  ", PrintUtils.GREEN);
        log.warn("");
        System.out.println("#   是否结束? Y / N  #");
        Scanner scannerExit = new Scanner(System.in);
        if (BusinessConstant.Y.toLowerCase().equals(scannerExit.nextLine().toLowerCase())) {
            System.exit(MagicNumConstant.ZERO);
        }
    }

    /**
     * 数据库处理
     * 1.修改数据集状态为已完成
     * 2.增加数据集版本数据
     *   已存在的情况下，不重复添加
     *
     * @param args 参数 (1)数据集ID (2)文件路径
     */
    @Transactional(rollbackFor = Exception.class)
    public void sqlExecute(Object[] args) {
        Dataset dataset = datasetService.findDatasetById((long)args[0]);
        if (Objects.isNull(dataset)) {
            throw new BusinessException("数据集不存在");
        }
        //更新数据集状态为已完成
        if (!DataStateCodeConstant.ANNOTATION_COMPLETE_STATE.equals(dataset.getStatus())) {
            dataset.setStatus(DataStateCodeConstant.ANNOTATION_COMPLETE_STATE);
            dataset.setCurrentVersionName(BusinessConstant.DEFAULT_VERSION);
            datasetService.updateDataset(dataset);
        }
        //生成版本信息 只会生成V0001
        if (Objects.isNull(datasetVersionService.getByDatasetIdAndVersionNum(dataset.getId(), BusinessConstant.DEFAULT_VERSION))) {
            datasetVersionService.insertVersion(dataset.getId(), BusinessConstant.DEFAULT_VERSION, "自定义");
        }

    }

    /**
     * 遍历用户文件夹上传所有问题
     *
     * @param args 参数 (1)数据集ID (2)文件路径
     */
    public void fileExecute(Object[] args) throws Exception {
        List<String> filePaths = FileUtil.traverseFolder((String) args[1]);
        List<Callable<Integer>> partitions = new ArrayList<>();
        int oneSize = ThreadUtils.createThread(filePaths.size());
        List<String> need = new ArrayList<>();
        Integer integer = new Integer(0);
        //初始化进度条
        ProcessBarUtil.initProcess("自定义导入", (long) filePaths.size());
        for (String filePath : filePaths) {
            need.add(filePath);
            if (need.size() == oneSize || integer.intValue() == filePaths.size() - 1) {
                List<String> now = new ArrayList<>(need);
                need.clear();
                partitions.add(() -> run(now, args));
            }
            integer ++;
        }
        ThreadUtils.runMultiThread(partitions);
    }

    public Integer run(List<String> filePaths, Object[] args) {
        log.info("#-------------开始处理,时间[" + DateUtil.getNowStr() + "]-------------#");
        log.info("#-------------文件数量[" + filePaths.size() + "]------------------------");
        Integer success = 0;
        for (String str : filePaths) {
            try {
                String objectName = "dataset/" + (long) args[0] + "/versionFile/V0001" + str.replace((String)args[1], "").replaceAll("\\\\", "/");
                minioUtil.upLoadFileByInputStream(objectName, str);
                ProcessBarUtil.processBar01(1L);
                success ++;
            } catch (Exception e) {
                log.error(str + "upload error {}", e);
            }
        }
        return success;
    }

    /**
     * 数据校验
     * 1.参数校验
     * 2.参数对应数据集是否存在
     * 3.用户输入目录下是否有问题
     * 4.数据集标注类型和数据类型是否正确
     *
     * @param args 参数 (1)数据集ID (2)文件路径
     */
    public void valid(Object[] args) {
        if (args == null || args.length != 2) {
            throw new BusinessException("参数数量不匹配");
        }
        Long datasetId = null;
        try {
            datasetId = (long) args[0];
        } catch (Exception e) {
            log.error("数据集ID输入不正确, {}", e);
            throw new BusinessException("数据集ID输入不正确");
        }
        Dataset dataset = datasetService.findDatasetById(datasetId);
        if (Objects.isNull(dataset)) {
            throw new BusinessException("输入数据集不存在");
        }
        if (!AnnotateTypeEnum.AUTO_IMPORT.getValue().equals(dataset.getAnnotateType()) || !DatatypeEnum.AUTO_IMPORT.getValue().equals(dataset.getDataType())) {
            throw new BusinessException("请确认该数据及的标注类型以及数据类型都是自定义导入");
        }
        String filePath = (String) args[1];
        if (!cn.hutool.core.io.FileUtil.exist(filePath) || !cn.hutool.core.io.FileUtil.isDirectory(filePath)) {
            throw new BusinessException("请确保您输入的数据集路径是否正确");
        }
    }

}
