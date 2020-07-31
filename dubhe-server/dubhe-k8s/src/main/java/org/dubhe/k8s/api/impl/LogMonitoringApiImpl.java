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

package org.dubhe.k8s.api.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.vo.LogMonitoringVO;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dubhe.base.MagicNumConstant.FIFTY;
import static org.dubhe.base.MagicNumConstant.TEN_THOUSAND;
import static org.dubhe.base.MagicNumConstant.ZERO;
import static org.dubhe.constant.SymbolConstant.BLANK;
import static org.dubhe.constant.SymbolConstant.COMMA;
import static org.dubhe.constant.SymbolConstant.LINEBREAK;


/**
 * @description k8s集群日志查询接口
 * @date 2020-06-29
 */
public class LogMonitoringApiImpl implements LogMonitoringApi {
    @Value("${k8s.elasticsearch.log.source_field}")
    private String sourceField;

    @Value("${k8s.elasticsearch.log.type}")
    private String type;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ResourceCache resourceCache;

    private KubernetesClient kubernetesClient;
    private static final String INDEX_NAME = "logstash-*";
    private static final String POD_NAME_KEY = "kubernetes.pod_name.keyword";
    private static final String POD_NAME = "kubernetes.pod_name";
    private static final String NAMESPACE_KEY = "kubernetes.namespace_name.keyword";
    private static final String NAMESPACE = "kubernetes.namespace_name";
    private static final String TIMESTAMP = "@timestamp";
    private static final String MESSAGE = "message";
    private static final String LOG_PREFIX = "[Dubhe Service Log] ";
    private static final String INDEX_FORMAT = "yyyy.MM.dd";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.sssssssss+'08:00'";

    public LogMonitoringApiImpl(K8sUtils k8sUtils) {
        this.kubernetesClient = k8sUtils.getClient();
    }


    /**
     * 添加日志到ES
     *
     * @param podName Pod名称
     * @param namespace 命名空间
     * @return boolean 日志添加是否成功
     */
    @Override
    public boolean addLogsToEs(String podName, String namespace) {

        if (StringUtils.isBlank(podName) || StringUtils.isBlank(namespace)) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApiImpl.addLogsToEs error: param [podName] and [namespace] are required");
            return false;
        }

        List<String> logList = searchLogInfoByEs(ZERO, FIFTY, new LogMonitoringBO().setPodName(podName).setNamespace(namespace));
        if (CollectionUtils.isNotEmpty(logList)){
            return true;
        }

        String logInfoString = getLogInfoString(podName, namespace);
        if (StringUtils.isBlank(logInfoString)) {
            LogUtil.info(LogEnum.BIZ_K8S, "LogMonitoringApiImpl.getLogInfoString could not get any log,no doc created in Elasticsearch");
            return false;
        }

        Date date = new Date();
        SimpleDateFormat indexFormat = new SimpleDateFormat(INDEX_FORMAT);
        SimpleDateFormat timeStampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
        logList = Arrays.asList(logInfoString.split(LINEBREAK)).stream().limit(TEN_THOUSAND).collect(Collectors.toList());
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < logList.size(); i++) {
            /**准备日志json数据**/
            String logString = logList.get(i);
            LinkedHashMap<String, Object> jsonMap = new LinkedHashMap() {{
                put(POD_NAME, podName);
                put(NAMESPACE, namespace);
                put(MESSAGE, logString);
                put(TIMESTAMP, timeStampFormat.format(date));
            }};

            /**添加索引创建对象到bulkRequest**/
            bulkRequest.add(new IndexRequest("logstash-" + indexFormat.format(date)).source(jsonMap));
        }
        try {
            /**通过restHighLevelClient发送http的请求批量创建文档**/
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApi.addLogsToEs error, param:[podName]={}, [namespace]={}, error:",podName, namespace, e);
            return false;
        }
        return true;
    }

    /**
     * 日志查询方法
     *
     * @param from 日志查询起始值，初始值为1，表示从第一条日志记录开始查询
     * @param size 日志查询记录数
     * @param logMonitoringBo 日志查询bo
     * @return LogMonitoringVO 日志查询结果类
     */
    @Override
    public LogMonitoringVO searchLog(int from, int size, LogMonitoringBO logMonitoringBo) {
        LogMonitoringVO logMonitoringResult = new LogMonitoringVO();
        List<String> logList = new ArrayList<>();
        /**处理查询范围参数起始值**/
        from = from <= MagicNumConstant.ZERO ? MagicNumConstant.ZERO : --from;
        size = size <= MagicNumConstant.ZERO || size > TEN_THOUSAND ? TEN_THOUSAND : size;
        Set<String> podNameSet = resourceCache.getPodNameByResourceName(logMonitoringBo.getNamespace(), logMonitoringBo.getResourceName());

        if (CollectionUtils.isEmpty(podNameSet)) {
            logMonitoringResult.setLogs(logList);
            logMonitoringResult.setTotalLogs(Long.valueOf(MagicNumConstant.ZERO));
            return logMonitoringResult;
        }
        /**遍历podNameSet,根据podName查询日志信息**/
        for (String podName : podNameSet) {
            logMonitoringBo.setPodName(podName);
            /**通过k8s Api查询日志**/
            List<String> logs = searchLogInfo(from, size, logMonitoringBo);
            /**如果k8s Api查不到,则pod可能被删除，通过es查询**/
            if (CollectionUtils.isEmpty(logs)) {
                logs = searchLogInfoByEs(from, size, logMonitoringBo);
            }
            if (!CollectionUtils.isEmpty(logs)) {
                logList.addAll(MagicNumConstant.ZERO, logs);
            }
        }

        logMonitoringResult.setLogs(logList);
        logMonitoringResult.setTotalLogs(Long.valueOf(logList.size()));
        return logMonitoringResult;
    }

    /**
     * 得到日志信息String
     *
     * @param podName Pod名称
     * @param namespace 命名空间
     * @return String 所有日志
     */
    private String getLogInfoString(String podName, String namespace) {
        PodResource<Pod, DoneablePod> podResource = kubernetesClient.pods().inNamespace(namespace).withName(podName);
        Pod pod = podResource.get();
        if (pod != null) {
            /**通过k8s客户端获取具体pod的日志监听类**/
            try {
                return podResource.getLog();
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApi.getLogInfoString error, param:[podName]={}, [namespace]={}, error:",podName, namespace, e);
            }
        }
        return null;
    }

    /**
     * 通过fabric8客户端查询日志
     *
     * @param from 日志查询起始值
     * @param size 日志查询记录数
     * @param logMonitoringBo 日志查询bo
     * @return List<String> 日志集合
     */
    private List<String> searchLogInfo(int from, int size, LogMonitoringBO logMonitoringBo) {
        List<String> logList = new ArrayList<>();

        /**通过k8s客户端获取具体pod的日志监听类**/
        String logInfoString = getLogInfoString(logMonitoringBo.getPodName(), logMonitoringBo.getNamespace());
        if (StringUtils.isBlank(logInfoString)) {
            return logList;
        }
        logList = Arrays.asList(logInfoString.split(LINEBREAK))
                .stream().map(logString -> LOG_PREFIX + logString).skip(from).limit(size).collect(Collectors.toList());
        return logList;
    }

    /**
     * 从Elasticsearch查询日志
     *
     * @param from 日志查询起始值
     * @param size 日志查询记录数
     * @param logMonitoringBo 日志查询bo
     * @return List<String> 日志集合
     */
    private List<String> searchLogInfoByEs(int from, int size, LogMonitoringBO logMonitoringBo) {
        List<String> logList = new ArrayList<>();

        SearchRequest searchRequest = buildSearchRequest(from, size, logMonitoringBo);
        /**执行搜索**/
        SearchResponse searchResponse;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApiImpl.searchLogInfoByEs error,param:{} error:", logMonitoringBo, e);
            return logList;
        }
        /**获取响应结果**/
        SearchHits hits = searchResponse.getHits();

        SearchHit[] searchHits = hits.getHits();
        if (searchHits.length == MagicNumConstant.ZERO) {
            return logList;
        }

        for (SearchHit hit : searchHits) {
            /**源文档**/
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            /**取出message**/
            String message = (String) sourceAsMap.get(MESSAGE);
            message = message.replace(LINEBREAK, BLANK);

            /**拼接日志信息**/
            String logString = LOG_PREFIX + message;
            /**添加日志信息到集合**/
            logList.add(logString);
        }
        return logList;
    }

    /**
     * 构建搜索请求对象
     *
     * @param from 日志查询起始值
     * @param size 日志查询记录数
     * @param logMonitoringBo 日志查询bo
     * @return SearchRequest ES搜索请求对象
     */
    private SearchRequest buildSearchRequest(int from, int size, LogMonitoringBO logMonitoringBo) {
        /**创建搜索请求对象**/
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        /**根据时间戳排序**/
        searchSourceBuilder.sort(TIMESTAMP, SortOrder.ASC);
        /**过虑源字段**/
        String[] sourceFieldArray = sourceField.split(COMMA);

        searchSourceBuilder.fetchSource(sourceFieldArray, new String[]{});

        /**创建布尔查询对象**/
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        /**搜索条件**/
        if (StringUtils.isNotEmpty(logMonitoringBo.getPodName())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(POD_NAME_KEY, logMonitoringBo.getPodName()));
        }
        if (StringUtils.isNotEmpty(logMonitoringBo.getNamespace())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(NAMESPACE_KEY, logMonitoringBo.getNamespace()));
        }

        /**设置boolQueryBuilder到searchSourceBuilder**/
        searchSourceBuilder.query(boolQueryBuilder);

        return searchRequest.source(searchSourceBuilder);
    }

}
