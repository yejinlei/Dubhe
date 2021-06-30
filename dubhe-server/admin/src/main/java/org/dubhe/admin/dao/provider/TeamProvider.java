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
 * @description  团队构建类
 * @date 2020-04-15
 */
public class TeamProvider {

  public String findByUserId(Long userId) {
    StringBuffer sql = new StringBuffer("select t.* from team t, teams_users_roles tur ");
    sql.append(" where tur.user_id=#{userId} ");
    sql.append(" and tur.team_id=t.id ");
    return sql.toString();
  }
}
