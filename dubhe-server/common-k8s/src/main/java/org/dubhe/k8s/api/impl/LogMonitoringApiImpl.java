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

package org.dubhe.k8s.api.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.utils.TimeTransferUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.vo.LogMonitoringVO;
import org.dubhe.k8s.utils.K8sUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import static org.dubhe.biz.base.constant.MagicNumConstant.ZERO;
import static org.dubhe.biz.base.constant.MagicNumConstant.*;
import static org.dubhe.biz.base.constant.SymbolConstant.*;


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
    private static final String INDEX_NAME = "kubelogs";
    private static final String TADL_INDEX_NAME = "tadllogs";
    private static final String INDEX_SHARDS_NUMBER = "index.number_of_shards";
    private static final String INDEX_REPLICAS_NUMBER = "index.number_of_replicas";
    private static final String TYPE = "type";
    private static final String TEXT = "text";
    private static final String DATE = "date";
    private static final String PROPERTIES = "properties";
    private static final String EXPERIMENT_ID = "experimentId";
    private static final String POD_NAME_KEY = "kubernetes.pod_name.keyword";
    private static final String POD_NAME = "kubernetes.pod_name";
    private static final String NAMESPACE_KEY = "kubernetes.namespace_name.keyword";
    private static final String NAMESPACE = "kubernetes.namespace_name";
    private static final String TIMESTAMP = "@timestamp";
    private static final String MESSAGE = "log";
    private static final String LOG_PREFIX = "[Dubhe Service Log] ";
    private static final String INDEX_FORMAT = "yyyy.MM.dd";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.sss";
    private static final String LOG_FORMAT = "[Dubhe Service Log]-[%s]-%s";
    private static final String KUBERNETES_KEY = "kubernetes";
    private static final String KUBERNETES_POD_NAME_KEY = "pod_name";
    private static final String BUSINESS_KEY = "kubernetes.labels.platform/business.keyword";
    private static final String BUSINESS_GROUP_ID_KEY = "kubernetes.labels.platform/business-group-id.keyword";

    public LogMonitoringApiImpl(K8sUtils k8sUtils) {
        this.kubernetesClient = k8sUtils.getClient();
    }


    /**
     * 添加Pod日志到ES,无日志参数，默认从k8s集群查询日志添加到ES
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
        List<String> logList = searchLogInfoByEs(ZERO, ONE, new LogMonitoringBO(namespace,podName));
        if (CollectionUtils.isNotEmpty(logList)) {
            return true;
        }

        String logInfoString = getLogInfoString(podName, namespace);
        if (StringUtils.isBlank(logInfoString)) {
            LogUtil.info(LogEnum.BIZ_K8S, "LogMonitoringApiImpl.getLogInfoString could not get any log,no doc created in Elasticsearch");
            return false;
        }
        logList = Arrays.asList(logInfoString.split(LINEBREAK)).stream().limit(TEN_THOUSAND).collect(Collectors.toList());
        return addLogsToEs(podName, namespace, logList);
    }

    /**
     * 添加Pod自定义日志到ES
     *
     * @param podName Pod名称
     * @param namespace 命名空间
     * @param logList 日志信息
     * @return boolean 日志添加是否成功
     */
    @Override
    public boolean addLogsToEs(String podName, String namespace, List<String> logList) {
        Date date = new Date();
        SimpleDateFormat indexFormat = new SimpleDateFormat(INDEX_FORMAT);
        String timestamp = TimeTransferUtil.dateTransferToUtc(date);
        BulkRequest bulkRequest = new BulkRequest();
        try {
            for (int i = 0; i < logList.size(); i++) {
                /**准备日志json数据**/
                String logString = logList.get(i);
                LinkedHashMap<String, Object> jsonMap = new LinkedHashMap() {{
                    put(POD_NAME, podName);
                    put(NAMESPACE, namespace);
                    put(MESSAGE, logString);
                    put(TIMESTAMP, timestamp);
                }};

                /**添加索引创建对象到bulkRequest**/
                bulkRequest.add(new IndexRequest(INDEX_NAME).source(jsonMap));
            }

            /**通过restHighLevelClient发送http的请求批量创建文档**/
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApi.addLogsToEs error:{}", e);
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
    public LogMonitoringVO searchLogByResName(int from, int size, LogMonitoringBO logMonitoringBo) {
        List<String> logList = new ArrayList<>();
        LogMonitoringVO logMonitoringResult = new LogMonitoringVO(ZERO, logList);
        String namespace = logMonitoringBo.getNamespace();
        String resourceName = logMonitoringBo.getResourceName();
        if (StringUtils.isBlank(resourceName) || StringUtils.isBlank(namespace)) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApiImpl.searchLogByResName error: param [resourceName] and [namespace] are required");
            return logMonitoringResult;
        }

        Set<String> podNameSet = resourceCache.getPodNameByResourceName(logMonitoringBo.getNamespace(), logMonitoringBo.getResourceName());

        if (CollectionUtils.isEmpty(podNameSet)) {
            return logMonitoringResult;
        }
        /**遍历podNameSet,根据podName查询日志信息**/
        for (String podName : podNameSet) {
            logMonitoringBo.setPodName(podName);

            /**查询ES存储的日志信息**/
            List<String> logs = searchLogInfoByEs(from, size, logMonitoringBo);

            if (!CollectionUtils.isEmpty(logs)) {
                logList.addAll(MagicNumConstant.ZERO, logs);
            }
        }

        logMonitoringResult.setLogs(logList);
        logMonitoringResult.setTotalLogs(logList.size());
        return logMonitoringResult;
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
    public LogMonitoringVO searchLogByPodName(int from, int size, LogMonitoringBO logMonitoringBo) {
        LogMonitoringVO logMonitoringResult = new LogMonitoringVO();
        List<String> logs = searchLogInfoByEs(from, size, logMonitoringBo);
        logMonitoringResult.setLogs(logs);
        logMonitoringResult.setTotalLogs(logs.size());
        return logMonitoringResult;

    }

    /**
     * 日志查询方法
     *
     * @param logMonitoringBo 日志查询bo
     * @return LogMonitoringVO 日志查询结果类
     */
    @Override
    public LogMonitoringVO searchLog(LogMonitoringBO logMonitoringBo) {
        LogMonitoringVO logMonitoringResult = new LogMonitoringVO();
        List<String> logs = searchLogInfoByEs(logMonitoringBo);
        logMonitoringResult.setLogs(logs);
        logMonitoringResult.setTotalLogs(logs.size());
        return logMonitoringResult;

    }

    /**
     * Pod 日志总量查询方法
     *
     * @param logMonitoringBo 日志查询bo
     * @return long Pod 产生的日志总量
     */
    @Override
    public long searchLogCountByPodName(LogMonitoringBO logMonitoringBo) {
        SearchRequest searchRequest = buildSearchRequest(ZERO, ZERO, logMonitoringBo);
        try {
            /**执行搜索，获得响应结果**/
            return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).getHits().getTotalHits().value;
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApiImpl.searchLogCountByPodName error,param:[logMonitoringBo]={}, error:{}", JSON.toJSONString(logMonitoringBo), e);
            return ZERO_LONG;
        }
    }

    /**
     * 添加 TADL 服务日志到 Elasticsearch
     *
     * @param experimentId 日志查询起始值，初始值为1，表示从第一条日志记录开始查询
     * @param log 日志
     * @return boolean 日志添加是否成功
     */
    @Override
    public boolean addTadlLogsToEs(long experimentId, String log) {


        Date date = new Date();
        String timestamp = TimeTransferUtil.dateTransferToUtc(date);
        BulkRequest bulkRequest = new BulkRequest();
        try {
            /**查询索引是否存在, 不存在则创建**/
            GetIndexRequest getIndexRequest = new GetIndexRequest(TADL_INDEX_NAME);
            boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (!exists){
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(TADL_INDEX_NAME);
                createIndexRequest.settings(Settings.builder()
                        .put(INDEX_SHARDS_NUMBER, 3)
                        .put(INDEX_REPLICAS_NUMBER, 2)
                );
                Map<String, String> timestampMapping = new HashMap<>();
                timestampMapping.put(TYPE, DATE);
                Map<String, String> logMapping = new HashMap<>();
                logMapping.put(TYPE, TEXT);
                Map<String, String> experimentIdMapping = new HashMap<>();
                experimentIdMapping.put(TYPE, TEXT);
                Map<String, Object> properties = new HashMap<>();
                properties.put(TIMESTAMP,timestampMapping);
                properties.put(EXPERIMENT_ID,experimentIdMapping);
                properties.put(MESSAGE,logMapping);
                Map<String, Object> mapping = new HashMap<>();
                mapping.put(PROPERTIES, properties);
                createIndexRequest.mapping(mapping);
                CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);

            }
            LinkedHashMap<String, Object> jsonMap = new LinkedHashMap() {{
                put(EXPERIMENT_ID, experimentId);
                put(MESSAGE, new SimpleDateFormat(TIMESTAMP_FORMAT).format(date) + SPACE + log);
                put(TIMESTAMP, timestamp);
            }};

            /**添加索引创建对象到bulkRequest**/
            bulkRequest.add(new IndexRequest(TADL_INDEX_NAME).source(jsonMap));

            /**通过restHighLevelClient发送http的请求创建文档**/
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApi.addTadlLogsToEs error:{}", e);
            return false;
        }
        return true;
    }



    /**
     * TADL 服务日志查询方法
     *
     * @param from 日志查询起始值，初始值为1，表示从第一条日志记录开始查询
     * @param size 日志查询记录数
     * @param experimentId TADL Experiment ID
     * @return LogMonitoringVO 日志查询结果类
     */
    @Override
    public LogMonitoringVO searchTadlLogById(int from, int size, long experimentId) {
        List<String> logList = new ArrayList<>();
        LogMonitoringVO logMonitoringResult = new LogMonitoringVO(ZERO, logList);
        /**处理查询范围参数起始值**/
        from = from <= MagicNumConstant.ZERO ? MagicNumConstant.ZERO : --from;
        size = size <= MagicNumConstant.ZERO || size > TEN_THOUSAND ? TEN_THOUSAND : size;
        /**创建搜索请求对象**/
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(TADL_INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.trackTotalHits(true).from(from).size(size);
        /**根据时间戳排序**/
        searchSourceBuilder.sort(TIMESTAMP, SortOrder.ASC);
        /**创建布尔查询对象**/
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.filter(QueryBuilders.matchQuery(EXPERIMENT_ID, experimentId));

        /**设置boolQueryBuilder到searchSourceBuilder**/
        searchSourceBuilder.query(boolQueryBuilder);

        searchRequest = searchRequest.source(searchSourceBuilder);
        /**执行搜索**/
        SearchResponse searchResponse;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApiImpl.searchTadlLogById error,param:[experimentId]={}, error:{}", experimentId, e);
            return logMonitoringResult;
        }
        /**获取响应结果**/
        SearchHits hits = searchResponse.getHits();

        SearchHit[] searchHits = hits.getHits();
        if (searchHits.length == MagicNumConstant.ZERO) {
            return logMonitoringResult;
        }

        for (SearchHit hit : searchHits) {
            /**源文档**/
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            /**取出message**/
            String message = (String) sourceAsMap.get(MESSAGE);
            message = message.replace(LINEBREAK, BLANK);
            /**添加日志信息到集合**/
            logList.add(message);
        }
        logMonitoringResult.setLogs(logList);
        logMonitoringResult.setTotalLogs(logList.size());
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
                LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApi.getLogInfoString error, param:[podName]={}, [namespace]={}, error:{}", podName, namespace, e);
            }
        }
        return null;
    }

    /**
     * 从Elasticsearch查询日志
     *
     * @param logMonitoringBo 日志查询bo
     * @return List<String> 日志集合
     */
    private List<String> searchLogInfoByEs(LogMonitoringBO logMonitoringBo) {

        List<String> logList = new ArrayList<>();

        SearchRequest searchRequest = buildSearchRequest(logMonitoringBo);
        /**执行搜索**/
        SearchResponse searchResponse;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApiImpl.searchLogInfoByEs error,param:[logMonitoringBo]={}, error:{}", JSON.toJSONString(logMonitoringBo), e);
            return logList;
        }
        /**获取响应结果**/
        SearchHits hits = searchResponse.getHits();

        SearchHit[] searchHits = hits.getHits();
        if (searchHits.length == MagicNumConstant.ZERO) {
            return logList;
        }

        for (SearchHit hit : searchHits) {

            String esResult = hit.getSourceAsString();
            JSONObject jsonObject = JSON.parseObject(esResult);
            String message = jsonObject.getString(MESSAGE);
            message = message.replace(LINEBREAK, BLANK);

            String podName = jsonObject.getJSONObject(KUBERNETES_KEY).
                    getString(KUBERNETES_POD_NAME_KEY);

            /**拼接日志信息**/
            String logString = String.format(LOG_FORMAT, podName, message);
            /**添加日志信息到集合**/
            logList.add(logString);
        }
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
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LogMonitoringApiImpl.searchLogInfoByEs error,param:[logMonitoringBo]={}, error:{}", JSON.toJSONString(logMonitoringBo), e);
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

        /**处理查询范围参数起始值**/
        from = from <= MagicNumConstant.ZERO ? MagicNumConstant.ZERO : --from;
        size = size <= MagicNumConstant.ZERO || size > TEN_THOUSAND ? TEN_THOUSAND : size;

        /**创建搜索请求对象**/
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.trackTotalHits(true).from(from).size(size);

        /**根据时间戳排序**/
        searchSourceBuilder.sort(TIMESTAMP, SortOrder.ASC);
        /**过虑源字段**/
        String[] sourceFieldArray = sourceField.split(COMMA);

        searchSourceBuilder.fetchSource(sourceFieldArray, new String[]{});

        /**创建布尔查询对象**/
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        /**添加podName查询条件**/
        String podName = logMonitoringBo.getPodName();
        if (StringUtils.isNotEmpty(podName)) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery(POD_NAME_KEY, podName));
        }
        /**添加namespace查询条件**/
        String namespace = logMonitoringBo.getNamespace();
        if (StringUtils.isNotEmpty(namespace)) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery(NAMESPACE_KEY, namespace));
        }
        /**添加关键字查询条件**/
        String logKeyword = logMonitoringBo.getLogKeyword();
        if (StringUtils.isNotEmpty(logKeyword)) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery(MESSAGE, logKeyword).operator(Operator.AND));
        }
        /**添加时间范围查询条件**/
        Long beginTimeMillis = logMonitoringBo.getBeginTimeMillis();
        Long endTimeMillis = logMonitoringBo.getEndTimeMillis();
        if (beginTimeMillis != null || endTimeMillis != null){
            beginTimeMillis = beginTimeMillis == null ? ZERO_LONG : beginTimeMillis;
            endTimeMillis = endTimeMillis == null ? System.currentTimeMillis() : endTimeMillis;

            /**将毫秒值转换为UTC时间**/
            String beginUtcTime = TimeTransferUtil.dateTransferToUtc(new Date(beginTimeMillis));
            String endUtcTime = TimeTransferUtil.dateTransferToUtc(new Date(endTimeMillis));
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(TIMESTAMP).gte(beginUtcTime).lte(endUtcTime));
        }


        /**设置boolQueryBuilder到searchSourceBuilder**/
        searchSourceBuilder.query(boolQueryBuilder);

        return searchRequest.source(searchSourceBuilder);
    }

    /**
     * 构建搜索请求对象
     *
     * @param logMonitoringBo 日志查询bo
     * @return SearchRequest ES搜索请求对象
     */
    private SearchRequest buildSearchRequest(LogMonitoringBO logMonitoringBo) {

        /**创建搜索请求对象**/
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.trackTotalHits(true).from(logMonitoringBo.getFrom()).size(logMonitoringBo.getSize());

        /**根据时间戳排序**/
        searchSourceBuilder.sort(TIMESTAMP, SortOrder.ASC);
        /**过虑源字段**/
        String[] sourceFieldArray = sourceField.split(COMMA);

        searchSourceBuilder.fetchSource(sourceFieldArray, new String[]{});

        /**创建布尔查询对象**/
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        /**添加podName查询条件**/
        Set<String> podNames = logMonitoringBo.getPodNames();
        if (CollectionUtils.isNotEmpty(podNames)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(POD_NAME_KEY, podNames.toArray(new String[podNames.size()])));
        }
        /**添加namespace查询条件**/
        String namespace = logMonitoringBo.getNamespace();
        if (StringUtils.isNotEmpty(namespace)) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery(NAMESPACE_KEY, namespace));
        }
        /**添加业务查询条件**/
        BizEnum business = logMonitoringBo.getBusiness();
        if (null != business) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(BUSINESS_KEY, business.getBizCode()));
        }
        /**添加实验Id查询条件**/
        String businessGroupId = logMonitoringBo.getBusinessGroupId();
        if (StringUtils.isNotEmpty(businessGroupId)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(BUSINESS_GROUP_ID_KEY, businessGroupId));
        }
        /**添加关键字查询条件**/
        String logKeyword = logMonitoringBo.getLogKeyword();
        if (StringUtils.isNotEmpty(logKeyword)) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery(MESSAGE, logKeyword).operator(Operator.AND));
        }
        /**添加时间范围查询条件**/
        Long beginTimeMillis = logMonitoringBo.getBeginTimeMillis();
        Long endTimeMillis = logMonitoringBo.getEndTimeMillis();
        if (beginTimeMillis != null || endTimeMillis != null){
            beginTimeMillis = beginTimeMillis == null ? ZERO_LONG : beginTimeMillis;
            endTimeMillis = endTimeMillis == null ? System.currentTimeMillis() : endTimeMillis;

            /**将毫秒值转换为UTC时间**/
            String beginUtcTime = TimeTransferUtil.dateTransferToUtc(new Date(beginTimeMillis));
            String endUtcTime = TimeTransferUtil.dateTransferToUtc(new Date(endTimeMillis));
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(TIMESTAMP).gte(beginUtcTime).lte(endUtcTime));
        }


        /**设置boolQueryBuilder到searchSourceBuilder**/
        searchSourceBuilder.query(boolQueryBuilder);

        return searchRequest.source(searchSourceBuilder);
    }


}
