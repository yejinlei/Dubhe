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

package org.dubhe.admin.dao.provider;

/**
 * @description 用户sql构建类
 * @date 2020-04-02
 */
public class UserProvider {
    public String queryPermissionByUserId(Long userId) {
        StringBuffer sql = new StringBuffer("select m.permission from menu m, users_roles ur, roles_menus rm ");
        sql.append(" where ur.user_id = #{userId} and ur.role_id = rm.role_id and rm.menu_id = m.id and m.permission <> '' and m.deleted = 0 ");
        return sql.toString();
    }

    public String findPermissionByUserIdAndTeamId(Long userId, Long teamId) {
        StringBuffer sql = new StringBuffer("select m.permission from menu m, teams_users_roles tur ,roles_menus rm ");
        sql.append(" where tur.user_id=#{userId} ");
        sql.append(" and tur.role_id=rm.role_id ");
        sql.append(" and tur.team_id=#{team_id} ");
        sql.append(" and rm.menu_id=m.id");
        sql.append(" and  and m.deleted = 0 ");
        return sql.toString();
    }

    public String findByTeamId(Long teamId) {
        StringBuffer sql = new StringBuffer("select u.* from user u,teams_users_roles tur where tur.team_id=#{teamId} and tur.user_id=u.id  and u.deleted = 0");
        return sql.toString();
    }
}
