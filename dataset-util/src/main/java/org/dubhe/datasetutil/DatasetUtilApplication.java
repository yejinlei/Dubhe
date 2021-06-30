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
package org.dubhe.datasetutil;

import lombok.extern.slf4j.Slf4j;
import org.dubhe.datasetutil.common.util.PrintUtils;
import org.dubhe.datasetutil.common.util.SpringContextHolder;
import org.dubhe.datasetutil.handle.CustomDatasetImportHandle;
import org.dubhe.datasetutil.handle.DatasetImageUploadHandle;
import org.dubhe.datasetutil.handle.DatasetImportHandle;
import org.dubhe.datasetutil.handle.PresetDatasetImportHandle;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * @description 文档导入工程启动类
 * @date 2020-09-17
 */
@Slf4j
@SpringBootApplication
@MapperScan("org.dubhe.datasetutil.dao")
public class DatasetUtilApplication {

    /**
     * 主函数
     *
     * @param args 入参
     */
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(org.dubhe.datasetutil.DatasetUtilApplication.class, args);
        SpringContextHolder springContextHolder = new SpringContextHolder();
        springContextHolder.setApplicationContext(applicationContext);
        execute(applicationContext);
    }

    /**
     * 执行脚本
     *
     * @param applicationContext 请求上下文
     */
    public static void execute(ApplicationContext applicationContext) {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println(" ");
            System.out.println("###请输入需要执行的任务###");
            System.out.println("#  输入1：上传文件          ");
            System.out.println("#  输入2：导入数据集        ");
            System.out.println("#  输入exit：退出        ");
            System.out.println("##########################");
            String a = scanner.nextLine();
            switch (a) {
                case "1":
                    uploadDatasetImage(scanner, applicationContext);
                    break;
                case "2":
                    executeImportDataset(applicationContext);
                    break;
                case "exit":
                default:
                    System.exit(0);
                    break;
            }
        }
    }

    public static void executeImportDataset(ApplicationContext applicationContext) {
        Boolean importFlag = true;
        while (importFlag) {
            Scanner scanner = new Scanner(System.in);
            System.out.println(" ");
            System.out.println("###请输入导入数据集类型###");
            System.out.println("#  输入1: 导入普通数据集          ");
            System.out.println("#  输入2: 导入预置数据集        ");
            System.out.println("#  输入3: 导入自定义数据集        ");
            System.out.println("#  输入命令：exit 返回        ");
            System.out.println("##########################");

            switch (scanner.nextLine()) {
                case "1":
                    importDataset(scanner, applicationContext);
                    break;
                case "2":
                    importPresetDataset(scanner, applicationContext);
                    break;
                case "3":
                    importCustomDataset(scanner, applicationContext);
                    break;
                case "exit":
                default:
                    importFlag = false;
                    break;
            }
        }
    }

    /**
     * 导入预置数据集
     *
     * @param scanner               输入控制台
     * @param applicationContext    请求上下文
     */
    private static void importPresetDataset(Scanner scanner, ApplicationContext applicationContext) {
        PresetDatasetImportHandle datasetImportHandle = (PresetDatasetImportHandle) applicationContext.getBean("presetDatasetImportHandle");
        datasetImportHandle.importPresetDataset(scanner);
    }

    /**
     * 导入图片
     *
     * @param scanner            输入控制台
     * @param applicationContext 请求上下文
     */
    public static void uploadDatasetImage(Scanner scanner, ApplicationContext applicationContext) {
        DatasetImageUploadHandle datasetImageUploadHandle = (DatasetImageUploadHandle) applicationContext.getBean("datasetImageUploadHandle");
        try {
            datasetImageUploadHandle.importPicture(scanner);
        } catch (Exception e) {
            log.error("");
            PrintUtils.printLine("  Error：" + e.getMessage(), PrintUtils.RED);
            log.error("");
        }
    }

    /**
     * 导入数据集
     *
     * @param scanner            输入控制台
     * @param applicationContext 请求上下文
     */
    public static void importDataset(Scanner scanner, ApplicationContext applicationContext) {
        DatasetImportHandle datasetImportHandle = (DatasetImportHandle) applicationContext.getBean("datasetImportHandle");
        try{
            datasetImportHandle.importDataset(scanner);
        } catch (Exception e) {
            log.error("");
            PrintUtils.printLine("  Error：" + e.getMessage(), PrintUtils.RED);
            log.error("");
        }
    }

    /**
     * 导入之定义数据集
     *
     * @param scanner             输入控制台
     * @param applicationContext  请求上下文
     */
    public static void importCustomDataset(Scanner scanner, ApplicationContext applicationContext) {
        System.out.println(" ");
        System.out.println("# 请输入数据集ID #");
        String datasetIdStr = scanner.nextLine();
        Long datasetId = Long.parseLong(datasetIdStr);
        System.out.println(" ");
        System.out.println("# 请输入待上传本地文件的绝对路径 #");
        String filePath = scanner.nextLine();
        CustomDatasetImportHandle customDatasetImportHandle = (CustomDatasetImportHandle) applicationContext.getBean("customDatasetImportHandle");
        try {
            customDatasetImportHandle.execute(new Object[]{datasetId, filePath});
        } catch (Exception e) {
            log.error("");
            PrintUtils.printLine("  Error：" + e.getMessage(), PrintUtils.RED);
            log.error("");
        }
    }

}
