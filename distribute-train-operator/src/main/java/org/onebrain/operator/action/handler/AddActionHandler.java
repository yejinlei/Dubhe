/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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

package org.onebrain.operator.action.handler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.onebrain.operator.action.PodInfo;
import org.onebrain.operator.action.deployer.ChildResourceCreateInfo;
import org.onebrain.operator.action.deployer.JobDeployer;
import org.onebrain.operator.action.deployer.ServiceDeployer;
import org.onebrain.operator.action.deployer.StatefulSetDeployer;
import org.onebrain.operator.action.deployer.impl.BaseJobDeployer;
import org.onebrain.operator.action.deployer.impl.BaseServiceDeployer;
import org.onebrain.operator.action.deployer.impl.BaseStatefulSetDeployer;
import org.onebrain.operator.api.pod.PodApi;
import org.onebrain.operator.constants.KubeConstants;
import org.onebrain.operator.crd.DistributeTrain;
import org.onebrain.operator.crd.DistributeTrainSpec;
import org.onebrain.operator.crd.DistributeTrainStatus;
import org.onebrain.operator.exception.OperatorException;
import org.onebrain.operator.redis.RedisService;
import org.onebrain.operator.redis.key.OperatorKey;
import org.onebrain.operator.utils.DistributeTrainClientHolder;
import org.onebrain.operator.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.onebrain.operator.constants.KubeConstants.CHARSET;
import static org.onebrain.operator.constants.KubeConstants.JOB_LABEL;
import static org.onebrain.operator.constants.KubeConstants.MASTER_CONTAINER_NAME;
import static org.onebrain.operator.constants.KubeConstants.SLAVE_CONTAINER_NAME;
import static org.onebrain.operator.constants.KubeConstants.STATEFULSET_LABEL;
import static org.onebrain.operator.constants.NumberConstant.NUMBER_2;

/**
 * @description 分布式训练添加事件的处理器
 * @date 2020-09-23
 */
@Component("addActionHandler")
@Slf4j
public class AddActionHandler implements DistributeTrainActionHandler {

    public static final String JOB_WATCHER = "job-watcher-";
    public static final String PRETREATMENT = "pretreatment";
    public static final String JOB_NAME = "job-name";
    public static final String RUNNING = "Running";
    public static final String MASTER = "master";
    public static final String SLAVE = "slave";
    public static final String PRETREATMENT_TARGET_DIR = "/home/pretreatment";
    public static final String IP = "ip";
    public static final String ROLE = "role";
    public static final String HOSTFILE_TARGET_DIR = "/home/hostfile.json";
    @Autowired
    private KubernetesClient client;

    @Autowired
    private PodApi podApi;

    /**
     * String 训练uid  List pod信息
     */
    private Map<String, List<PodInfo>> dtMap = new ConcurrentHashMap();

    @Autowired
    private RedisService redis;

    /**
     * 线程池
     */
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1), new ThreadFactory() {
        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, JOB_WATCHER + mThreadNum.getAndIncrement());
        }
    }, new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * 处理事件的任务
     */
    class HandlerActionTask implements Runnable {

        private DistributeTrain distributeTrain;

        public HandlerActionTask(DistributeTrain distributeTrain) {
            this.distributeTrain = distributeTrain;
        }

        @Override
        public void run() {
            doAction(distributeTrain);
        }
    }

    /**
     * 执行任务动作
     * @param distributeTrain
     */
    public void doAction(DistributeTrain distributeTrain) {
        log.info("doAction=>distributeTrain : 【{}】", distributeTrain);
        ChildResourceCreateInfo info = null;
        try {
            //redis重复检查
            //根据k8s 创建DistributionTrain 的uid去重
            if (null != redis.get(OperatorKey.CR, distributeTrain.getMetadata().getUid())) {
                log.info("distribute train 【{}】 in namespace 【{}】 already exists", distributeTrain.getMetadata().getName(), distributeTrain.getMetadata().getNamespace());
                return;
            } else {
                //录入redis做消费记录
                redis.set(OperatorKey.CR, distributeTrain.getMetadata().getUid(), System.currentTimeMillis());
            }

            //参数检查，提取并生成所需参数
            validateParams(distributeTrain);
            info = ChildResourceCreateInfo.fromCr(distributeTrain);
            //按照size，创建副本数为size-1的statefulSet
            createStatefulSet(info);
            //等待statefulset全部ready
            waitUntilStatefulSetReady(info);
            //创建job，job此时在死循环
            createJob(info);
            //等待job ready
            waitUntilJobReady(info);
            //复制 /home/pretreatment 到 pod
            copyPretreatmentShell(info);
            //收集statefulSet和job的ip
            validateAndCollectPods(info);
            //本地生成公私钥、认证文件，并拷贝到所有节点的~/.ssh目录下
            sshAuthWithoutPass(info);
            //本地生成hostfile，并拷贝到所有节点的指定目录下
            generateAndUploadHostFile(info);
            //解锁job的死循环
            releaseInterLock(info);
            //改状态
            //updateStatus(info, distributeTrain);
            //为job注册监听器
            registerJobListener(info);

            log.info("all parts of【{}】 are ready", info.getParentName());
        } catch (Exception e) {
            log.error("doAction error:【{}】", e);
            //移除缓存
            redis.del(OperatorKey.CR, distributeTrain.getMetadata().getUid());
            //回收创建的资源
            if (info != null) {
                recycleCr(info);
            }
        }
    }

    /**
     * 处理分布式训练
     * @param distributeTrain 分布式训练信息
     */
    @Override
    public void handlerAction(DistributeTrain distributeTrain) {
        log.info("handlerAction=>distributeTrain : 【{}】", distributeTrain);
        HandlerActionTask handlerActionTask = new HandlerActionTask(distributeTrain);
        pool.getActiveCount();
        pool.execute(handlerActionTask);
    }

    /**
     * 校验参数合法性
     * @param distributeTrain 分布式训练
     */
    private void validateParams(DistributeTrain distributeTrain) {
        log.info("validateParams=>distributeTrain : 【{}】", distributeTrain);
        Integer size = distributeTrain.getSpec().getSize();
        if (size < NUMBER_2) {
            throw new OperatorException("size must be greater than 1");
        }
        String masterCmd = distributeTrain.getSpec().getMasterCmd();
        String slaveCmd = distributeTrain.getSpec().getSlaveCmd();
        if (StrUtil.isEmpty(slaveCmd) || StrUtil.isEmpty(masterCmd)) {
            throw new OperatorException("cmd lines must not be empty");
        }
    }

    /**
     * 拷贝文件pretreatment到pod
     * @param info 资源信息
     */
    private void copyPretreatmentShell(ChildResourceCreateInfo info) {
        log.info("start to copy pretreatment for 【{}】 ", info.getParentName());
        try {
            String path = System.getProperty(KubeConstants.USER_DIR_SYSTEM_PROPERTY) + File.separator + PRETREATMENT;
            if (!FileUtil.exist(path)) {
                FileUtil.writeFromStream(new ClassPathResource("/shell/pretreatment").getInputStream(), path);
            }
            File pretreatment = new File(path);
            //上传到pod指定目录
            List<Pod> pods = getPods(info);
            for (int i = 0; i < pods.size(); i++) {
                Pod pod = pods.get(i);
                //默认第一个为master
                String containerName = i < 1 ? MASTER_CONTAINER_NAME : SLAVE_CONTAINER_NAME;
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, pretreatment, PRETREATMENT_TARGET_DIR);
            }
        } catch (Exception e) {
            log.error("copy pretreatment shell error: 【{}】",e);
            throw new OperatorException("exception is thrown when copy pretreatment for 【" + info.getParentName() + "】 : \n" + e.getMessage());
        }
    }

    /**
     * 创建statefulSet
     * @param info 资源信息
     */
    private void createStatefulSet(ChildResourceCreateInfo info) {
        log.info("createStatefulSet=>childResourceCreateInfo : 【{}】", info);
        StatefulSet statefulSet = client.apps().statefulSets()
                .inNamespace(info.getNamespace())
                .withName(info.getStatefulSetName()).get();
        //已存在
        if (statefulSet != null) {
            log.info("statefulSet 【{}】 already exists", statefulSet.getMetadata().getName());
            return;
        }
        //不存在，新建
        StatefulSetDeployer deployer = new BaseStatefulSetDeployer();
        StatefulSetBuilder builder = deployer.deploy(info);
        statefulSet = builder.build();
        client.apps().statefulSets().create(statefulSet);
        log.info("create statefulSet【{}】 successfully", statefulSet.getMetadata().getName());
    }

    /**
     * 等待statefulSet全部ready
     * @param info 资源信息
     */
    private void waitUntilStatefulSetReady(ChildResourceCreateInfo info) {
        log.info("wait for statefulSet 【{}】 in namespace 【{}】 ready", info.getStatefulSetName(), info.getNamespace());
        try {
            client.apps().statefulSets()
                    .inNamespace(info.getNamespace())
                    .withName(info.getStatefulSetName())
                    //阻塞 直到全部pod Ready  最长阻塞时间2小时
                    .waitUntilCondition(c ->
                                    c.getStatus().getReplicas() != null
                                            && ObjectUtil.equal(c.getStatus().getReplicas(), c.getStatus().getReadyReplicas()),
                            NUMBER_2, TimeUnit.HOURS);
            log.info("statefulSet 【{}】 in namespace 【{}】 is ready", info.getStatefulSetName(), info.getNamespace());
        } catch (Exception e) {
            log.error("wait until statefulSet ready error:【{}】", e);
            throw new OperatorException("exception is thrown when waiting for statefulSet 【" + info.getStatefulSetName() + "】 ready : \n" + e.getMessage());
        }
    }

    /**
     * 创建job
     * @param info Job信息
     */
    private void createJob(ChildResourceCreateInfo info) {
        log.info("createJob=>childResourceCreateInfo : 【{}】", info);
        Job job = client.batch().jobs()
                .inNamespace(info.getNamespace())
                .withName(info.getJobName()).get();
        //已存在
        if (job != null) {
            log.info("job 【{}】 already exists", job.getMetadata().getName());
            return;
        }
        //不存在，新建
        JobDeployer deployer = new BaseJobDeployer();
        JobBuilder builder = deployer.deploy(info);
        job = builder.build();
        log.info("job is : 【{}】", job);
        client.batch().jobs().create(job);
        log.info("create job【{}】 successfully", job.getMetadata().getName());
    }

    /**
     * 等待job全部ready
     * @param info 资源信息
     */
    private void waitUntilJobReady(ChildResourceCreateInfo info) {
        log.info("wait for job 【{}】 in namespace 【{}】 ready", info.getStatefulSetName(), info.getNamespace());
        try {
            List<Pod> podList = client.pods().inNamespace(info.getNamespace())
                    .withLabel(JOB_NAME, info.getJobName())
                    .list().getItems();
            while (CollectionUtil.isEmpty(podList)) {
                TimeUnit.SECONDS.sleep(2);
                podList = client.pods().inNamespace(info.getNamespace())
                        .withLabel(JOB_NAME, info.getJobName())
                        .list().getItems();
            }
            Pod pod = podList.get(0);
            client.pods().inNamespace(info.getNamespace())
                    .withName(pod.getMetadata().getName())
                    //等待直到Ready状态 最长2小时
                    .waitUntilReady(2, TimeUnit.HOURS);
            log.info("job 【{}】 in namespace 【{}】 is ready", info.getJobName(), info.getNamespace());
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            throw new OperatorException("exception is thrown when waiting for job 【" + info.getJobName() + "】 ready : \n" + e.getMessage());
        }
    }

    /**
     * 收集资源的podInfo
     * @param info 资源信息
     */
    private void validateAndCollectPods(ChildResourceCreateInfo info) {
        //检查是否都在正常运行
        log.info("validate pods status for 【{}】", info.getParentName());
        boolean isAllSlaveRunning = true;
        boolean isMasterRunning = true;
        Pod masterPod = null;
        List<Pod> slavePods = null;

        do {
            //取得主的pod
            masterPod = getMasterPod(info);

            //取得从的所有pod
            slavePods = getSlavePods(info);

            if (masterPod == null) {
                log.info("can not find pod belongs to job 【{}】", info.getJobName());
                return;
            }
            if (CollectionUtil.isEmpty(slavePods)) {
                log.info("can not find pod belongs to statefulSet 【{}】", info.getStatefulSetName());
                return;
            }

            isMasterRunning = RUNNING.equals(masterPod.getStatus().getPhase());
            isAllSlaveRunning = true;
            for (Pod slavePod : slavePods) {
                boolean isSlaveRunning = RUNNING.equals(slavePod.getStatus().getPhase());
                if (!isSlaveRunning) {
                    isAllSlaveRunning = false;
                    break;
                }
            }
        } while (!(isMasterRunning && isAllSlaveRunning));

        log.info("status checked 【{}】 all right", info.getParentName());
        collectChildPodInfo(info, masterPod, slavePods);
    }

    /**
     * 收集pod基本信息
     * @param info 资源信息
     * @param masterPod
     * @param slavePods
     */
    private void collectChildPodInfo(ChildResourceCreateInfo info, Pod masterPod, List<Pod> slavePods) {
        log.info("collectChildPodInfo=>childResourceCreateInfo : 【{}】, masterPod : 【{}】, slavePods : 【{}】", info, masterPod, slavePods);
        String key = info.getOwnerReference().getUid();
        if (dtMap.containsKey(key)) {
            dtMap.remove(key);
        }
        List<PodInfo> podInfos = Lists.newArrayList();
        PodInfo masterPodInfo = PodInfo.builder()
                .ip(masterPod.getStatus().getPodIP())
                .role(MASTER)
                .build();
        podInfos.add(masterPodInfo);
        for (Pod slavePod : slavePods) {
            PodInfo slavePodInfo = PodInfo.builder()
                    .ip(slavePod.getStatus().getPodIP())
                    .role(SLAVE)
                    .build();
            podInfos.add(slavePodInfo);
        }
        dtMap.put(key, podInfos);
    }

    /**
     * ssh免密互通相关配置
     * @param info 资源信息
     */
    private void sshAuthWithoutPass(ChildResourceCreateInfo info) {
        log.info("start to configure ssh no password environment for 【{}】 ", info.getParentName());
        File tempDir = Files.createTempDir();
        try (
                InputStream isRsa = getClass().getClassLoader().getResourceAsStream("key/id_rsa");
                InputStream isRsaPub = getClass().getClassLoader().getResourceAsStream("key/id_rsa.pub")
        ) {
            //id_rsa
            File tempIdRsa = FileUtil.createTempFile(tempDir);
            IOUtils.copy(isRsa, tempIdRsa);
            //id_rsa.pub
            File tempIdRsaPub = FileUtil.createTempFile(tempDir);
            IOUtils.copy(isRsaPub, tempIdRsaPub);
            List<String> pubLines = FileUtil.readLines(tempIdRsaPub, CHARSET);
            String pubKeyContent = pubLines.get(0);
            //按机器修改id_rsa.pub, 并组装一个大而全的authorized_keys
            List<File> idRsaPubFiles = Lists.newArrayList();
            File tempAuthorizedKeys = FileUtil.createTempFile(tempDir);
            List<String> pubKeys = Lists.newArrayList();
            for (PodInfo podInfo : dtMap.get(info.getOwnerReference().getUid())) {
                String podPubKeyContent = pubKeyContent.replace("{{ip}}", podInfo.getIp());
                File tempIdRsaPubOnPod = FileUtil.createTempFile(tempDir);
                FileUtil.writeLines(Collections.singletonList(podPubKeyContent), tempIdRsaPubOnPod, CHARSET);
                idRsaPubFiles.add(tempIdRsaPubOnPod);
                pubKeys.add(podPubKeyContent);
            }
            FileUtil.writeLines(pubKeys, tempAuthorizedKeys, CHARSET);

            //获得所有pod, 上传三个文件
            List<Pod> pods = getPods(info);
            for (int i = 0; i < pods.size(); i++) {
                Pod pod = pods.get(i);
                String containerName = i < 1 ? MASTER_CONTAINER_NAME : SLAVE_CONTAINER_NAME;
                //上传id_rsa
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, tempIdRsa, "/root/.ssh/id_rsa");
                //上传id_rsa.pub
                File tempIdRsaPubOnPod = idRsaPubFiles.get(i);
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, tempIdRsaPubOnPod, "/root/.ssh/id_rsa.pub");
                //上传authorized_keys
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, tempAuthorizedKeys, "/root/.ssh/authorized_keys");
                //修改权限
                String chmodCmd = StrUtil.format("chmod 644 /root/.ssh/authorized_keys && chmod 600 /root/.ssh/id_rsa && chmod 644 /root/.ssh/id_rsa.pub");
                podApi.exec(info.getNamespace(), pod.getMetadata().getName(), containerName, chmodCmd);
            }
            log.info("configure ssh no password environment for 【{}】 successfully ", info.getParentName());
        } catch (Exception e) {
            log.error("sshAuthWithoutPass error:【{}】", e);
            throw new OperatorException("exception is thrown when configure ssh no password environment for 【" + info.getParentName() + "】 : \n" + e.getMessage());
        } finally {
            //清理临时文件
            FileUtil.del(tempDir);
        }
    }

    /**
     * 生成并上传hostfile
     * @param info 资源信息
     */
    private void generateAndUploadHostFile(ChildResourceCreateInfo info) {
        log.info("start to configure hostfile for 【{}】 ", info.getParentName());
        File tempDir = Files.createTempDir();
        try {
            //生成hostfile
            JSONArray jsonArray = new JSONArray();
            List<PodInfo> podInfos = dtMap.get(info.getOwnerReference().getUid());
            for (PodInfo podInfo : podInfos) {
                JSONObject podJson = new JSONObject();
                podJson.put(IP, podInfo.getIp());
                podJson.put(ROLE, podInfo.getRole());
                jsonArray.add(podJson);
            }
            File tempHostFile = FileUtil.createTempFile(tempDir);
            FileUtil.writeLines(Collections.singletonList(jsonArray.toJSONString()), tempHostFile, CHARSET);
            //上传到pod指定目录
            List<Pod> pods = getPods(info);
            for (int i = 0; i < pods.size(); i++) {
                Pod pod = pods.get(i);
                String containerName = i < 1 ? MASTER_CONTAINER_NAME : SLAVE_CONTAINER_NAME;
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, tempHostFile, HOSTFILE_TARGET_DIR);
            }

        } catch (Exception e) {
            log.error("generateAndUploadHostFile error:【{}】", e);
            throw new OperatorException("exception is thrown when generate and upload hostfile for 【" + info.getParentName() + "】 : \n" + e.getMessage());
        } finally {
            //清理临时文件
            FileUtil.del(tempDir);
        }
    }

    /**
     * 创建service 解除闭锁
     * @param info
     */
    private void releaseInterLock(ChildResourceCreateInfo info) {
        log.info("release lock for 【{}】", info.getParentName());
        ServiceDeployer deployer = new BaseServiceDeployer();
        ServiceBuilder builder = deployer.deploy(info);
        Service svc = builder.build();
        client.services().create(svc);
        log.info("lock for 【{}】 released", info.getParentName());
    }

    /**
     * 回收cr
     * @param info
     */
    private void recycleCr(ChildResourceCreateInfo info) {
        log.info("recycleCr=>childResourceCreateInfo : 【{}】", info);
        Optional.ofNullable(DistributeTrainClientHolder.getClient())
                .ifPresent(distributeTrainClient -> {
                    ObjectMeta metadata = new ObjectMeta();
                    metadata.setName(info.getParentName());
                    metadata.setNamespace(info.getNamespace());
                    DistributeTrain dt = new DistributeTrain(metadata, DistributeTrainSpec.builder()
                            .build());
                    distributeTrainClient.delete(dt);
                    log.info("recycle distribute train 【{}】", info.getParentName());
                });
    }

    /**更新状态*/
    private void updateStatus(ChildResourceCreateInfo info, DistributeTrain distributeTrain) {
        log.info("updateStatus=>childResourceCreateInfo : 【{}】, distributeTrain : 【{}】", info, distributeTrain);
        if (distributeTrain.getStatus() == null) {
            distributeTrain.setStatus(new DistributeTrainStatus());
        }
        Integer size = distributeTrain.getSpec().getSize();
        distributeTrain.getStatus().setReplicas(size);
        distributeTrain.getStatus().setReadyReplicas(size);
    }

    /**
     * 为job注册监听器
     * @param info
     */
    private void registerJobListener(ChildResourceCreateInfo info) {
        log.info("register listener for distribute train 【{}】", info.getParentName());
//        client.batch().jobs()
//                .inNamespace(info.getNamespace())
//                .withName(info.getJobName()).watch(null);
    }

    /**
     * 获取所有分布式训练相关的pod
     * @param info
     * @return List<Pod> 分布式相关Pod集合
     */
    private List<Pod> getPods(ChildResourceCreateInfo info) {
        log.info("getPods=>childResourceCreateInfo : 【{}】", info);
        List<Pod> pods = Lists.newArrayList();
        pods.add(getMasterPod(info));
        pods.addAll(getSlavePods(info));
        if (CollectionUtil.hasNull(pods) || pods.size() != info.getSlaveReplicas() + 1) {
            throw new OperatorException("can not get pods in correct numbers");
        }
        return pods;
    }

    /**
     * 获取master信息
     * @param info 资源信息
     * @return Pod Master节点对应的Pod
     */
    private Pod getMasterPod(ChildResourceCreateInfo info) {
        log.info("getMasterPod=>childResourceCreateInfo : 【{}】", info);
        List<Pod> masterPods = client.pods().inNamespace(info.getNamespace())
                .withLabel(JOB_LABEL, info.getJobName())
                .list().getItems();
        if (CollectionUtil.isEmpty(masterPods)) {
            return null;
        }
        return masterPods.get(0);
    }

    /**
     * 取得从的所有pod
     * @param info 资源信息
     * @return List<Pod> Slave节点对应的Pod集合
     */
    private List<Pod> getSlavePods(ChildResourceCreateInfo info) {
        log.info("getSlavePods=>childResourceCreateInfo : 【{}】", info);
        //取得从的所有pod
        List<Pod> slavePods = client.pods().inNamespace(info.getNamespace())
                .withLabel(STATEFULSET_LABEL, info.getStatefulSetName())
                .list().getItems();
        if (CollectionUtil.isEmpty(slavePods)) {
            return null;
        }
        return slavePods;
    }

}
