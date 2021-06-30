package org.dubhe.datasetutil.common.util;

import me.tongfei.progressbar.ProgressBar;

/**
 * @description 进度条工具类
 * @date 2021-03-23
 */
public class ProcessBarUtil {

    public static ProgressBar pb = null;

    /**
     * 初始化进度条工具
     *
     * @param task
     * @param maxValue
     */
    public static void initProcess(String task, Long maxValue) {
        pb = new ProgressBar(task, maxValue);
    }

    /**
     * 更新进度条
     *
     * @param step
     */
    public static void processBar01(Long step) {
        pb.stepBy(step);
    }

}
