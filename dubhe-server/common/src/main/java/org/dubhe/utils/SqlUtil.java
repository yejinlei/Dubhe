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

package org.dubhe.utils;

import org.dubhe.base.BaseService;
import org.dubhe.base.DataContext;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @description sql语句转换的工具类
 * @date 2020-07-06
 */

public class SqlUtil {

	/**
	 * 将数组转换成in('1','2')的形式
	 *
	 * @param objs
	 * @return
	 */
	public static String integerlistToString(Integer[] objs) {

		if (objs != null && objs.length > 0) {
			StringBuilder sb = new StringBuilder("(");
			for (Object obj : objs) {
				if (obj != null) {
					sb.append("'" + obj.toString() + "',");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			return sb.toString();
		}
		return "";
	}


	/**
	 * 获取资源拥有着ID
	 *
	 * @return 资源拥有者id集合
	 */
	public static Set<Long> getResourceIds() {
		if (!Objects.isNull(DataContext.get())) {
			return DataContext.get().getResourceUserIds();
		}
		Set<Long> ids = new HashSet<>();
		Long id = JwtUtils.getCurrentUserDto().getId();
		ids.add(id);
		return ids;

	}


	/**
	 * 构建目标sql语句
	 *
	 * @param originSql 		原生sql
	 * @param resourceUserIds 	所属资源用户ids
	 * @return 目标sql
	 */
	public static String buildTargetSql(String originSql, Set<Long> resourceUserIds) {
		if (BaseService.isAdmin()) {
			return originSql;
		}
		String sqlWhereBefore = org.dubhe.utils.StringUtils.substringBefore(originSql.toLowerCase(), "where");
		String sqlWhereAfter = org.dubhe.utils.StringUtils.substringAfter(originSql.toLowerCase(), "where");
		StringBuffer buffer = new StringBuffer();
		//操作的sql拼接
		String targetSql = buffer.append(sqlWhereBefore).append(" where ").append(" origin_user_id in (")
				.append(org.dubhe.utils.StringUtils.join(resourceUserIds, ",")).append(") and ").append(sqlWhereAfter).toString();

		return targetSql;
	}




}
