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

package org.dubhe.recycle.utils;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.AesUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.api.impl.ShellFileStoreApiImpl;
import org.dubhe.biz.file.utils.IOUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * @description 资源回收工具类
 * @date 2021-01-21
 */
@Component
public class RecycleTool {

    /**
     * token秘钥
     */
    @Value("${recycle.call.token.secret-key}")
    private String secretKey;
    /**
     * token超时时间
     */
    @Value("${recycle.call.token.expire-seconds}")
    private Integer expireSeconds;

    /**
     * 资源无效文件临时存放目录(默认/tmp/empty_)
     */
    @Value("${recycle.file-tmp-path.recycle:/tmp/empty_}")
    private String recycleFileTmpPath;


    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    @Value("${storage.file-store}")
    private String ip;

    @Value("${data.server.userName}")
    private String userName;

    /**
     * 资源回收授权token
     */
    public static final String RECYCLE_TOKEN = AuthConst.COMMON_TOKEN;
    /**
     * 路径
     */
    public static final String RECYCLE_CALL_PATH = StringConstant.RECYCLE_CALL_URI;
    /**
     * 回收业务
     */
    public static final String BIZ_RECYCLE = "recycle";
    /**
     * 还原业务
     */
    public static final String BIZ_RESTORE = "restore";
    /**
     * 权限匹配地址
     */
    public static final String MATCH_RECYCLE_PATH = RECYCLE_CALL_PATH + "**";


    /**
     * 生成token
     *
     * @return token
     */
    public String generateToken() {
        String expireTime = DateUtil.format(
                DateUtil.offset(new Date(), DateField.SECOND, expireSeconds),
                DatePattern.PURE_DATETIME_PATTERN
        );
        return AesUtil.encrypt(expireTime, secretKey);
    }

    /**
     * 验证token
     *
     * @param token 待校验token
     * @return true token有效 false 无效
     */
    public boolean validateToken(String token) {
        String expireTime = AesUtil.decrypt(token, secretKey);
        if (StringUtils.isEmpty(expireTime)) {
            return false;
        }
        String nowTime = DateUtil.format(
                new Date(),
                DatePattern.PURE_DATETIME_PATTERN
        );
        return expireTime.compareTo(nowTime) > 0;
    }


    /**
     * 获取调用地址
     *
     * @param model   模块代号 RecycleModuleEnum.value
     * @param biz     模块业务
     * @return String 回调地址
     */
    public static String getCallUrl(int model, String biz) {
        return "http://" + RecycleModuleEnum.getServer(model) + RECYCLE_CALL_PATH + biz;
    }

    /**
     * 生成回收说明
     * @param baseNote  基本说明信息
     * @param bizId     业务ID
     * @return string回收说明
     */
    public static String generateRecycleNote(String baseNote, long bizId) {
        return String.format("%s ID:%d", baseNote, bizId);
    }

    /**
     * 生成回收说明
     * @param baseNote  基本说明信息
     * @param bizId     业务ID
     * @return string回收说明
     */
    public static String generateRecycleNote(String baseNote, String name, long bizId) {
        return String.format("%s name:%s ID:%d", baseNote, name, bizId);
    }

    /**
     * 实时删除临时目录完整路径无效文件
     *
     * @param sourcePath 删除路径
     */
    public void delTempInvalidResources(String sourcePath) {
        String resMsg = deleteFileByCMD(sourcePath, RandomUtil.randomString(MagicNumConstant.TWO));
        if (StrUtil.isNotEmpty(resMsg)) {
            throw new BusinessException(ResponseCode.ERROR, resMsg);
        }
    }


    /**
     * 回收天枢一站式平台中的无效文件资源
     * 处理方式：获取到回收任务表中的无效文件路径，通过linux命令进行具体删除
     * 文件路径必须满足格式如：/nfs/当前系统环境/具体删除的文件或文件夹(至少三层目录)
     *
     * @param recycleConditionPath 文件回收绝对路径
     * @param randomPath emptyDir目录补偿位置
     * @return String 回收任务失败返回的失败信息
     */
    private String deleteFileByCMD(String recycleConditionPath, String randomPath) {
        LogUtil.info(LogEnum.GARBAGE_RECYCLE, "RecycleTool deleteFileByCMD  recycleConditionPath:{}, randomPath:{}", recycleConditionPath, randomPath);

        String sourcePath = fileStoreApi.formatPath(recycleConditionPath + File.separator);
        //判断该路径是否存在文件或文件夹
        String nfsBucket = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + File.separator);
        sourcePath = sourcePath.startsWith(nfsBucket) ? sourcePath : fileStoreApi.formatPath(nfsBucket + sourcePath);
        Process process = null;
        try {
            if (sourcePath.length() > nfsBucket.length()) {
                String emptyDir = recycleFileTmpPath + randomPath + StrUtil.SLASH;
                LogUtil.info(LogEnum.GARBAGE_RECYCLE, "recycle task sourcePath:{},emptyDir:{}", sourcePath, emptyDir);
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.format(ShellFileStoreApiImpl.DEL_COMMAND, emptyDir, emptyDir, sourcePath, emptyDir, sourcePath)});
            }
            return processRecycle(process);

        } catch (Exception e) {
            LogUtil.error(LogEnum.GARBAGE_RECYCLE, "文件资源回收失败! Exception:{}", e);
            return "文件资源回收失败! sourcePath:" + sourcePath + " Exception:" + e.getMessage();
        }

    }

    /**
     * 执行服务器命令
     *
     * @param process Process对象
     * @return null 成功执行，其他：异常结束信息
     */
    public String processRecycle(Process process) {
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
}
