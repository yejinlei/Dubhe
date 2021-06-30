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
package org.dubhe.admin.rest;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.service.DictService;
import org.dubhe.admin.service.MenuService;
import org.dubhe.admin.service.RoleService;
import org.dubhe.admin.service.UserService;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.cloud.authconfig.factory.PasswordEncoderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @description 个人中心 控制器
 * @date 2020-06-01
 */
@Api(tags = "系统：个人中心")
@RestController
@RequestMapping("/user")
public class UserCenterController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private DictService dictService;

    @Autowired
    private UserContextService userContextService;

    @Value("${rsa.private_key}")
    private String privateKey;


    @ApiOperation("用户团队")
    @GetMapping(value = "/teams")
    public DataResponseBody getTeams() {
        return new DataResponseBody(userService.queryTeams(userContextService.getCurUserId()));
    }

    @ApiOperation("获取前端所需菜单")
    @GetMapping(value = "/menus")
    public DataResponseBody menus() {
        Long curUserId = userContextService.getCurUserId();
        List<RoleSmallDTO> roles = roleService.getRoleByUserId(curUserId);
        List<MenuDTO> menuDtoList = menuService.findByRoles(roles);
        List<MenuDTO> menuDtos = (List<MenuDTO>) menuService.buildTree(menuDtoList).get("result");

        return new DataResponseBody(menuService.buildMenus(menuDtos));
    }

    @ApiOperation("用户查询字典详情")
    @GetMapping(value = "/dict/{name}")
    public DataResponseBody getDict(@PathVariable String name) {
        return new DataResponseBody(dictService.findByName(name));
    }

    @ApiOperation("个人中心")
    @GetMapping(value = "info")
    public DataResponseBody getInfo() {
        return new DataResponseBody(userContextService.getCurUser());
    }

    @ApiOperation("修改用户：个人中心")
    @PutMapping(value = "info")
    public DataResponseBody info(@Valid @RequestBody UserCenterUpdateDTO resources) {
        Long curUserId = userContextService.getCurUserId();
        if (!resources.getId().equals(curUserId)) {
            throw new BusinessException("不能修改他人资料");
        }
        userService.updateCenter(resources);
        return new DataResponseBody(userService.findById(curUserId));
    }

    @ApiOperation("修改密码")
    @PostMapping(value = "/updatePass")
    public DataResponseBody updatePass(@Valid @RequestBody UserPassUpdateDTO passUpdateDTO) {
        PasswordEncoder passwordEncoder = PasswordEncoderFactory.getPasswordEncoder();
        // 密码解密
        RSA rsa = new RSA(privateKey, null);
        String oldPass = new String(rsa.decrypt(passUpdateDTO.getOldPass(), KeyType.PrivateKey));
        String newPass = new String(rsa.decrypt(passUpdateDTO.getNewPass(), KeyType.PrivateKey));
        UserContext curUser = userContextService.getCurUser();
        if (!passwordEncoder.matches(oldPass, curUser.getPassword())) {
            throw new BusinessException("修改失败，旧密码错误");
        }
        if (passwordEncoder.matches(newPass, curUser.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        userService.updatePass(curUser.getUsername(), passwordEncoder.encode(newPass));
        return new DataResponseBody();
    }

    @ApiOperation("修改头像")
    @PostMapping(value = "/updateAvatar")
    public DataResponseBody updateAvatar(@Valid @RequestBody UserAvatarUpdateDTO avatarUpdateDTO) {
        userService.updateAvatar(avatarUpdateDTO.getRealName(), avatarUpdateDTO.getPath());
        return new DataResponseBody();
    }


    @ApiOperation("修改邮箱接口")
    @PostMapping(value = "/resetEmail")
    public DataResponseBody resetEmail(@RequestBody UserEmailUpdateDTO userEmailUpdateDTO) {
        return userService.resetEmail(userEmailUpdateDTO);
    }
}
