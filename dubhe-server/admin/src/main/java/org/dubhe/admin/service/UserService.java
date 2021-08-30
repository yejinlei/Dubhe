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
package org.dubhe.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.domain.entity.User;
import org.dubhe.admin.domain.vo.UserConfigCreateVO;
import org.dubhe.admin.domain.vo.UserConfigVO;
import org.dubhe.biz.base.dto.TeamDTO;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.cloud.authconfig.service.AdminUserService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description Demo服务接口
 * @date 2020-11-26
 */
public interface UserService extends AdminUserService, IService<User> {


    /**
     * 根据ID获取用户信息
     *
     * @param id 用户Id
     * @return org.dubhe.domain.dto.UserDTO 用户信息返回实例
     */
    UserDTO findById(long id);

    /**
     * 新增用户
     *
     * @param resources 用户新增实体
     * @return org.dubhe.domain.dto.UserDTO 用户信息返回实例
     */
    UserDTO create(UserCreateDTO resources);

    /**
     * 修改用户
     *
     * @param resources 用户修改请求实例
     * @return org.dubhe.domain.dto.UserDTO 用户信息返回实例
     */
    UserDTO update(UserUpdateDTO resources);

    /**
     * 批量删除用户信息
     *
     * @param ids 用户ID列表
     */
    void delete(Set<Long> ids);

    /**
     * 根据用户名称获取用户信息
     *
     * @param userName 用户名称
     * @return org.dubhe.domain.dto.UserDTO 用户信息返回实例
     */
    UserDTO findByName(String userName);

    /**
     * 修改用户密码
     *
     * @param username            账号
     * @param encryptPassword     密码
     * @return void
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
     * 分页查询用户列表
     *
     * @param criteria 查询条件
     * @param page     分页请求实体
     * @return java.lang.Object 用户列表返回实例
     */
    Object queryAll(UserQueryDTO criteria, Page page);

    /**
     * 查询用户列表
     *
     * @param criteria 用户查询条件
     * @return java.util.List<org.dubhe.domain.dto.UserDTO> 用户列表返回实例
     */
    List<UserDTO> queryAll(UserQueryDTO criteria);

    /**
     * 根据用户ID查询团队列表
     *
     * @param userId 用户ID
     * @return java.util.List<org.dubhe.domain.dto.TeamDTO> 团队列表信息
     */
    List<TeamDTO> queryTeams(Long userId);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response 导出http响应
     * @throws IOException 导出异常
     */
    void download(List<UserDTO> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 修改用户个人中心信息
     *
     * @param resources 个人用户信息修改请求实例
     */
    void updateCenter(UserCenterUpdateDTO resources);

    /**
     * 查询用户ID权限
     *
     * @param id 用户ID
     * @return java.util.Set<java.lang.String> 权限列表
     */
    Set<String> queryPermissionByUserId(Long id);

    /**
     * 用户注册信息
     *
     * @param userRegisterDTO 用户注册请求实体
     * @return org.dubhe.base.DataResponseBody 注册返回结果集
     */
    DataResponseBody userRegister(UserRegisterDTO userRegisterDTO);


    /**
     * 获取code通过发送邮件
     *
     * @param userRegisterMailDTO 用户发送邮件请求实体
     * @return org.dubhe.base.DataResponseBody 发送邮件返回结果集
     */
    DataResponseBody getCodeBySentEmail(UserRegisterMailDTO userRegisterMailDTO);


    /**
     * 邮箱修改
     *
     * @param userEmailUpdateDTO 修改邮箱请求实体
     * @return org.dubhe.base.DataResponseBody 修改邮箱返回结果集
     */
    DataResponseBody resetEmail(UserEmailUpdateDTO userEmailUpdateDTO);

    /**
     * 获取用户信息
     *
     * @return java.util.Map<java.lang.String, java.lang.Object> 用户信息结果集
     */
    Map<String, Object> userinfo();

    /**
     * 密码重置接口
     *
     * @param userResetPasswordDTO 密码修改请求参数
     * @return org.dubhe.base.DataResponseBody 密码修改结果集
     */
    DataResponseBody resetPassword(UserResetPasswordDTO userResetPasswordDTO);

    /**
     * 登录
     *
     * @param authUserDTO 登录请求实体
     */
    DataResponseBody<Map<String, Object>> login(AuthUserDTO authUserDTO);

    /**
     * 退出登录
     *
     * @param accessToken token
     */
    DataResponseBody logout(String accessToken);

    /**
     * 根据用户昵称获取用户信息
     *
     * @param nickName 用户昵称
     * @return org.dubhe.domain.dto.UserDTO 用户信息DTO集合
     */
    List<UserDTO> findByNickName(String nickName);

    /**
     * 根据用户id批量查询用户信息
     *
     * @param ids 用户id集合
     * @return org.dubhe.domain.dto.UserDTO 用户信息DTO集合
     */
    List<UserDTO> getUserList(List<Long> ids);

    /**
     * 根据用户 ID 查询用户配置
     *
     * @param userId 用户 ID
     * @return org.dubhe.admin.domain.vo.UserConfigVO 用户配置 VO
     */
    UserConfigVO findUserConfig(Long userId);

    /**
     * 创建或更新用户配置
     *
     * @param userConfigDTO 用户配置
     * @return org.dubhe.admin.domain.vo.UserConfigCreateVO 用户配置 VO
     */
    UserConfigCreateVO createOrUpdateUserConfig(UserConfigDTO userConfigDTO);
}
