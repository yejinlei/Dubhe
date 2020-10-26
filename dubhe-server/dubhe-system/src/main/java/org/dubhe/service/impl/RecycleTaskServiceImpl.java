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
package org.dubhe.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.config.NfsConfig;
import org.dubhe.config.RecycleConfig;
import org.dubhe.constatnts.UserConstant;
import org.dubhe.dao.RecycleTaskMapper;
import org.dubhe.domain.dto.RecycleTaskCreateDTO;
import org.dubhe.domain.dto.RecycleTaskQueryDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.RecycleTask;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.RecycleStatusEnum;
import org.dubhe.enums.RecycleTypeEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.RecycleTaskService;
import org.dubhe.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description 垃圾回收 实现类
 * @date 2020-09-17
 */
@Service
public class RecycleTaskServiceImpl implements RecycleTaskService {

    @Autowired
    private RecycleTaskMapper recycleTaskMapper;

    @Autowired
    private NfsUtil nfsUtil;

    @Value("${k8s.nfs}")
    private String nfsIp;

    @Value("${data.server.userName}")
    private String userName;

    @Autowired
    private RecycleConfig recycleConfig;

    @Autowired
    private NfsConfig nfsConfig;


    /**
     * 查询回收任务列表
     *
     * @param recycleTaskQueryDTO 查询任务列表条件
     * @return Map<String, Object> 可回收任务列表
     */
    @Override
    public Map<String, Object> getRecycleTasks(RecycleTaskQueryDTO recycleTaskQueryDTO) {

        //获取当前用户信息
        UserDTO currentUser = JwtUtils.getCurrentUserDto();

        Page page = recycleTaskQueryDTO.toPage();

        LambdaQueryWrapper<RecycleTask> queryWrapper = new LambdaQueryWrapper();
        if (recycleTaskQueryDTO.getRecycleStatus() != null) {
            queryWrapper.eq(RecycleTask::getRecycleStatus, recycleTaskQueryDTO.getRecycleStatus());
        }
        if (recycleTaskQueryDTO.getRecycleType() != null) {
            queryWrapper.eq(RecycleTask::getRecycleType, recycleTaskQueryDTO.getRecycleType());
        }

        if (!Objects.equals(currentUser.getId(), UserConstant.ADMIN_USER_ID)) {
            queryWrapper.eq(RecycleTask::getCreateUserId, currentUser.getId());
        }
        List<RecycleTask> recycleTaskList = recycleTaskMapper.selectPage(page, queryWrapper).getRecords();
        return PageUtil.toPage(page, recycleTaskList);
    }

    /**
     * 创建垃圾回收任务
     *
     * @param recycleTaskCreateDTO 垃圾回收任务信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRecycleTask(RecycleTaskCreateDTO recycleTaskCreateDTO) {

        //获取当前用户信息
        UserDTO currentUser = JwtUtils.getCurrentUserDto();

        //设置默认回收延迟时间
        if (recycleTaskCreateDTO.getRecycleDelayDate() == 0) {
            recycleTaskCreateDTO.setRecycleDelayDate(recycleConfig.getDate());
        }

        //如果是删除文件任务，校验根目录及系统环境
        if (Objects.equals(recycleTaskCreateDTO.getRecycleType(), RecycleTypeEnum.FILE.getCode()) &&
                recycleTaskCreateDTO.getRecycleCondition().startsWith(nfsConfig.getRootDir() + nfsConfig.getBucket())) {
            LogUtil.error(LogEnum.GARBAGE_RECYCLE, "User {} created recycle task failed,file sourcePath :{} invalid", currentUser.getUsername(), recycleTaskCreateDTO.getRecycleCondition());
            throw new BusinessException("创建回收文件任务失败");
        }

        RecycleTask recycleTask = new RecycleTask();
        BeanUtils.copyProperties(recycleTaskCreateDTO, recycleTask);

        //组装回收任务
        recycleTask.setRecycleStatus(RecycleStatusEnum.PENDING.getCode());
        recycleTask.setCreateUserId(Objects.isNull(recycleTaskCreateDTO.getCreateUserId()) ? currentUser.getId() : recycleTaskCreateDTO.getCreateUserId());
        recycleTask.setUpdateUserId(Objects.isNull(recycleTaskCreateDTO.getUpdateUserId()) ? currentUser.getId() : recycleTaskCreateDTO.getCreateUserId());
        recycleTask.setRecycleDelayDate(DateUtil.offsetDay(new Date(), recycleTaskCreateDTO.getRecycleDelayDate()));
        int taskCount = recycleTaskMapper.insert(recycleTask);
        if (taskCount < 1) {
            LogUtil.error(LogEnum.GARBAGE_RECYCLE, "User {} created recycle task, failed to insert data in recycle_task table", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }
    }

    /**
     * 实时删除临时目录完整路径无效文件
     *
     * @param sourcePath 删除路径
     */
    @Override
    public void delTempInvalidResources(String sourcePath) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        if (currentUser.getId() != UserConstant.ADMIN_USER_ID) {
            throw new BusinessException("不支持普通用户操作");
        }
        RecycleTask recycleTask = new RecycleTask();
        recycleTask.setRecycleCondition(sourcePath);
        deleteFileByCMD(recycleTask);
    }

    /**
     * 实时执行回收任务
     *
     * @param taskId 回收任务ID
     */
    @Override
    public void recycleTaskResources(Long taskId) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        //根据taskId查询回收任务
        RecycleTask recycleTask = recycleTaskMapper.selectOne(new LambdaQueryWrapper<RecycleTask>()
                .eq(RecycleTask::getId, taskId)
                .eq(RecycleTask::getRecycleStatus, RecycleStatusEnum.PENDING.getCode()));
        if (recycleTask != null) {
            //只有创建该任务用户或管理员有权限实时执行回收任务
            if (currentUser.getId().equals(recycleTask.getCreateUserId()) || Objects.equals(currentUser.getId(), UserConstant.ADMIN_USER_ID)) {
                //执行回收任务
                deleteFileByCMD(recycleTask);
            } else {
                throw new BusinessException("没有权限操作");
            }
        } else {
            throw new BusinessException("未查询到回收任务");
        }
    }

    /**
     * 获取垃圾回收任务列表
     *
     * @return List<RecycleTask> 垃圾回收任务列表
     */
    @Override
    public List<RecycleTask> getRecycleTaskList() {

        List<RecycleTask> recycleTaskList = recycleTaskMapper.selectList(new LambdaQueryWrapper<RecycleTask>()
                .ne(RecycleTask::getRecycleStatus, RecycleStatusEnum.SUCCEEDED.getCode())
                .le(RecycleTask::getRecycleDelayDate, new Date()));
        return recycleTaskList;
    }

    /**
     * 回收文件资源
     *
     * @param recycleTask 回收任务
     */
    @Override
    public void deleteFileByCMD(RecycleTask recycleTask) {
        String sourcePath = nfsUtil.formatPath(recycleTask.getRecycleCondition());
        //判断该路径是否存在文件或文件夹
        String emptyDir = "";
        if (!nfsUtil.fileOrDirIsEmpty(sourcePath) && sourcePath.startsWith(nfsUtil.formatPath(nfsConfig.getRootDir() + nfsConfig.getBucket()))) {
            try {
                sourcePath = sourcePath.endsWith(StrUtil.SLASH) ? sourcePath : sourcePath + StrUtil.SLASH;
                emptyDir = "/tmp/empty_" + recycleTask.getId() + StrUtil.SLASH;
                LogUtil.info(LogEnum.GARBAGE_RECYCLE, "recycle task sourcePath:{},emptyDir:{}", sourcePath, emptyDir);
                Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.format(RecycleConfig.DEL_COMMAND, userName, nfsIp, emptyDir, emptyDir, sourcePath, emptyDir, sourcePath)});
                //资源回收完毕修改回收表状态
                if (recycleTask.getId() != null) {
                    updateRecycleStatus(recycleTask, recycleSourceIsOk(process));
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.GARBAGE_RECYCLE, "recycle task id:{} Run failed，fail Exception:{}", recycleTask.getId(), e);
            }
        }
    }

    /**
     * 判断执行服务器命名是否成功退出
     *
     * @param process Process对象
     * @return boolean linux命令是否执行成功正常退出
     */
    public boolean recycleSourceIsOk(Process process) {
        InputStreamReader stream = new InputStreamReader(process.getErrorStream());
        BufferedReader reader = new BufferedReader(stream);
        StringBuilder errMessage = new StringBuilder();
        boolean recycleIsOk = true;
        try {
            while (reader.read() != MagicNumConstant.NEGATIVE_ONE) {
                errMessage.append(reader.readLine());
            }
            int status = process.waitFor();
            if (status != 0) {
                recycleIsOk = false;
            }
            LogUtil.info(LogEnum.GARBAGE_RECYCLE, "recycleSourceIsOk is failure,errorMsg:{},processStatus:{}", errMessage.toString(), status);
        } catch (Exception e) {
            LogUtil.error(LogEnum.GARBAGE_RECYCLE, "recycleSourceIsOk is failure: {} ", e);
            recycleIsOk = false;
        } finally {
            IOUtil.close(reader, stream);
        }
        return recycleIsOk;
    }

    /**
     * 修改回收任务状态
     *
     * @param recycleTask 回收任务
     * @param recycleIsOk 是否回收成功
     */
    @Override
    public void updateRecycleStatus(RecycleTask recycleTask, boolean recycleIsOk) {
        //如果回收任务执行成功，则修改回收表回收状态
        if (recycleIsOk) {
            recycleTask.setRecycleStatus(RecycleStatusEnum.SUCCEEDED.getCode())
                    .setUpdateTime(new Timestamp(System.currentTimeMillis()));
            recycleTaskMapper.updateById(recycleTask);
        } else {
            recycleTask.setRecycleStatus(RecycleStatusEnum.FAILED.getCode());
            recycleTaskMapper.updateById(recycleTask);
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
        if (nfsUtil.fileOrDirIsEmpty(sourcePath)) {
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
                                String delRealPath = nfsUtil.formatPath(sourcePath + File.separator + fileName + File.separator + directoryName);
                                delRealPath = delRealPath.endsWith(StrUtil.SLASH) ? delRealPath : delRealPath + StrUtil.SLASH;
                                String emptyDir = "/tmp/tmp_" + directoryName + StrUtil.SLASH;
                                Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.format(RecycleConfig.DEL_COMMAND, userName, nfsIp, emptyDir, emptyDir, delRealPath, emptyDir, delRealPath)});
                                Integer deletStatus = process.waitFor();
                                LogUtil.info(LogEnum.GARBAGE_RECYCLE, "recycle resources path:{},recycle status:{}", delRealPath, deletStatus);
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
