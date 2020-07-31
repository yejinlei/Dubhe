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
package org.dubhe.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.base.DataResponseBody;
import org.dubhe.constatnts.UserConstant;
import org.dubhe.dao.*;
import org.dubhe.domain.dto.*;
import org.dubhe.domain.entity.Role;
import org.dubhe.domain.entity.User;
import org.dubhe.domain.entity.UserAvatar;
import org.dubhe.domain.entity.UserRole;
import org.dubhe.domain.vo.EmailVo;
import org.dubhe.domain.vo.UserVO;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.SwitchEnum;
import org.dubhe.enums.UserMailCodeEnum;
import org.dubhe.event.EmailEventPublisher;
import org.dubhe.exception.BaseErrorCode;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.UserService;
import org.dubhe.service.convert.TeamConvert;
import org.dubhe.service.convert.UserConvert;
import org.dubhe.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.dubhe.utils.DateUtil.getSecondTime;

/**
 * @description :用户服务 实现类
 * @date 2020-06-01
 */
@Service
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {

    @Value("${rsa.private_key}")
    private String privateKey;

    @Value("${initial_password}")
    private String initialPassword;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private UserConvert userConvert;

    @Autowired
    private TeamConvert teamConvert;

    @Autowired
    private UserAvatarMapper userAvatarMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private EmailEventPublisher publisher;

    private final String LOCK_SEND_CODE = "LOCK_SEND_CODE";

    /**
     * 分页查询用户列表
     *
     * @param criteria 查询条件
     * @param page     分页请求实体
     * @return java.lang.Object 用户列表返回实例
     */
    @Override
    @Cacheable
    public Object queryAll(UserQueryDTO criteria, Page page) {
        if (criteria.getRoleId() == null) {
            IPage<User> users = userMapper.selectCollPage(page, WrapperHelp.getWrapper(criteria));
            return PageUtil.toPage(users, userConvert::toDto);
        } else {
            IPage<User> users = userMapper.selectCollPageByRoleId(page, WrapperHelp.getWrapper(criteria), criteria.getRoleId());
            return PageUtil.toPage(users, userConvert::toDto);
        }
    }

    /**
     * 查询用户列表
     *
     * @param criteria 用户查询条件
     * @return java.util.List<org.dubhe.domain.dto.UserDTO> 用户列表返回实例
     */
    @Override
    @Cacheable
    public List<UserDTO> queryAll(UserQueryDTO criteria) {
        List<User> users = userMapper.selectCollList(WrapperHelp.getWrapper(criteria));
        return userConvert.toDto(users);
    }

    /**
     * 根据用户ID查询团队列表
     *
     * @param userId 用户ID
     * @return java.util.List<org.dubhe.domain.dto.TeamDTO> 团队列表信息
     */
    @Override
    @Cacheable
    public List<TeamDTO> queryTeams(Long userId) {

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, userId)
                        .eq(User::getDeleted, SwitchEnum.getBooleanValue(SwitchEnum.OFF.getValue()))
        );
        List teamList = teamMapper.findByUserId(user.getId());
        return teamConvert.toDto(teamList);
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id id
     * @return org.dubhe.domain.dto.UserDTO 用户信息返回实例
     */
    @Override
    @Cacheable(key = "#p0")
    public UserDTO findById(long id) {
        User user = userMapper.selectCollById(id);
        return userConvert.toDto(user);
    }


    /**
     * 新增用户
     *
     * @param resources 用户新增实体
     * @return org.dubhe.domain.dto.UserDTO 用户信息返回实例
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public UserDTO create(UserCreateDTO resources) {
        if (!Objects.isNull(userMapper.findByUsername(resources.getUsername()))) {
            throw new BusinessException("用户名已存在");
        }
        if (userMapper.findByEmail(resources.getEmail()) != null) {
            throw new BusinessException("邮箱已存在");
        }
        resources.setPassword(passwordEncoder.encode(initialPassword));

        User user = User.builder().build();
        BeanUtils.copyProperties(resources, user);

        userMapper.insert(user);
        for (Role role : resources.getRoles()) {
            roleMapper.tiedUserRole(user.getId(), role.getId());
        }

        return userConvert.toDto(user);
    }


    /**
     * 修改用户
     *
     * @param resources 用户修改请求实例
     * @return org.dubhe.domain.dto.UserDTO 用户信息返回实例
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public UserDTO update(UserUpdateDTO resources) {

        //修改管理员信息校验
        checkIsAdmin(resources.getId());

        User user = userMapper.selectCollById(resources.getId());
        User userTmp = userMapper.findByUsername(resources.getUsername());
        if (userTmp != null && !user.equals(userTmp)) {
            throw new BusinessException("用户名已存在");
        }
        userTmp = userMapper.findByEmail(resources.getEmail());
        if (userTmp != null && !user.equals(userTmp)) {
            throw new BusinessException("邮箱已存在");
        }
        roleMapper.untiedUserRoleByUserId(user.getId());
        for (Role role : resources.getRoles()) {
            roleMapper.tiedUserRole(user.getId(), role.getId());
        }
        user.setUsername(resources.getUsername());
        user.setEmail(resources.getEmail());
        user.setEnabled(resources.getEnabled());
        user.setRoles(resources.getRoles());
        user.setPhone(resources.getPhone());
        user.setNickName(resources.getNickName());
        user.setRemark(resources.getRemark());
        user.setSex(resources.getSex());
        userMapper.updateById(user);
        return userConvert.toDto(user);
    }


    /**
     * 批量删除用户信息
     *
     * @param ids 用户ID列表
     * @return void
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            Long adminId = Long.valueOf(UserConstant.ADMIN_USER_ID);
            if (ids.contains(adminId)) {
                throw new BusinessException(BaseErrorCode.SYSTEM_USER_CANNOT_DELETE);
            }
            ids.forEach(id -> {
                userMapper.updateById(
                        User.builder()
                                .id(id)
                                .deleted(SwitchEnum.getBooleanValue(SwitchEnum.ON.getValue()))
                                .build());
            });
        }
    }


    /**
     * 根据用户名称获取用户信息
     *
     * @param userName 用户名称
     * @return org.dubhe.domain.dto.UserDTO 用户信息返回实例
     */
    @Override
    @Cacheable(key = "'loadUserByUsername:'+#p0")
    public UserDTO findByName(String userName) {
        User user = userMapper.findByUsername(userName);
        if (user == null) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl findByName user is null");
            throw new BusinessException("user not found");
        } else {
            return userConvert.toDto(user);
        }
    }


    /**
     * 修改用户个人中心信息
     *
     * @param resources 个人用户信息修改请求实例
     * @return void
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateCenter(UserCenterUpdateDTO resources) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, resources.getId())
                        .eq(User::getDeleted, SwitchEnum.getBooleanValue(SwitchEnum.OFF.getValue()))
        );
        user.setNickName(resources.getNickName());
        user.setRemark(resources.getRemark());
        user.setPhone(resources.getPhone());
        user.setSex(resources.getSex());
        userMapper.updateById(user);
        if (user.getUserAvatar() != null) {
            if (user.getUserAvatar().getId() != null) {
                userAvatarMapper.updateById(user.getUserAvatar());
            } else {
                userAvatarMapper.insert(user.getUserAvatar());
            }
        }
    }


    /**
     * 修改用户密码
     *
     * @param username 账号
     * @param pass     密码
     * @return void
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updatePass(String username, String pass) {
        userMapper.updatePass(username, pass, new Date());
    }


    /**
     * 修改用户头像
     *
     * @param realName 名称
     * @param path     头像路径
     * @return void
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateAvatar(String realName, String path) {
        User user = userMapper.findByUsername(JwtUtils.getCurrentUserDto().getUsername());
        UserAvatar userAvatar = user.getUserAvatar();
        UserAvatar newAvatar = new UserAvatar(userAvatar, realName, path, null);

        if (newAvatar.getId() != null) {
            userAvatarMapper.updateById(newAvatar);
        } else {
            userAvatarMapper.insert(newAvatar);
        }
        user.setAvatarId(newAvatar.getId());
        user.setUserAvatar(newAvatar);
        userMapper.updateById(user);
    }

    /**
     * 修改用户邮箱
     *
     * @param username 用户名称
     * @param email    用户邮箱
     * @return void
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String username, String email) {
        userMapper.updateEmail(username, email);
    }


    /**
     * 用户信息导出
     *
     * @param queryAll 用户信息列表
     * @param response
     * @return void
     */
    @Override
    public void download(List<UserDTO> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserDTO userDTO : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", userDTO.getUsername());
            map.put("邮箱", userDTO.getEmail());
            map.put("状态", userDTO.getEnabled() ? "启用" : "禁用");
            map.put("手机号码", userDTO.getPhone());
            map.put("最后修改密码的时间", userDTO.getLastPasswordResetTime());
            map.put("创建日期", userDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    @Cacheable(key = "#p0")
    /**
     * 查询用户ID权限
     *
     * @param id 用户ID
     * @return java.util.Set<java.lang.String> 权限列表
     */
    public Set<String> queryPermissionByUserId(Long id) {
        return userMapper.queryPermissionByUserId(id);
    }

    /**
     * 用户注册信息
     *
     * @param userRegisterDTO 用户注册请求实体
     * @return org.dubhe.base.DataResponseBody 注册返回结果集
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataResponseBody userRegister(UserRegisterDTO userRegisterDTO) {

        //用户信息校验
        checkoutUserInfo(userRegisterDTO);
        String encode = passwordEncoder.encode(RsaEncrypt.decrypt(userRegisterDTO.getPassword(), privateKey));
        try {
            User newUser = User.builder()
                    .email(userRegisterDTO.getEmail())
                    .enabled(true)
                    .nickName(userRegisterDTO.getNickName())
                    .password(encode)
                    .phone(userRegisterDTO.getPhone())
                    .sex(SwitchEnum.ON.getValue().compareTo(userRegisterDTO.getSex()) == 0 ? UserConstant.SEX_MEN : UserConstant.SEX_WOMEN)
                    .username(userRegisterDTO.getUsername()).build();

            //新增用户注册信息
            userMapper.insert(newUser);

            //绑定用户默认权限
            userRoleMapper.insert(UserRole.builder().roleId((long) UserConstant.REGISTER_ROLE_ID).userId(newUser.getId()).build());

        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl userRegister error , param:{} error:{}", JSONObject.toJSONString(userRegisterDTO), e);
            throw new BusinessException(BaseErrorCode.ERROR_SYSTEM.getCode(), BaseErrorCode.ERROR_SYSTEM.getMsg());
        }

        return new DataResponseBody();
    }


    /**
     * 获取code通过发送邮件
     *
     * @param userRegisterMailDTO 用户发送邮件请求实体
     * @return org.dubhe.base.DataResponseBody 发送邮件返回结果集
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataResponseBody getCodeBySentEmail(UserRegisterMailDTO userRegisterMailDTO) {
        String email = userRegisterMailDTO.getEmail();

        User dbUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getDeleted, SwitchEnum.getBooleanValue(SwitchEnum.OFF.getValue()))
        );
        //校验用户是否注册（type : 1 用户注册 2 修改邮箱 ）
        Boolean isRegisterOrUpdate = UserMailCodeEnum.REGISTER_CODE.getValue().compareTo(userRegisterMailDTO.getType()) == 0 ||
                UserMailCodeEnum.MAIL_UPDATE_CODE.getValue().compareTo(userRegisterMailDTO.getType()) == 0;
        if (!Objects.isNull(dbUser) && isRegisterOrUpdate) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl dbUser already register , dbUser:{} ", JSONObject.toJSONString(dbUser));
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_EMAIL_ALREADY_EXISTS.getCode(),
                    BaseErrorCode.SYSTEM_USER_EMAIL_ALREADY_EXISTS.getMsg());
        }


        //限制邮箱发送次数
        limitSendEmail(email);

        try {
            synchronized (LOCK_SEND_CODE) {
                //产生随机的验证码
                String code = RandomUtil.randomCode();
                //异步发送邮件
                publisher.sentEmailEvent(
                        EmailDTO.builder()
                                .code(code)
                                .subject(UserMailCodeEnum.getEnumValue(userRegisterMailDTO.getType()).getDesc())
                                .type(userRegisterMailDTO.getType())
                                .receiverMailAddress(email).build());
                //redis存储邮箱验证信息
                redisUtils.hset(
                        getSendEmailCodeRedisKeyByType(userRegisterMailDTO.getType()).concat(email),
                        email,
                        EmailVo.builder().code(code).email(email).build(),
                        UserConstant.DATE_SECOND);
            }

        } catch (Exception e) {
            redisUtils.hdel(UserConstant.USER_EMAIL_REGISTER.concat(email), email);
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl getCodeBySentEmail error , param:{} error:{}", email, e);
            throw new BusinessException(BaseErrorCode.ERROR_SYSTEM.getCode(), BaseErrorCode.ERROR_SYSTEM.getMsg());
        }
        return new DataResponseBody();
    }


    /**
     * 邮箱修改
     *
     * @param userEmailUpdateDTO 修改邮箱请求实体
     * @return org.dubhe.base.DataResponseBody 修改邮箱返回结果集
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataResponseBody resetEmail(UserEmailUpdateDTO userEmailUpdateDTO) {
        //校验邮箱信息
        User dbUser = checkoutEmailInfoByReset(userEmailUpdateDTO);

        try {
            //修改邮箱信息
            userMapper.updateById(
                    User.builder()
                            .id(dbUser.getId())
                            .email(userEmailUpdateDTO.getEmail()).build());
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl update email error , email:{} error:{}", userEmailUpdateDTO.getEmail(), e);
            throw new BusinessException(BaseErrorCode.ERROR_SYSTEM.getCode(),
                    BaseErrorCode.ERROR_SYSTEM.getMsg());
        }

        return new DataResponseBody();
    }


    /**
     * 获取用户信息
     *
     * @param
     * @return java.util.Map<java.lang.String, java.lang.Object> 用户信息结果集
     */
    @Override
    public Map<String, Object> userinfo() {
        UserDTO userDto = JwtUtils.getCurrentUserDto();
        if (Objects.isNull(userDto)) {
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_IS_NOT_EXISTS.getCode()
                    , BaseErrorCode.SYSTEM_USER_IS_NOT_EXISTS.getMsg());
        }

        //查询用户是否是管理员
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userDto.getId())
                        .eq(UserRole::getRoleId, Long.parseLong(String.valueOf(UserConstant.ADMIN_ROLE_ID)))
        );
        UserVO vo = UserVO.builder()
                .email(userDto.getEmail())
                .password(Md5Util.createMd5(Md5Util.createMd5(userDto.getUsername()).concat(initialPassword)))
                .username(userDto.getUsername())
                .is_staff(!CollectionUtils.isEmpty(userRoles) ? true : false).build();

        return BeanMap.create(vo);
    }


    /**
     * 密码重置接口
     *
     * @param userResetPasswordDTO 密码修改请求参数
     * @return org.dubhe.base.DataResponseBody 密码修改结果集
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataResponseBody resetPassword(UserResetPasswordDTO userResetPasswordDTO) {

        //校验 邮箱地址 和 验证码
        checkoutEmailAndCode(userResetPasswordDTO.getCode(), userResetPasswordDTO.getEmail(), UserConstant.USER_EMAIL_RESET_PASSWORD);

        User dbUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, userResetPasswordDTO.getEmail())
                .eq(User::getDeleted, SwitchEnum.getBooleanValue(SwitchEnum.OFF.getValue()))
        );
        if (Objects.isNull(dbUser)) {
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_EMAIL_NOT_EXISTS.getCode()
                    , BaseErrorCode.SYSTEM_USER_EMAIL_NOT_EXISTS.getMsg());
        }

        //加密密码
        String encode = passwordEncoder.encode(RsaEncrypt.decrypt(userResetPasswordDTO.getPassword(), privateKey));
        try {
            userMapper.updateById(User.builder().id(dbUser.getId()).password(encode).build());
        } catch (Exception e) {
            throw new BusinessException(BaseErrorCode.ERROR_SYSTEM.getCode()
                    , BaseErrorCode.ERROR_SYSTEM.getMsg());
        }
        return new DataResponseBody();
    }

    /**
     * 修改邮箱校验邮箱信息
     *
     * @param userEmailUpdateDTO 邮箱修改参数校验实体
     */
    private User checkoutEmailInfoByReset(UserEmailUpdateDTO userEmailUpdateDTO) {
        String email = userEmailUpdateDTO.getEmail();
        //管理员信息校验
        checkIsAdmin(userEmailUpdateDTO.getUserId());

        //校验用户信息是否存在
        User dbUser = userMapper.selectCollById(userEmailUpdateDTO.getUserId());
        if (ObjectUtil.isNull(dbUser)) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl dbUser is null , userId:{}", userEmailUpdateDTO.getUserId());
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_IS_NOT_EXISTS.getCode(),
                    BaseErrorCode.SYSTEM_USER_IS_NOT_EXISTS.getMsg());
        }
        //校验密码是否正确
        String decryptPassword = RsaEncrypt.decrypt(userEmailUpdateDTO.getPassword(), privateKey);
        if (!passwordEncoder.matches(decryptPassword, dbUser.getPassword())) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl password error , webPassword:{}, dbPassword:{} ",
                    userEmailUpdateDTO.getPassword(), dbUser.getPassword());
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_EMAIL_PASSWORD_ERROR.getCode(),
                    BaseErrorCode.SYSTEM_USER_EMAIL_PASSWORD_ERROR.getMsg());
        }

        //邮箱唯一性校验
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, userEmailUpdateDTO.getEmail()));
        if (!ObjectUtil.isNull(user)) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl Email already exists , email:{} ", userEmailUpdateDTO.getEmail());
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_EMAIL_ALREADY_EXISTS.getCode(),
                    BaseErrorCode.SYSTEM_USER_EMAIL_ALREADY_EXISTS.getMsg());
        }

        //校验 邮箱地址 和 验证码
        checkoutEmailAndCode(userEmailUpdateDTO.getCode(), email, UserConstant.USER_EMAIL_UPDATE);

        return dbUser;
    }


    /**
     * 限制发送次数
     *
     * @param receiverMailAddress 邮箱接受者地址
     */
    private void limitSendEmail(final String receiverMailAddress) {
        double count = redisUtils.hincr(UserConstant.USER_EMAIL_LIMIT_COUNT.concat(receiverMailAddress), receiverMailAddress, 1);
        if (count > UserConstant.COUNT_SENT_EMAIL) {
            LogUtil.error(LogEnum.SYS_ERR, "Email verification code cannot exceed three times , error:{}", UserConstant.COUNT_SENT_EMAIL);
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_EMAIL_CODE_CANNOT_EXCEED_TIMES.getCode(),
                    BaseErrorCode.SYSTEM_USER_EMAIL_CODE_CANNOT_EXCEED_TIMES.getMsg());
        } else {
            // 验证码次数凌晨清除
            String concat = UserConstant.USER_EMAIL_LIMIT_COUNT.concat(receiverMailAddress);

            Long secondsNextEarlyMorning = getSecondTime();

            redisUtils.expire(concat, secondsNextEarlyMorning);
        }
    }


    /**
     * 用户信息校验
     *
     * @param userRegisterDTO 用户信息校验实体
     * @return
     */
    private void checkoutUserInfo(UserRegisterDTO userRegisterDTO) {
        //账户唯一性校验
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userRegisterDTO.getUsername()));
        if (!ObjectUtil.isNull(user)) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl username already exists , username:{} ", userRegisterDTO.getUsername());
            throw new BusinessException(BaseErrorCode.SYSTEM_USERNAME_ALREADY_EXISTS.getCode(),
                    BaseErrorCode.SYSTEM_USERNAME_ALREADY_EXISTS.getMsg());
        }
        //校验 邮箱地址 和 验证码
        checkoutEmailAndCode(userRegisterDTO.getCode(), userRegisterDTO.getEmail(), UserConstant.USER_EMAIL_REGISTER);
    }

    /**
     * 校验 邮箱地址 和 验证码
     *
     * @param code 验证码
     * @param email 邮箱
     * @param codeRedisKey redis-key
     */
    private void checkoutEmailAndCode(String code, String email, String codeRedisKey) {
        //校验验证码是否过期
        Object emailVoObj = redisUtils.hget(codeRedisKey.concat(email), email);
        if (Objects.isNull(emailVoObj)) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl emailVo already expired , email:{} ", email);
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_REGISTER_EMAIL_INFO_EXPIRED.getCode(),
                    BaseErrorCode.SYSTEM_USER_REGISTER_EMAIL_INFO_EXPIRED.getMsg());
        }

        //校验邮箱和验证码
        EmailVo emailVo = (EmailVo) emailVoObj;
        if (!email.equals(emailVo.getEmail()) || !code.equals(emailVo.getCode())) {
            LogUtil.error(LogEnum.SYS_ERR, "UserServiceImpl email or code error , email:{} code:{}", email, code);
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_EMAIL_OR_CODE_ERROR.getCode(),
                    BaseErrorCode.SYSTEM_USER_EMAIL_OR_CODE_ERROR.getMsg());
        }
    }


    /**
     * 获取 发送邮箱code 的 redis key
     *
     * @param type 发送邮件类型
     * @return
     */
    private String getSendEmailCodeRedisKeyByType(Integer type) {
        String typeKey = null;
        if (UserMailCodeEnum.REGISTER_CODE.getValue().compareTo(type) == 0) {
            typeKey = UserConstant.USER_EMAIL_REGISTER;
        } else if (UserMailCodeEnum.MAIL_UPDATE_CODE.getValue().compareTo(type) == 0) {
            typeKey = UserConstant.USER_EMAIL_UPDATE;
        } else if (UserMailCodeEnum.FORGET_PASSWORD.getValue().compareTo(type) == 0) {
            typeKey = UserConstant.USER_EMAIL_RESET_PASSWORD;
        } else {
            typeKey = UserConstant.USER_EMAIL_OTHER;
        }
        return typeKey;
    }


    /**
     * 修改管理员信息校验
     *
     * @param userId 用户ID
     */
    private void checkIsAdmin(Long userId) {
        //修改管理员信息校验
        if (UserConstant.ADMIN_USER_ID == userId.intValue() &&
                UserConstant.ADMIN_USER_ID != JwtUtils.getCurrentUserDto().getId().intValue()) {
            throw new BusinessException(BaseErrorCode.SYSTEM_USER_CANNOT_UPDATE_ADMIN);
        }
    }

}
