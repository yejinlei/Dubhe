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
package org.dubhe.rest;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.subject.Subject;
import org.dubhe.base.DataResponseBody;
import org.dubhe.config.Resources;
import org.dubhe.constant.StringConstant;
import org.dubhe.constatnts.UserConstant;
import org.dubhe.domain.dto.*;
import org.dubhe.exception.BaseErrorCode;
import org.dubhe.exception.BusinessException;
import org.dubhe.exception.CaptchaException;
import org.dubhe.exception.LoginException;
import org.dubhe.service.UserService;
import org.dubhe.support.login.UsernamePasswordCaptchaToken;
import org.dubhe.utils.DateUtil;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Description 系统登录 控制器
 * @Date 2020-06-01
 */
@Api(tags = "系统登录")
@RestController
@RequestMapping("/auth")
@Slf4j
@SuppressWarnings("unchecked")
public class LoginController {

    @Value("${rsa.private_key}")
    private String privateKey;

    @Value("${rsa.public_key}")
    private String publicKey;

    @Value("${loginCode.expiration}")
    private Long expiration;

    @Value("${loginCode.codeKey}")
    private String codeKey;

    @Value("${spring.profiles.active}")
    private String profileActive;

    @Autowired
    private DefaultKaptcha kaptcha;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserService userService;


    @ApiOperation("登录")
    @PostMapping(value = "/login")
    public DataResponseBody login(@Validated @RequestBody AuthUserDTO authUserDTO) {
        RSA rsa = new RSA(privateKey, null);
        String password = new String(rsa.decrypt(authUserDTO.getPassword(), KeyType.PrivateKey));
        UsernamePasswordCaptchaToken userToken = new UsernamePasswordCaptchaToken(authUserDTO.getUsername(), password);
        if(!StringConstant.PROFILE_ACTIVE_TEST.equals(profileActive)){
            validateCode(authUserDTO.getCode(), authUserDTO.getUuid());
        }
        userToken.setRememberMe(true);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(userToken);
            UserDTO userDto = JwtUtils.getCurrentUserDto();
            if (BooleanUtil.isFalse(userDto.getEnabled())) {
                throw new BusinessException(BaseErrorCode.SYSTEM_USER_IS_LOCKED.getCode(),
                        BaseErrorCode.SYSTEM_USER_IS_LOCKED.getMsg());
            }
            String token = JwtUtils.sign(authUserDTO.getUsername());
            Set<String> permissions = userService.queryPermissionByUserId(userDto.getId());
            // 返回 token 与 用户信息
            Map<String, Object> authInfo = new HashMap<String, Object>(2) {{
                put("token", token);
                put("user", userDto);
                put("permissions", permissions);
            }};

            return new DataResponseBody(authInfo);
        } catch (BusinessException e) {
            log.error("LoginController login user is locked!");
            throw new BusinessException(e.getResponseBody().getCode(), e.getResponseBody().getMsg());
        } catch (LockedAccountException e) {
            throw new LoginException(Resources.getMessage("ACCOUNT_LOCKED", userToken.getPrincipal()));
        } catch (DisabledAccountException e) {
            throw new LoginException(Resources.getMessage("ACCOUNT_DISABLED", userToken.getPrincipal()));
        } catch (ExpiredCredentialsException e) {
            throw new LoginException(Resources.getMessage("ACCOUNT_EXPIRED", userToken.getPrincipal()));
        } catch (CaptchaException e) {
            throw new LoginException("验证码错误！");
        } catch (AuthenticationException e) {
            log.error("LoginController login error :{} ", e);
            throw new LoginException(BaseErrorCode.SYSTEM_USER_USERNAME_OR_PASSWORD_ERROR.getMsg());
        } catch (Exception e) {
            log.error("LoginController login error :{} ", e);
            String msg = Resources.getMessage("LOGIN_FAIL", e);
            throw new LoginException(msg, e);
        }

    }

    @ApiOperation("获取验证码")
    @GetMapping(value = "/code")
    public DataResponseBody getCode() {
        byte[] verByte = null;
        try {
            ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
            String createText = kaptcha.createText();
            BufferedImage challenge = kaptcha.createImage(createText);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
            String uuid = codeKey + IdUtil.simpleUUID();
            // 保存
            redisUtils.set(uuid, createText, expiration, TimeUnit.MINUTES);
            // 验证码信息
            Map<String, Object> imgResult = new HashMap<String, Object>(2) {{
                put("img", "data:image/png;base64," + DatatypeConverter.printBase64Binary(jpegOutputStream.toByteArray()));
                put("uuid", uuid);
            }};
            return new DataResponseBody(imgResult);
        } catch (IOException e) {
            throw new CaptchaException(e);
        }

    }

    private void validateCode(String loginCaptcha, String uuid) {
        // 验证码未输入
        if (loginCaptcha == null || "".equals(loginCaptcha)) {
            throw new CaptchaException("验证码错误");
        }

        String sessionCaptcha = (String) redisUtils.get(uuid);
        System.out.println("loginCaptcha=" + sessionCaptcha + "," + loginCaptcha);
        if (!loginCaptcha.equals(sessionCaptcha)) {
            throw new CaptchaException("验证码错误");
        }

    }

    @ApiOperation("退出登录")
    @DeleteMapping(value = "/logout")
    public DataResponseBody login() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return new DataResponseBody();
    }

    @ApiOperation("获取用户信息")
    @GetMapping(value = "/info")
    public DataResponseBody info() {
        UserDTO userDto = JwtUtils.getCurrentUserDto();
        Set<String> permissions = userService.queryPermissionByUserId(userDto.getId());
        Map<String, Object> authInfo = new HashMap<String, Object>(2) {{
            put("user", userDto);
            put("permissions", permissions);
        }};
        return new DataResponseBody(authInfo);
    }


    @ApiOperation("用户注册信息")
    @PostMapping(value = "/userRegister")
    public DataResponseBody userRegister(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        return userService.userRegister(userRegisterDTO);
    }


    @ApiOperation("用户忘记密码")
    @PostMapping(value = "/resetPassword")
    public DataResponseBody resetPassword(@Valid @RequestBody UserResetPasswordDTO userResetPasswordDTO) {
        return userService.resetPassword(userResetPasswordDTO);
    }


    @ApiOperation("获取code通过发送邮件")
    @PostMapping(value = "/getCodeBySentEmail")
    public DataResponseBody getCodeBySentEmail(@Valid @RequestBody UserRegisterMailDTO userRegisterMailDTO) {
        return userService.getCodeBySentEmail(userRegisterMailDTO);
    }


    @ApiOperation("获取公钥")
    @GetMapping(value = "/getPublicKey")
    public DataResponseBody getPublicKey() {
        return new DataResponseBody(publicKey);
    }
    
    @ApiOperation(value = "获取用户信息 供第三方平台使用", notes = "获取用户信息 供第三方平台使用")
    @GetMapping("/userinfo")
    public Map<String,Object> userinfo() {
        return userService.userinfo();
    }

    /**
     * 限制登录失败次数
     *
     * @param username
     */
    private boolean limitLoginCount(final String username) {
        String concat = UserConstant.USER_LOGIN_LIMIT_COUNT.concat(username);
        double count = redisUtils.hincr(UserConstant.USER_LOGIN_LIMIT_COUNT.concat(username), concat, 1);
        if (count > UserConstant.COUNT_LOGIN_FAIL) {
            return false;
        } else {
            // 验证码次数凌晨清除
            long afterSixHourTime = DateUtil.getAfterSixHourTime();
            redisUtils.hset(concat, concat, afterSixHourTime);
        }
        return true;
    }


}
