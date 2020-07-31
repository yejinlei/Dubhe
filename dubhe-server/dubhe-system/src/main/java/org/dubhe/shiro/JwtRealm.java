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

package org.dubhe.shiro;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.handle.CustomRequestMappingHandlerMapping;
import org.dubhe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @Description : Jwt 规则类
 * @Date 2020-06-01
 */
@Component
public class JwtRealm extends AuthorizingRealm {


  @Autowired
  private  UserService userService;

  @Autowired
  private  CustomRequestMappingHandlerMapping customRequestMappingHandlerMapping;


  /**
   * 限定这个 Realm 只处理我们自定义的 JwtToken
   */
  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof JwtToken;
  }

  /**
   * 此处的 SimpleAuthenticationInfo 可返回任意值，密码校验时不会用到它
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken)
    throws AuthenticationException {
    JwtToken jwtToken = (JwtToken) authcToken;
    if (jwtToken.getPrincipal() == null) {
      throw new AccountException("JWT token参数异常！");
    }
    String userName = jwtToken.getPrincipal().toString();
    try {
      UserDTO user = userService.findByName(userName);
      if (user == null) {
        throw new AccountException("JWT token参数异常！");
      }
      AuthenticationInfo codeAuth = new SimpleAuthenticationInfo(user, jwtToken.getCredentials(), "");
      return codeAuth;
    }catch (Exception e){
      e.printStackTrace();
    }
    return null;
  }


  /**
   * 权限检查
   *
   * @param principals
   * @return
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    UserDTO user = (UserDTO) principals.getPrimaryPrincipal();
    Set<String> permissionsSet = userService.queryPermissionByUserId(user.getId());
    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    info.setStringPermissions(permissionsSet);
    return info;
  }

}
