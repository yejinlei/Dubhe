/**
 * Copyright 2019-2020 Zheng Jie
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
 */
package org.dubhe.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.base.DataResponseBody;
import org.dubhe.domain.dto.*;
import org.dubhe.domain.vo.UserVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description : 用户服务 Service
 * @Date 2020-06-01
 */
public interface UserService {

    /**
     * 根据ID查询
     *
     * @param id ID
     * @return /
     */
    UserDTO findById(long id);

    /**
     * 新增用户
     *
     * @param resources /
     * @return /
     */
    UserDTO create(UserCreateDTO resources);

    /**
     * 编辑用户
     *
     * @param resources /
     * @return /
     */
    UserDTO update(UserUpdateDTO resources);

    /**
     * 删除用户
     *
     * @param ids /
     */
    void delete(Set<Long> ids);

    /**
     * 根据用户名查询
     *
     * @param userName /
     * @return /
     */
    UserDTO findByName(String userName);

    /**
     * 修改密码
     *
     * @param username        用户名
     * @param encryptPassword 密码
     */
    void updatePass(String username, String encryptPassword);

    /**
     * 修改头像
     *
     * @param realName 文件名
     * @param path     文件路径
     */
    void updateAvatar(String realName, String path);

    /**
     * 修改邮箱
     *
     * @param username 用户名
     * @param email    邮箱
     */
    void updateEmail(String username, String email);

    /**
     * 查询全部
     *
     * @param criteria 条件
     * @param page     分页参数
     * @return /
     */
    Object queryAll(UserQueryDTO criteria, Page page);

    /**
     * 查询全部不分页
     *
     * @param criteria 条件
     * @return /
     */
    List<UserDTO> queryAll(UserQueryDTO criteria);

    /**
     * 查询用户团队
     *
     * @param userId 条件
     * @return /
     */
    List<TeamDTO> queryTeams(Long userId);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<UserDTO> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 用户自助修改资料
     *
     * @param resources /
     */
    void updateCenter(UserCenterUpdateDTO resources);

    /**
     * 查找用户权限
     *
     * @param id
     * @return
     */
    Set<String> queryPermissionByUserId(Long id);

    /**
     * 用户注册信息
     *
     * @param userRegisterDTO
     * @return
     */
    DataResponseBody userRegister(UserRegisterDTO userRegisterDTO);


    /**
     * 获取code通过发送邮件
     *
     * @param userRegisterMailDTO
     * @return
     */
    DataResponseBody getCodeBySentEmail(UserRegisterMailDTO userRegisterMailDTO);


    /**
     * 修改邮箱
     * @param userEmailUpdateDTO
     * @return
     */
    DataResponseBody resetEmail(UserEmailUpdateDTO userEmailUpdateDTO);

    /**
     * 获取用户信息
     * @return
     */
    Map<String,Object> userinfo();

    /**
     * 密码重置
     *
     * @param userResetPasswordDTO 密码重置请求实体
     * @return
     */
    DataResponseBody resetPassword(UserResetPasswordDTO userResetPasswordDTO);
}
