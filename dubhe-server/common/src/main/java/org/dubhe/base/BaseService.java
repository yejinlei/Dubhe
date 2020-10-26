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
package org.dubhe.base;

import org.dubhe.constant.PermissionConstant;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.Role;
import org.dubhe.exception.BaseErrorCode;
import org.dubhe.exception.BusinessException;
import org.dubhe.utils.JwtUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description 服务层基础数据公共方法类
 * @date 2020-03-27
 */
public class BaseService {

    private BaseService (){}

    /**
     * 校验是否具有管理员权限
     */
    public static void checkAdminPermission() {
        if(!isAdmin()){
            throw new BusinessException(BaseErrorCode.DATASET_ADMIN_PERMISSION_ERROR);
        }
    }

    /**
     * 校验是否是管理管理员
     *
     * @return 校验标识
     */
    public static Boolean isAdmin() {
        UserDTO currentUserDto = JwtUtils.getCurrentUserDto();
        if (currentUserDto != null && !CollectionUtils.isEmpty(currentUserDto.getRoles())) {
            List<Role> roles = currentUserDto.getRoles();
            List<Role> roleList = roles.stream().
                    filter(a -> a.getId().compareTo(PermissionConstant.ADMIN_USER_ID) == 0)
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(roleList)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 清除本地线程数据权限数据
     */
    public static void removeContext(){
        if( !Objects.isNull(DataContext.get())){
            DataContext.remove();
        }
    }

}
