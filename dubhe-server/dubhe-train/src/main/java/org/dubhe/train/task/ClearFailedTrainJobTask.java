package org.dubhe.train.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.DistributeTrainApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.train.dao.PtTrainJobMapper;
import org.dubhe.train.domain.entity.PtTrainJob;
import org.dubhe.train.enums.TrainJobStatusEnum;
import org.dubhe.train.enums.TrainTypeEnum;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ClearFailedTrainJobTask {

    @Resource
    private PtTrainJobMapper ptTrainJobMapper;

    @Resource
    private K8sNameTool k8sNameTool;

    @Resource
    private PodApi podApi;

    @Resource
    private TrainJobApi trainJobApi;

    @Resource
    private DistributeTrainApi distributeTrainApi;

    @Resource
    private UserContextService  userContextService;

    /**
     * 每隔30s执行
     */
    @Scheduled(cron = "*/30 * * * * ?")
    public void clear() {
        //查询失败的训练任务
        QueryWrapper<PtTrainJob> queryTrainJonWrapper = new QueryWrapper<>();
        queryTrainJonWrapper.eq("train_status", TrainJobStatusEnum.FAILED.getStatus());
        List<PtTrainJob> failedTrainJobs = ptTrainJobMapper.selectList(queryTrainJonWrapper);

        //删除失败训练任务的k8s资源
        failedTrainJobs.forEach(job -> {
            String namespace = k8sNameTool.generateNamespace(job.getCreateUserId());
            UserContext currentUser = userContextService.getCurUser();
            boolean bool = TrainTypeEnum.isDistributeTrain(job.getTrainType()) ?
                    distributeTrainApi.deleteByResourceName(namespace, job.getJobName()).isSuccess() :
                    trainJobApi.delete(namespace, job.getJobName());
            if (!bool) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} delete training Job and k8s fails in the delete process, namespace is {}, resourceName is {}",
                        currentUser.getUsername(), namespace, job.getJobName());
            }
        });
    }
}
