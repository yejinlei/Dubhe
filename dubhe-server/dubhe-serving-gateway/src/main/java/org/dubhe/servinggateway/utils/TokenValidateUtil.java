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

package org.dubhe.servinggateway.utils;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import org.dubhe.biz.base.constant.NumberConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @description token工具类
 * @date 2020-10-09
 */
@Component
public class TokenValidateUtil {

	/**
	 * 生成token所用的秘钥
	 */
	@Value("${serving.gateway.token.secret-key}")
	private String secretKey;
	/**
	 * token超时时间
	 */
	@Value("${serving.gateway.token.expire-seconds}")
	private Integer expireSeconds;

	/**
	 * 生成token
	 *
	 * @return String
	 */
	public String generateToken() {
		String expireTime = DateUtil.format(
				DateUtil.offset(new Date(), DateField.SECOND, expireSeconds),
				DatePattern.PURE_DATETIME_PATTERN
		);
		return AesUtil.encrypt(expireTime, secretKey);
	}

	/**
	 * 验证token
	 *
	 * @param token
	 * @return boolean
	 */
	public boolean validateToken(String token) {
		String expireTime = AesUtil.decrypt(token, secretKey);
		String nowTime = DateUtil.format(
				new Date(),
				DatePattern.PURE_DATETIME_PATTERN
		);
		return expireTime.compareTo(nowTime) > NumberConstant.NUMBER_0;
	}
}
