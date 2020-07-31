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

package org.dubhe.data.util;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 视频采样工具类
 * @date 2020-04-27
 */
public class JavaCvUtil {

    /**
     * 截取视频获得指定帧的图片
     *
     * @param videoPath 原视频路径
     * @param space     截取桢间隔
     * @return List<String> 采样后图片名字列表
     */
    public static List<String> getVideoPic(String videoPath, int space) throws IOException {
        File file = new File(videoPath);
        LogUtil.info(LogEnum.BIZ_DATASET, "get video file success");
        String picPath = StringUtils.substringBeforeLast(file.getParent(), File.separator)
                + File.separator + "origin" + File.separator;
        File directory = new File(picPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File isExistFile = new File(picPath);
        File[] needDeleteFiles = isExistFile.listFiles();
        for (File needDeleteFile : needDeleteFiles) {
            needDeleteFile.delete();
        }
        LogUtil.info(LogEnum.BIZ_DATASET, "sample is already");
        FFmpegFrameGrabber ff = FFmpegFrameGrabber.createDefault(file);
        ff.start();
        LogUtil.info(LogEnum.BIZ_DATASET, "get FFmpegFrameGrabber success");
        int length = ff.getLengthInFrames();
        List<String> picNames = new ArrayList<>();
        for (int i = MagicNumConstant.ZERO; i < length; i++) {
            String tempName = file.getName()
                    .substring(MagicNumConstant.ZERO, file.getName().indexOf(".")) + "_" + i + "_" + System.currentTimeMillis() + ".jpg";
            Frame frame = ff.grabFrame();
            if (frame != null && frame.image != null) {
                saveFrameToFile(frame, picPath + tempName, i, space, picNames);
            } else if (picNames.size() >= length / space) {
                break;
            } else if (frame != null && frame.image == null) {
                i--;
            } else {
                continue;
            }
        }
        ff.stop();
        LogUtil.info(LogEnum.BIZ_DATASET, "sample end,pictureNums:" + picNames.size());
        return picNames;
    }

    /**
     * 保存帧到图片
     *
     * @param frame
     * @param filePath 原图片路径
     */
    public static void saveFrameToFile(Frame frame, String filePath, int i, int space, List<String> picNames) throws IOException {
        if (i % space != MagicNumConstant.ZERO) {
            return;
        }
        Java2DFrameConverter javaFrameConverter = new Java2DFrameConverter();
        ImageIO.write(javaFrameConverter.getBufferedImage(frame), "jpg", new File(filePath));
        picNames.add(filePath);
    }

}
