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
package org.dubhe.admin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.admin.service.RecycleTaskService;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.api.impl.ShellFileStoreApiImpl;
import org.dubhe.biz.file.utils.IOUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.dao.RecycleDetailMapper;
import org.dubhe.recycle.dao.RecycleMapper;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleTaskQueryDTO;
import org.dubhe.recycle.domain.entity.Recycle;
import org.dubhe.recycle.domain.entity.RecycleDetail;
import org.dubhe.recycle.enums.RecycleStatusEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @description 垃圾回收
 * @date 2021-01-20
 */
@Service
@RefreshScope
public class RecycleTaskServiceImpl implements RecycleTaskService {

    @Autowired
    private RecycleMapper recycleMapper;

    @Autowired
    private RecycleDetailMapper recycleDetailMapper;

    @Autowired
    private UserContextService userContextService;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    @Value("${storage.file-store}")
    private String ip;

    @Value("${data.server.userName}")
    private String userName;

    /**
     * 资源回收单次执行任务数量限制（默认10000）
     */
    @Value("${recycle.task.execute-limits:10000}")
    private String taskExecuteLimits;
    /**
     * 资源无效文件临时存放目录(默认/tmp/tmp_)
     */
    @Value("${recycle.file-tmp-path.invalid:/tmp/tmp_}")
    private String invalidFileTmpPath;
    /**
     * 资源无效文件临时存放目录(默认/tmp/empty_)
     */
    @Value("${recycle.file-tmp-path.recycle:/tmp/empty_}")
    private String recycleFileTmpPath;

    @Autowired
    private RecycleConfig recycleConfig;

    @Autowired
    private RecycleService recycleService;

    @Autowired
    private RecycleTool recycleTool;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 查询回收任务列表
     *
     * @param recycleTaskQueryDTO 查询任务列表条件
     * @return Map<String, Object> 可回收任务列表
     */
    @Override
    public Map<String, Object> getRecycleTasks(RecycleTaskQueryDTO recycleTaskQueryDTO) {
        //获取当前用户信息
        Long curUserId = userContextService.getCurUserId();
        Page page = recycleTaskQueryDTO.toPage();
        LambdaQueryWrapper<Recycle> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(CollectionUtil.isNotEmpty(recycleTaskQueryDTO.getRecycleTaskIdList()), Recycle::getId, recycleTaskQueryDTO.getRecycleTaskIdList());
        queryWrapper.eq(recycleTaskQueryDTO.getRecycleStatus() != null, Recycle::getRecycleStatus, recycleTaskQueryDTO.getRecycleStatus());
        queryWrapper.eq(recycleTaskQueryDTO.getRecycleModel() != null, Recycle::getRecycleModule, recycleTaskQueryDTO.getRecycleModel());
        if (!BaseService.isAdmin()) {
            queryWrapper.eq(Recycle::getCreateUserId, curUserId);
        }
        queryWrapper.last(" ORDER BY recycle_delay_date DESC,update_time DESC ");
        List<Recycle> recycleList = recycleMapper.selectPage(page, queryWrapper).getRecords();
        return PageUtil.toPage(page, recycleList);
    }

    /**
     *  获取垃圾回收任务列表
     *  资源回收单次执行任务数量限制（默认10000）
     * @return List<RecycleTask> 垃圾回收任务列表
     */
    @Override
    public List<Recycle> getRecycleTaskList() {
        List<Recycle> recycleTaskList = recycleMapper.selectList(new LambdaQueryWrapper<Recycle>()
                .in(Recycle::getRecycleStatus, Arrays.asList(RecycleStatusEnum.PENDING.getCode(), RecycleStatusEnum.FAILED.getCode()))
                .le(Recycle::getRecycleDelayDate, DateUtil.format(new Date(), "yyyy-MM-dd"))
                .last(" limit ".concat(taskExecuteLimits))
        );
        return recycleTaskList;
    }

    /**
     * 执行回收任务(单个)
     * @param recycle 回收实体类
     * @param userId 当前操作用户
     */
    @Override
    public void recycleTask(Recycle recycle, long userId) {
        if (StrUtil.isNotEmpty(recycle.getRecycleCustom())) {
            // 自定义回收
            customRecycle(recycle, userId, RecycleTool.BIZ_RECYCLE);
        } else {
            // 默认回收0
            defaultRecycle(recycle, userId);
        }
    }

    /**
     * 获取任务详情
     * @param recycleId 回收任务ID
     * @return List<RecycleDetail>
     */
    private List<RecycleDetail> getDetail(long recycleId) {
        return recycleDetailMapper.selectList(new LambdaQueryWrapper<RecycleDetail>()
                .eq(RecycleDetail::getRecycleId, recycleId)
        );
    }

    /**
     * 自定义回收远程调用
     *
     * @param recycle       回收任务
     * @param userId            用户ID
     * @param biz               模块业务
     */
    private void customRecycle(Recycle recycle, long userId, String biz) {
        recycle.setUpdateUserId(userId);
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.recycleTaskCreateDTO(recycle);
        recycleCreateDTO.setDetailList(getDetail(recycle.getId()));
        // 远程调用
        String url = RecycleTool.getCallUrl(recycleCreateDTO.getRecycleModule(), biz);
        String token = recycleTool.generateToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(RecycleTool.RECYCLE_TOKEN, token);
        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(recycleCreateDTO), headers);
        ResponseEntity<DataResponseBody> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(url, entity, DataResponseBody.class);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_SYS, "RecycleTaskServiceImpl customRecycle error :{}", e);
            throw new BusinessException(responseEntity.getStatusCodeValue(), "自定义回收【" + biz + "】远程调用失败！");
        }
        if (HttpStatus.HTTP_OK != responseEntity.getStatusCodeValue()) {
            throw new BusinessException(responseEntity.getStatusCodeValue(), "自定义回收【" + biz + "】远程调用失败！");
        }
        DataResponseBody dataResponseBody = responseEntity.getBody();
        if (!dataResponseBody.succeed()) {
            throw new BusinessException(dataResponseBody.getCode(), dataResponseBody.getMsg());
        }
    }

    /**
     * 默认回收
     * (无统一事务操作)
     *
     * @param recycle       回收任务
     * @param userId            用户ID
     */
    private void defaultRecycle(Recycle recycle, long userId) {
        try {
            List<RecycleDetail> detailList = getDetail(recycle.getId());
            recycleService.updateRecycle(recycle, RecycleStatusEnum.DOING, null, userId);
            for (RecycleDetail detail : detailList) {
                if (Objects.equals(RecycleTypeEnum.FILE.getCode(), detail.getRecycleType())) {
                    // 文件回收
                    if (!defaultRecycleFile(detail, userId)) {
                        recycleService.updateRecycle(recycle, RecycleStatusEnum.FAILED, "文件回收失败！" + detail.getId(), userId);
                        return;
                    }
                } else {
                    // 其他资源（数据库资源）回收
                    if (!defaultRecycleOthers(detail, userId)) {
                        recycleService.updateRecycle(recycle, RecycleStatusEnum.FAILED, "不支持非文件资源默认回收方式！" + detail.getId(), userId);
                        return;
                    }
                }
            }
            recycleService.updateRecycle(recycle, RecycleStatusEnum.SUCCEEDED, null, userId);
        } catch (Exception e) {
            LogUtil.error(LogEnum.GARBAGE_RECYCLE, "默认回收失败！{}", e);
            recycleService.updateRecycle(recycle, RecycleStatusEnum.FAILED, e.getMessage(), userId);
        }
    }


    /**
     * 默认方式文件回收
     * @param detail        回收任务详情
     * @param userId            用户ID
     * @return true 回收成功， false 回收失败
     */
    private boolean defaultRecycleFile(RecycleDetail detail, long userId) {
        if (RecycleStatusEnum.SUCCEEDED.getCode().equals(detail.getRecycleStatus())) {
            // 已经删除成功，无需再执行
            return true;
        }
        String errMsg = deleteFileByCMD(detail.getRecycleCondition(), detail.getId().toString());
        recycleService.updateRecycleDetail(detail,
                StrUtil.isEmpty(errMsg) ? RecycleStatusEnum.SUCCEEDED : RecycleStatusEnum.FAILED,
                errMsg,
                userId
        );
        return StrUtil.isEmpty(errMsg);
    }

    /**
     * 默认方式回收其他资源（暂不支持）
     * @param detail       回收任务详情
     * @param userId            用户ID
     * @return false 回收失败
     */
    private boolean defaultRecycleOthers(RecycleDetail detail, long userId) {
        LogUtil.warn(LogEnum.GARBAGE_RECYCLE, "recycle task id:{} is not support", detail.getId());
        recycleService.updateRecycleDetail(detail, RecycleStatusEnum.FAILED, "不支持非文件资源默认回收方式！", userId);
        return false;
    }


    /**
     * 实时删除临时目录完整路径无效文件
     *
     * @param sourcePath 删除路径
     */
    @Override
    public void delTempInvalidResources(String sourcePath) {
        if (!BaseService.isAdmin(userContextService.getCurUser())) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "不支持普通用户操作");
        }
        String resMsg = deleteFileByCMD(sourcePath, RandomUtil.randomString(MagicNumConstant.TWO));
        if (StrUtil.isNotEmpty(resMsg)) {
            throw new BusinessException(ResponseCode.ERROR, resMsg);
        }
    }

    /**
     * 回收天枢一站式平台中的无效文件资源
     * 处理方式：获取到回收任务表中的无效文件路径，通过linux命令进行具体删除
     * 文件路径必须满足格式如：/nfs/当前系统环境/具体删除的文件或文件夹(至少三层目录)
     * @param recycleConditionPath 文件回收绝对路径
     * @param randomPath emptyDir目录补偿位置
     * @return String 回收任务失败返回的失败信息
     */
    private String deleteFileByCMD(String recycleConditionPath, String randomPath) {
        String sourcePath = fileStoreApi.formatPath(recycleConditionPath);
        //判断该路径是否存在文件或文件夹
        String nfsBucket = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + File.separator);
        sourcePath = sourcePath.endsWith(File.separator) ? sourcePath : sourcePath + File.separator;
        try {
            //校验回收文件是否存在以及回收文件必须至少在当前环境目录下还有一层目录，如：/nfs/dubhe-test/xxxx/
            if (sourcePath.startsWith(nfsBucket)
                    && sourcePath.length() > nfsBucket.length()) {
                if (!fileStoreApi.fileOrDirIsExist(sourcePath)) {
                    // 文件不存在，即认为已删除成功
                    return null;
                }
                String emptyDir = recycleFileTmpPath + randomPath + File.separator;
                LogUtil.debug(LogEnum.GARBAGE_RECYCLE, "recycle task sourcePath:{},emptyDir:{}", sourcePath, emptyDir);
                Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.format(ShellFileStoreApiImpl.DEL_COMMAND, emptyDir, emptyDir, sourcePath, emptyDir, sourcePath)});
                return processRecycle(process);
            } else {
                LogUtil.error(LogEnum.GARBAGE_RECYCLE, "file recycle is failed! sourcePath:{}", sourcePath);
                return "文件资源回收失败! sourcePath:" + sourcePath;
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.GARBAGE_RECYCLE, "file recycle is failed! Exception:{}", e);
            return "文件资源回收失败! sourcePath:" + sourcePath + " Exception:" + e.getMessage();
        }
    }

    /**
     * 执行服务器命令
     *
     * @param process Process对象
     * @return null 成功执行，其他：异常结束信息
     */
    private String processRecycle(Process process) {
        InputStreamReader stream = new InputStreamReader(process.getErrorStream());
        BufferedReader reader = new BufferedReader(stream);
        StringBuilder errMessage = new StringBuilder();
        try {
            while (reader.read() != MagicNumConstant.NEGATIVE_ONE) {
                errMessage.append(reader.readLine());
            }
            int status = process.waitFor();
            if (status == 0) {
                // 成功
                return null;
            } else {
                // 失败
                LogUtil.info(LogEnum.GARBAGE_RECYCLE, "recycleSourceIsOk is failure,errorMsg:{},processStatus:{}", errMessage.toString(), status);
                return errMessage.length() > 0 ? errMessage.toString() : "文件删除失败！";
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.GARBAGE_RECYCLE, "recycleSourceIsOk is failure: {} ", e);
            return e.getMessage();
        } finally {
            IOUtil.close(reader, stream);
        }
    }

    /**
     * 立即执行回收任务
     *
     * @param taskId 回收任务ID
     */
    @Override
    public void recycleTaskResources(long taskId) {
        //根据taskId查询回收任务
        Recycle recycle = getRecycleTask(taskId, Arrays.asList(RecycleStatusEnum.PENDING.getCode(), RecycleStatusEnum.FAILED.getCode()));
        //执行回收任务
        recycleTask(recycle, recycle.getUpdateUserId());
    }

    /**
     * 获取任务并校验
     * 更新修改人ID为当前操作人
     *
     * @param taskId         回收任务ID
     * @param statusEnumList 回收状态
     * @return Recycle
     */
    private Recycle getRecycleTask(long taskId, List<Integer> statusEnumList) {
        //根据taskId查询回收任务
        Recycle recycle = recycleMapper.selectOne(new LambdaQueryWrapper<Recycle>()
                .eq(Recycle::getId, taskId)
                .in(CollectionUtil.isNotEmpty(statusEnumList), Recycle::getRecycleStatus, statusEnumList));
        if (recycle == null) {
            throw new BusinessException("未查询到回收任务");
        }
        UserContext curUser = userContextService.getCurUser();
        //只有创建该任务用户或管理员有权限实时执行回收任务
        if (!recycle.getCreateUserId().equals(curUser.getId()) && !BaseService.isAdmin(curUser)) {
            throw new BusinessException("没有权限操作");
        }
        recycle.setUpdateUserId(curUser.getId());
        return recycle;
    }


    /**
     * 还原回收任务
     *
     * @param taskId 回收任务ID
     */
    @Override
    public void restore(long taskId) {
        //根据taskId查询回收任务
        Recycle recycle = getRecycleTask(taskId, Arrays.asList(RecycleStatusEnum.PENDING.getCode(), RecycleStatusEnum.FAILED.getCode()));
        if (StrUtil.isEmpty(recycle.getRestoreCustom())) {
            throw new BusinessException("仅支持自定义还原！");
        } else {
            long userId = recycle.getUpdateUserId();
            // 自定义还原
            customRecycle(recycle, userId, RecycleTool.BIZ_RESTORE);
        }
    }

    /**
     * 根据路径回收无效文件
     *
     * @param sourcePath 文件路径
     */
    @Override
    public void deleteInvalidResourcesByCMD(String sourcePath) {
        //判断该路径是否存在文件或文件夹
        if (!fileStoreApi.fileOrDirIsExist(sourcePath)) {
            return;
        }
        File file = new File(sourcePath);
        File[] files = file.listFiles();
        if (files != null && files.length != 0) {
            for (File f : files) {
                //获取文件夹命名（userId）
                String fileName = f.getName();
                if (!f.isDirectory()) {
                    continue;
                }
                File[] director = f.listFiles();
                if (director != null && director.length != 0) {
                    for (File directory : director) {
                        //获取文件夹命名（时间戳+4位随机数）
                        String directoryName = directory.getName();
                        //如果文件上传时长大于最大有效时间，则删除
                        if ((System.currentTimeMillis() - directory.lastModified()) >= recycleConfig.getFileValid() * MagicNumConstant.SIXTY * MagicNumConstant.SIXTY * MagicNumConstant.ONE_THOUSAND) {
                            try {
                                String delRealPath = fileStoreApi.formatPath(sourcePath + File.separator + fileName + File.separator + directoryName);
                                delRealPath = delRealPath.endsWith(File.separator) ? delRealPath : delRealPath + File.separator;
                                String emptyDir = invalidFileTmpPath + directoryName + File.separator;
                                Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.format(ShellFileStoreApiImpl.DEL_COMMAND, emptyDir, emptyDir, delRealPath, emptyDir, delRealPath)});
                                Integer deleteStatus = process.waitFor();
                                LogUtil.info(LogEnum.GARBAGE_RECYCLE, "recycle resources path:{},recycle status:{}", delRealPath, deleteStatus);
                            } catch (Exception e) {
                                LogUtil.error(LogEnum.GARBAGE_RECYCLE, "recycle invalid resources error:{}", e);
                            }
                        }
                    }
                }
            }
        }
    }
}
