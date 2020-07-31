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

package org.dubhe.data.service.http;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.service.DatasetVersionFileService;
import org.dubhe.data.util.FileUtil;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @description 调用目标跟踪算法服务
 * @date 2020-05-27
 */
@Service
public class TrackHttpService {

    @Value("${data.track.endpoint}track_label")
    private String trackUrl;

    @Autowired
    private DatasetVersionFileService datasetVersionFileService;

    @Autowired
    private FileUtil fileUtil;

    @Value("${minio.bucketName}")
    private String bucket;


    /**
     * @param fileMap 文件信息
     * @return: boolean 是否调用成功
     */
    public boolean track(Map<Long, List<DatasetVersionFile>> fileMap) {
        try {
            fileMap.forEach((k, v) -> {
                JSONObject param = new JSONObject();
                param.put("id", k);
                ArrayList<HashMap<String, String>> list = new ArrayList<>();
                List<File> fileList = datasetVersionFileService.getFileListByVersionFileList(v);
                fileList.forEach(f -> {
                    list.add(new HashMap<String, String>(MagicNumConstant.ONE) {
                        {
                            put("filePath", f.getUrl());
                            put("annotationPath", bucket + java.io.File.separator + fileUtil.getAnnotationAbsPath(f.getDatasetId(), f.getName()));
                        }
                    });
                });
                param.put("images", list);
                LogUtil.info(LogEnum.BIZ_DATASET, "request track:{}, param:{}", trackUrl, param.toJSONString());
                String resp = HttpUtil.post(trackUrl, param.toJSONString());
                LogUtil.info(LogEnum.BIZ_DATASET, "resp:{}", resp);
            });
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "request track service error. error:", e);
            return false;
        }
        return true;
    }
}
