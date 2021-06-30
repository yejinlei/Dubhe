package org.dubhe.datasetutil.common.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 文件处理工具
 * @date 2021-03-23
 */
public class FileUtil {

    /**
     * 遍历文件
     *
     * @param path 文件路径
     */
    public static List<String> traverseFolder(String path) {
        List<String> filePaths = new ArrayList<>();
        // 实例化file对象，指明要操作的文件路径
        File file = new File(path);
        // 判断是否有文件
        if (file.exists()) {
            // 获取该目录下的所有文件或者文件目录的File数组
            File[] files = file.listFiles();
            // 判断文件是否为空
            if (files != null && files.length > 0) {
                // 利用foreach 进行循环遍历
                for (File f : files) {
                    // 判断是文件还是文件夹
                    if (f.isDirectory()) {
                        // 递归调用
                        filePaths.addAll(traverseFolder(f.getPath()));
                    } else {
                        filePaths.add(f.getAbsolutePath());
                    }
                }
            }
        }
        return filePaths;
    }

}
