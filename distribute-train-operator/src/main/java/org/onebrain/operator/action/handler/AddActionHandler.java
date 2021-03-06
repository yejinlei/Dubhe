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
 * @description ???????????????????????????????????????
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
     * String ??????uid  List pod??????
     */
    private Map<String, List<PodInfo>> dtMap = new ConcurrentHashMap();

    @Autowired
    private RedisService redis;

    /**
     * ?????????
     */
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1), new ThreadFactory() {
        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, JOB_WATCHER + mThreadNum.getAndIncrement());
        }
    }, new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * ?????????????????????
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
     * ??????????????????
     * @param distributeTrain
     */
    public void doAction(DistributeTrain distributeTrain) {
        log.info("doAction=>distributeTrain : ???{}???", distributeTrain.getMetadata().getName());
        ChildResourceCreateInfo info = null;
        try {
            //redis????????????
            //??????k8s ??????DistributionTrain ???uid??????
            if (null != redis.get(OperatorKey.CR, distributeTrain.getMetadata().getUid())) {
                log.info("distribute train ???{}??? in namespace ???{}??? already exists", distributeTrain.getMetadata().getName(), distributeTrain.getMetadata().getNamespace());
                return;
            } else {
                //??????redis???????????????
                redis.set(OperatorKey.CR, distributeTrain.getMetadata().getUid(), System.currentTimeMillis());
            }

            //??????????????????????????????????????????
            validateParams(distributeTrain);
            info = ChildResourceCreateInfo.fromCr(distributeTrain);
            //??????size?????????????????????size-1???statefulSet
            createStatefulSet(info);
            //??????statefulset??????ready
            waitUntilStatefulSetReady(info);
            //??????job???job??????????????????
            createJob(info);
            //??????job ready
            waitUntilJobReady(info);
            //?????? /home/pretreatment ??? pod
            copyPretreatmentShell(info);
            //??????statefulSet???job???ip
            validateAndCollectPods(info);
            //??????????????????????????????????????????????????????????????????~/.ssh?????????
            sshAuthWithoutPass(info);
            //????????????hostfile?????????????????????????????????????????????
            generateAndUploadHostFile(info);
            //??????job????????????
            releaseInterLock(info);
            //?????????
            //updateStatus(info, distributeTrain);
            //???job???????????????
            registerJobListener(info);

            log.info("all parts of???{}??? are ready", info.getParentName());
        } catch (Exception e) {
            log.error("doAction error:???{}???", e);
            //????????????
            redis.del(OperatorKey.CR, distributeTrain.getMetadata().getUid());
            //?????????????????????
            if (info != null) {
                recycleCr(info);
            }
        }
    }

    /**
     * ?????????????????????
     * @param distributeTrain ?????????????????????
     */
    @Override
    public void handlerAction(DistributeTrain distributeTrain) {
        log.info("handlerAction=>distributeTrain : ???{}???", distributeTrain.getMetadata().getName());
        HandlerActionTask handlerActionTask = new HandlerActionTask(distributeTrain);
        pool.getActiveCount();
        pool.execute(handlerActionTask);
    }

    /**
     * ?????????????????????
     * @param distributeTrain ???????????????
     */
    private void validateParams(DistributeTrain distributeTrain) {
        log.info("validateParams=>distributeTrain : ???{}???", distributeTrain.getMetadata().getName());
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
     * ????????????pretreatment???pod
     * @param info ????????????
     */
    private void copyPretreatmentShell(ChildResourceCreateInfo info) {
        log.info("start to copy pretreatment for ???{}??? ", info.getParentName());
        try {
            String path = System.getProperty(KubeConstants.USER_DIR_SYSTEM_PROPERTY) + File.separator + PRETREATMENT;
            if (!FileUtil.exist(path)) {
                FileUtil.writeFromStream(new ClassPathResource("/shell/pretreatment").getInputStream(), path);
            }
            File pretreatment = new File(path);
            //?????????pod????????????
            List<Pod> pods = getPods(info);
            for (int i = 0; i < pods.size(); i++) {
                Pod pod = pods.get(i);
                //??????????????????master
                String containerName = i < 1 ? MASTER_CONTAINER_NAME : SLAVE_CONTAINER_NAME;
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, pretreatment, PRETREATMENT_TARGET_DIR);
            }
        } catch (Exception e) {
            log.error("copy pretreatment shell error: ???{}???",e);
            throw new OperatorException("exception is thrown when copy pretreatment for ???" + info.getParentName() + "??? : \n" + e.getMessage());
        }
    }

    /**
     * ??????statefulSet
     * @param info ????????????
     */
    private void createStatefulSet(ChildResourceCreateInfo info) {
        log.info("createStatefulSet=>childResourceCreateInfo : ???{}???", info.getParentName());
        StatefulSet statefulSet = client.apps().statefulSets()
                .inNamespace(info.getNamespace())
                .withName(info.getStatefulSetName()).get();
        //?????????
        if (statefulSet != null) {
            log.info("statefulSet ???{}??? already exists", statefulSet.getMetadata().getName());
            return;
        }
        //??????????????????
        StatefulSetDeployer deployer = new BaseStatefulSetDeployer();
        StatefulSetBuilder builder = deployer.deploy(info);
        statefulSet = builder.build();
        client.apps().statefulSets().create(statefulSet);
        log.info("create statefulSet???{}??? successfully", statefulSet.getMetadata().getName());
    }

    /**
     * ??????statefulSet??????ready
     * @param info ????????????
     */
    private void waitUntilStatefulSetReady(ChildResourceCreateInfo info) {
        log.info("wait for statefulSet ???{}??? in namespace ???{}??? ready", info.getStatefulSetName(), info.getNamespace());
        try {
            client.apps().statefulSets()
                    .inNamespace(info.getNamespace())
                    .withName(info.getStatefulSetName())
                    //?????? ????????????pod Ready  ??????????????????2??????
                    .waitUntilCondition(c ->
                                    c.getStatus().getReplicas() != null
                                            && ObjectUtil.equal(c.getStatus().getReplicas(), c.getStatus().getReadyReplicas()),
                            NUMBER_2, TimeUnit.HOURS);
            log.info("statefulSet ???{}??? in namespace ???{}??? is ready", info.getStatefulSetName(), info.getNamespace());
        } catch (Exception e) {
            log.error("wait until statefulSet ready error:???{}???", e);
            throw new OperatorException("exception is thrown when waiting for statefulSet ???" + info.getStatefulSetName() + "??? ready : \n" + e.getMessage());
        }
    }

    /**
     * ??????job
     * @param info Job??????
     */
    private void createJob(ChildResourceCreateInfo info) {
        log.info("createJob=>childResourceCreateInfo : ???{}???", info.getParentName());
        Job job = client.batch().jobs()
                .inNamespace(info.getNamespace())
                .withName(info.getJobName()).get();
        //?????????
        if (job != null) {
            log.info("job ???{}??? already exists", job.getMetadata().getName());
            return;
        }
        //??????????????????
        JobDeployer deployer = new BaseJobDeployer();
        JobBuilder builder = deployer.deploy(info);
        job = builder.build();
        log.info("job is : ???{}???", job.getMetadata().getName());
        client.batch().jobs().create(job);
        log.info("create job???{}??? successfully", job.getMetadata().getName());
    }

    /**
     * ??????job??????ready
     * @param info ????????????
     */
    private void waitUntilJobReady(ChildResourceCreateInfo info) {
        log.info("wait for job ???{}??? in namespace ???{}??? ready", info.getStatefulSetName(), info.getNamespace());
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
                    //????????????Ready?????? ??????2??????
                    .waitUntilReady(2, TimeUnit.HOURS);
            log.info("job ???{}??? in namespace ???{}??? is ready", info.getJobName(), info.getNamespace());
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            throw new OperatorException("exception is thrown when waiting for job ???" + info.getJobName() + "??? ready : \n" + e.getMessage());
        }
    }

    /**
     * ???????????????podInfo
     * @param info ????????????
     */
    private void validateAndCollectPods(ChildResourceCreateInfo info) {
        //??????????????????????????????
        log.info("validate pods status for ???{}???", info.getParentName());
        boolean isAllSlaveRunning = true;
        boolean isMasterRunning = true;
        Pod masterPod = null;
        List<Pod> slavePods = null;

        do {
            //????????????pod
            masterPod = getMasterPod(info);

            //??????????????????pod
            slavePods = getSlavePods(info);

            if (masterPod == null) {
                log.info("can not find pod belongs to job ???{}???", info.getJobName());
                return;
            }
            if (CollectionUtil.isEmpty(slavePods)) {
                log.info("can not find pod belongs to statefulSet ???{}???", info.getStatefulSetName());
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

        log.info("status checked ???{}??? all right", info.getParentName());
        collectChildPodInfo(info, masterPod, slavePods);
    }

    /**
     * ??????pod????????????
     * @param info ????????????
     * @param masterPod
     * @param slavePods
     */
    private void collectChildPodInfo(ChildResourceCreateInfo info, Pod masterPod, List<Pod> slavePods) {
        log.info("collectChildPodInfo=>childResourceCreateInfo : ???{}???, masterPod : ???{}???", info.getParentName(), masterPod.getMetadata().getName());
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
     * ssh????????????????????????
     * @param info ????????????
     */
    private void sshAuthWithoutPass(ChildResourceCreateInfo info) {
        log.info("start to configure ssh no password environment for ???{}??? ", info.getParentName());
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
            //???????????????id_rsa.pub, ???????????????????????????authorized_keys
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

            //????????????pod, ??????????????????
            List<Pod> pods = getPods(info);
            for (int i = 0; i < pods.size(); i++) {
                Pod pod = pods.get(i);
                String containerName = i < 1 ? MASTER_CONTAINER_NAME : SLAVE_CONTAINER_NAME;
                //??????id_rsa
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, tempIdRsa, "/root/.ssh/id_rsa");
                //??????id_rsa.pub
                File tempIdRsaPubOnPod = idRsaPubFiles.get(i);
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, tempIdRsaPubOnPod, "/root/.ssh/id_rsa.pub");
                //??????authorized_keys
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, tempAuthorizedKeys, "/root/.ssh/authorized_keys");
                //????????????
                String chmodCmd = StrUtil.format("chmod 644 /root/.ssh/authorized_keys && chmod 600 /root/.ssh/id_rsa && chmod 644 /root/.ssh/id_rsa.pub");
                podApi.exec(info.getNamespace(), pod.getMetadata().getName(), containerName, chmodCmd);
            }
            log.info("configure ssh no password environment for ???{}??? successfully ", info.getParentName());
        } catch (Exception e) {
            log.error("sshAuthWithoutPass error:???{}???", e);
            throw new OperatorException("exception is thrown when configure ssh no password environment for ???" + info.getParentName() + "??? : \n" + e.getMessage());
        } finally {
            //??????????????????
            FileUtil.del(tempDir);
        }
    }

    /**
     * ???????????????hostfile
     * @param info ????????????
     */
    private void generateAndUploadHostFile(ChildResourceCreateInfo info) {
        log.info("start to configure hostfile for ???{}??? ", info.getParentName());
        File tempDir = Files.createTempDir();
        try {
            //??????hostfile
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
            //?????????pod????????????
            List<Pod> pods = getPods(info);
            for (int i = 0; i < pods.size(); i++) {
                Pod pod = pods.get(i);
                String containerName = i < 1 ? MASTER_CONTAINER_NAME : SLAVE_CONTAINER_NAME;
                podApi.copyToPod(info.getNamespace(), pod.getMetadata().getName(), containerName, tempHostFile, HOSTFILE_TARGET_DIR);
            }

        } catch (Exception e) {
            log.error("generateAndUploadHostFile error:???{}???", e);
            throw new OperatorException("exception is thrown when generate and upload hostfile for ???" + info.getParentName() + "??? : \n" + e.getMessage());
        } finally {
            //??????????????????
            FileUtil.del(tempDir);
        }
    }

    /**
     * ??????service ????????????
     * @param info
     */
    private void releaseInterLock(ChildResourceCreateInfo info) {
        log.info("release lock for ???{}???", info.getParentName());
        ServiceDeployer deployer = new BaseServiceDeployer();
        ServiceBuilder builder = deployer.deploy(info);
        Service svc = builder.build();
        client.services().create(svc);
        log.info("lock for ???{}??? released", info.getParentName());
    }

    /**
     * ??????cr
     * @param info
     */
    private void recycleCr(ChildResourceCreateInfo info) {
        log.info("recycleCr=>childResourceCreateInfo : ???{}???", info.getParentName());
        Optional.ofNullable(DistributeTrainClientHolder.getClient())
                .ifPresent(distributeTrainClient -> {
                    ObjectMeta metadata = new ObjectMeta();
                    metadata.setName(info.getParentName());
                    metadata.setNamespace(info.getNamespace());
                    DistributeTrain dt = new DistributeTrain(metadata, DistributeTrainSpec.builder()
                            .build());
                    distributeTrainClient.delete(dt);
                    log.info("recycle distribute train ???{}???", info.getParentName());
                });
    }

    /**????????????*/
    private void updateStatus(ChildResourceCreateInfo info, DistributeTrain distributeTrain) {
        log.info("updateStatus=>childResourceCreateInfo : ???{}???, distributeTrain : ???{}???", info.getParentName(), distributeTrain.getMetadata().getName());
        if (distributeTrain.getStatus() == null) {
            distributeTrain.setStatus(new DistributeTrainStatus());
        }
        Integer size = distributeTrain.getSpec().getSize();
        distributeTrain.getStatus().setReplicas(size);
        distributeTrain.getStatus().setReadyReplicas(size);
    }

    /**
     * ???job???????????????
     * @param info
     */
    private void registerJobListener(ChildResourceCreateInfo info) {
        log.info("register listener for distribute train ???{}???", info.getParentName());
//        client.batch().jobs()
//                .inNamespace(info.getNamespace())
//                .withName(info.getJobName()).watch(null);
    }

    /**
     * ????????????????????????????????????pod
     * @param info
     * @return List<Pod> ???????????????Pod??????
     */
    private List<Pod> getPods(ChildResourceCreateInfo info) {
        log.info("getPods=>childResourceCreateInfo : ???{}???", info.getParentName());
        List<Pod> pods = Lists.newArrayList();
        pods.add(getMasterPod(info));
        pods.addAll(getSlavePods(info));
        if (CollectionUtil.hasNull(pods) || pods.size() != info.getSlaveReplicas() + 1) {
            throw new OperatorException("can not get pods in correct numbers");
        }
        return pods;
    }

    /**
     * ??????master??????
     * @param info ????????????
     * @return Pod Master???????????????Pod
     */
    private Pod getMasterPod(ChildResourceCreateInfo info) {
        log.info("getMasterPod=>childResourceCreateInfo : ???{}???", info.getParentName());
        List<Pod> masterPods = client.pods().inNamespace(info.getNamespace())
                .withLabel(JOB_LABEL, info.getJobName())
                .list().getItems();
        if (CollectionUtil.isEmpty(masterPods)) {
            return null;
        }
        return masterPods.get(0);
    }

    /**
     * ??????????????????pod
     * @param info ????????????
     * @return List<Pod> Slave???????????????Pod??????
     */
    private List<Pod> getSlavePods(ChildResourceCreateInfo info) {
        log.info("getSlavePods=>childResourceCreateInfo : ???{}???", info.getParentName());
        //??????????????????pod
        List<Pod> slavePods = client.pods().inNamespace(info.getNamespace())
                .withLabel(STATEFULSET_LABEL, info.getStatefulSetName())
                .list().getItems();
        if (CollectionUtil.isEmpty(slavePods)) {
            return null;
        }
        return slavePods;
    }

}
