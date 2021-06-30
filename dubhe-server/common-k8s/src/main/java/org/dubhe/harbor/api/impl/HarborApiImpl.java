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

package org.dubhe.harbor.api.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.harbor.api.HarborApi;
import org.dubhe.harbor.domain.vo.ImagePageVO;
import org.dubhe.harbor.domain.vo.ImageVO;
import org.dubhe.harbor.utils.HttpClientUtils;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dubhe.biz.base.constant.SymbolConstant.COLON;

/**
 * @description HarborApi接口实现类
 * @date 2020-05-21
 */
public class HarborApiImpl implements HarborApi {

    @Value("https://${harbor.address}/api/repositories?project_id=")
    private String imageSearchUrl;
    @Value("${harbor.address}/")
    private String imagePullUrl;
    @Value("https://${harbor.address}/api/projects")
    private String projectSearchUrl;
    @Value("https://${harbor.address}/api/repositories/")
    private String tagSearchUrl;
    @Value("${harbor.username}")
    private String harborName;
    @Value("${harbor.password}")
    private String harborPassword;
    private static final String TAG_SEARCH_CONF = "/%2F";
    private static final String TAG_SEARCH_PARAMS = "/tags";
    private static final String RESOURCE_NAME_KEY = "name";
    private static final String PROJECT_ID_KEY = "project_id";
    private static final String CREATION_TIME = "creation_time";
    private static final String UPDATE_TIME = "update_time";
    private static final String CPL_IMAGE_NAMES = "cplImageName";

    /**
     * 通过ProjectName名称查询镜像名称
     *
     * @param projectNameList 项目名称集合
     * @return List<String> 镜像名称集合
     */
    @Override
    public List<String> searchImageNames(List<String> projectNameList) {
        List<String> imageNames = new ArrayList<>(MagicNumConstant.TEN);
        List<Map> imageInfoList = searchImageByProjects(projectNameList);
        if (CollectionUtils.isEmpty(projectNameList) || CollectionUtils.isEmpty(imageInfoList)) {
            return imageNames;
        }
        imageInfoList.forEach(imageInfo -> imageNames.add((String) imageInfo.get(CPL_IMAGE_NAMES)));
        return imageNames;
    }

    /**
     * 通过projectM名称查询镜像名称
     *
     * @param projectNameList 镜像名称
     * @return List<Map> 镜像结果集合
     */
    @Override
    public List<Map> searchImageByProjects(List<String> projectNameList) {
        /**创建镜像名称List**/
        List<Map> imageInfoList = new ArrayList<>(MagicNumConstant.TEN);
        /**处理数据非空判断**/
        if (CollectionUtils.isEmpty(projectNameList)) {
            return imageInfoList;
        }
        /**获取projectName-projectId映射Map**/
        Map<String, Integer> project = getProjectIdMap();
        projectNameList.parallelStream().forEach(projectName -> {
            /**根据projectName获取projectId**/
            Integer projectId = project.get(projectName);
            if (projectId != null) {
                /**调用Harbor提供的http接口，传入projectId获取Json格式的repository 信息**/
                String repoJson = HttpClientUtils.sendHttps(imageSearchUrl + projectId);
                /**解析repoJson**/
                JSONArray repoArray = JSON.parseArray(repoJson);
                repoArray.stream().forEach(repo -> {
                    /**获取repoName**/
                    Map<String, String> repoDetailMap = (Map) repo;
                    String repoName = repoDetailMap.get(RESOURCE_NAME_KEY);
                    /**获取imageName**/
                    String imageName = repoName.substring(projectName.length() + MagicNumConstant.ONE);
                    /**调用getImageTagNames方法获取镜像的tag**/
                    List<String> imageTagNames = getImageTagNameList(projectName, imageName);
                    /**获取creation_time**/
                    String creationTime = repoDetailMap.get(CREATION_TIME);
                    /**获取update_time**/
                    String updateTime = repoDetailMap.get(UPDATE_TIME);
                    imageTagNames.stream().forEach(imageTagName -> {
                        Map imageMap = new HashMap<String, String>(MagicNumConstant.SIXTEEN);
                        imageMap.put("imageName", imageName);
                        imageMap.put("cplImageName", imagePullUrl + repoName + COLON + imageTagName);
                        imageMap.put("tag", imageTagName);
                        imageMap.put("creationTime", creationTime);
                        imageMap.put("updateTime", updateTime);
                        imageInfoList.add(imageMap);
                    });
                });
            } else {
                LogUtil.error(LogEnum.BIZ_K8S, "The number information corresponding to the project name was not found {}", projectNameList);
            }
        });
        return imageInfoList;
    }

    /**
     * 获取projectName-projectId映射Map
     *
     * @return Map<String, Integer> 项目名称和项目id映射map
     */
    private Map<String, Integer> getProjectIdMap() {
        Map<String, Integer> projectIdMap = new HashMap<>(MagicNumConstant.SIXTEEN);
        /**调用Harbor提供的http接口，获取Json格式的project信息**/
        String projectJson = HttpClientUtils.sendHttps(projectSearchUrl);
        /**解析projectArray**/
        JSONArray projectArray = JSON.parseArray(projectJson);
        List<JSONArray> jsonArrays = Arrays.asList(projectArray);
        if (CollectionUtils.isEmpty(jsonArrays)) {
            return projectIdMap;
        }
        JSONArray projects = jsonArrays.get(0);
        /**将对象转换为Map集合**/
        projects.stream().forEach(project -> {
            Map<String, Object> projectDetailMap = (Map) project;
            /**得到projectName**/
            String projectName = (String) projectDetailMap.get(RESOURCE_NAME_KEY);
            /**得到projectID**/
            Integer projectId = (Integer) projectDetailMap.get(PROJECT_ID_KEY);
            projectIdMap.put(projectName, projectId);
        });
        return projectIdMap;
    }


    /**
     * 查询镜像Tag名称方法
     *
     * @param projectName 项目名称
     * @param imageName   镜像名称
     * @return List<String> 镜像标签集合名称
     */
    private List<String> getImageTagNameList(String projectName, String imageName) {
        List<String> tagNames = new ArrayList<>(MagicNumConstant.TEN);
        /***非空校验*/
        if (StringUtils.isBlank(projectName)) {
            return tagNames;
        }
        if (StringUtils.isBlank(imageName)) {
            return tagNames;
        }
        /**调用Harbor提供的http接口，获取Json格式的Tag信息**/
        String imageTagsJson = HttpClientUtils.sendHttps(tagSearchUrl + projectName + TAG_SEARCH_CONF + imageName + TAG_SEARCH_PARAMS);
        if (StringUtils.isBlank(imageTagsJson)) {
            return tagNames;
        }

        /***解析imageTagsJson*/
        JSONArray tagsArray = JSON.parseArray(imageTagsJson);
        /**遍历数据并且转换为map类型**/
        tagsArray.stream().forEach(tag -> {
            /**获取tagName**/
            Map<String, String> tagDetailMap = (Map) tag;
            String tagName = tagDetailMap.get(RESOURCE_NAME_KEY);
            tagNames.add(tagName);
        });

        return tagNames;
    }


    /**
     * 根据完整镜像路径查询判断是否具有镜像
     *
     * @param imageUrl 镜像路径
     * @return Boolean true 存在 false 不存在
     */
    @Override
    public Boolean isExistImage(String imageUrl) {
        if (StringUtils.isBlank(imageUrl)) {
            LogUtil.info(LogEnum.BIZ_K8S, "The image path is empty", imageUrl);
            return false;
        }
        /**将imageName(含版本号)分割出来**/
        String[] urlSplit = imageUrl.split(SymbolConstant.SLASH);
        String imageName = urlSplit[MagicNumConstant.TWO];
        /**获取该项目下所有镜像名称**/
        List<Map> imageInfoList = searchImageByProjects(Arrays.asList(urlSplit[MagicNumConstant.ONE]));
        /**创建集合存储该项目下面的所有的镜像**/
        List<String> imageList = new ArrayList<>(MagicNumConstant.TEN);
        /**根据ProjectName和image将版本号查询出来**/
        try {
            if (!CollectionUtils.isEmpty(imageInfoList)) {
                imageInfoList.stream().forEach(name -> {
                    /**将该项目下面的所有镜像存储进来**/
                    imageList.add(((String) name.get(CPL_IMAGE_NAMES)).split(SymbolConstant.SLASH)[MagicNumConstant.TWO]);
                });
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "error path:{}", imageUrl);
        }

        return imageList.contains(imageName);
    }

    /**
     * 查询所有镜像名称不用根据ProjectName进行查询
     *
     * @param
     * @return List<Map> 镜像结果集
     **/
    @Override
    public List<Map> findImageList() {
        /**查询所有项目名称**/
        Map<String, Integer> projectIdMap = getProjectIdMap();
        /**创建集合将项目名添加进去**/
        List<String> projects = new ArrayList<>(projectIdMap.keySet());
        return searchImageByProjects(projects);
    }

    /**
     * 分页查询（镜像）
     *
     * @param page 分页对象
     * @return ImageVO 镜像vo对象
     **/
    @Override
    public ImageVO findImagePage(Page page) {
        /**查询所有项目名称**/
        Map<String, Integer> projectIdMap = getProjectIdMap();
        /**创建集合将项目名添加进去**/
        List<String> projects = new ArrayList<>(projectIdMap.keySet());
        List<Map> imageList = searchImageByProjects(projects);
        ImageVO imageVO = new ImageVO();
        if (CollectionUtils.isEmpty(imageList)) {
            List<Map> imagesData = imageList.stream().skip((page.getCurrent() - MagicNumConstant.ONE) * page.getSize()).limit(page.getSize()).collect(Collectors.toList());
            imageVO.setResult(imagesData);
            ImagePageVO imagePageVO = new ImagePageVO();
            /**获取当前页**/
            imagePageVO.setCurrent(page.getCurrent());
            /**获取每页显示都数据**/
            imagePageVO.setSize(page.getSize());
            /**获取总记录数**/
            imagePageVO.setTotal(imageList.size());
            imageVO.setPage(imagePageVO);
        }
        return imageVO;
    }

    /**
     * 根据镜像标签删除镜像
     *
     * @param imageUrl
     */
    @Override
    public void deleteImageByTag(String imageUrl) {
     if(StringUtils.isNotEmpty(imageUrl)){
         LogUtil.info(LogEnum.BIZ_K8S,"image path{}",imageUrl);
         String[] urlSplits = imageUrl.split(SymbolConstant.SLASH);
         String[] tagUrls = urlSplits[MagicNumConstant.TWO].split(SymbolConstant.COLON);
         String  dataRep=urlSplits[MagicNumConstant.ONE]+SymbolConstant.SLASH+tagUrls[MagicNumConstant.ZERO];
         LogUtil.info(LogEnum.BIZ_K8S,"data warehouse{}",dataRep);
         Map<String, Integer> projectIdMap = getProjectIdMap();
         //获取harbor中所有项目的名称
         Set<String> names = projectIdMap.keySet();
         //判断harbor中是否具有改项目
         names.forEach(name->{
             if(urlSplits[MagicNumConstant.ONE].equals(name)){
                 //发送删除请求
                HttpClientUtils.sendHttpsDelete(tagSearchUrl+dataRep+TAG_SEARCH_PARAMS+SymbolConstant.SLASH+tagUrls[MagicNumConstant.ONE],harborName,harborPassword);
                LogUtil.error(LogEnum.BIZ_K8S,"fail to delete{}",imageUrl);
                return;
             }
         });
     }
    }

}
