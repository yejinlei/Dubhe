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

package org.dubhe.utils;

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

}
