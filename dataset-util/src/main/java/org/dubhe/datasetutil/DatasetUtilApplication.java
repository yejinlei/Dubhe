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
import org.dubhe.datasetutil.common.util.SpringContextHolder;
import org.dubhe.datasetutil.handle.DatasetImageUploadHandle;
import org.dubhe.datasetutil.handle.DatasetImportHandle;
import org.dubhe.datasetutil.common.util.PrintUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

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
        ApplicationContext applicationContext = SpringApplication.run(DatasetUtilApplication.class, args);
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
            log.warn("###################请输入需要执行的任务#############");
            log.warn("#  输入1.执行上传图片          ");
            log.warn("#  输入2.执行导入数据集        ");
            log.warn("#  输入命令 ：exit 退出        ");
            log.warn("################################################");
            String a = scanner.nextLine();
            switch (a) {
                case "1":
                    uploadDatasetImage(scanner, applicationContext);
                    break;
                case "2":
                    importDataset(scanner, applicationContext);
                    break;
                case "exit":
                default:
                    System.exit(0);
                    break;
            }
        }
    }

    /**
     * 导入图片
     *
     * @param scanner            输入控制台
     * @param applicationContext 请求上下文
     */
    public static void uploadDatasetImage(Scanner scanner, ApplicationContext applicationContext) {
        log.warn("# 请输入数据集ID #");
        String datasetIdStr = scanner.nextLine();
        Long datasetId = Long.parseLong(datasetIdStr);
        log.warn("# 请输入要上传的图片地址 #");
        String filePath = scanner.nextLine();
        DatasetImageUploadHandle datasetImageUploadHandle = (DatasetImageUploadHandle) applicationContext.getBean("datasetImageUploadHandle");
        try {
            datasetImageUploadHandle.execute(filePath, datasetId);
        } catch (Exception e) {
            log.error("", e);
            log.error("# 数据集上传失败，请重新尝试.....");
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

}
