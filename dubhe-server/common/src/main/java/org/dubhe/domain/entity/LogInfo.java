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

package org.dubhe.domain.entity;
import java.io.Serializable;

import org.dubhe.base.MagicNumConstant;

import com.alibaba.fastjson.annotation.JSONField;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description 日志对象封装类
 * @date 2020-06-29
 */
@Data
@Accessors(chain = true)
public class LogInfo implements Serializable {

	@JSONField(ordinal = MagicNumConstant.ONE)
	private String traceId;

	@JSONField(ordinal = MagicNumConstant.TWO)
	private String type;

	@JSONField(ordinal = MagicNumConstant.THREE)
	private String level;

	@JSONField(ordinal = MagicNumConstant.FOUR)
	private String cName;

	@JSONField(ordinal = MagicNumConstant.FIVE)
	private String mName;
	
	@JSONField(ordinal = MagicNumConstant.SIX)
	private String line;
	
	@JSONField(ordinal = MagicNumConstant.SEVEN)
	private String time = DateUtil.now();

	@JSONField(ordinal = MagicNumConstant.EIGHT)
	private Object info;

	public void setInfo(Object info) {
		this.info = info;
	}
}
