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

import org.dubhe.enums.LogEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description: LogUtil 工具测试类
 * @date 2020-6-19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class LogUtilTest  {

	/**
	 * 
	 * 字符串使用场景
	 * 
	 */
	@Test
	public void logStringTest() {
		// info 日志级别
		// 一个字符串的场景
		LogUtil.info(LogEnum.BIZ_TRAIN, "str");
		// 字符串替换{}的场景
		LogUtil.info(LogEnum.BIZ_DATASET, "concat({}--{});", "str2", "str3");

		// trace 日志级别
		LogUtil.trace(LogEnum.BIZ_TRAIN, "str");
		LogUtil.trace(LogEnum.BIZ_DATASET, "concat({}--{});", "str2", "str3");

		// debug 日志级别
		LogUtil.debug(LogEnum.BIZ_TRAIN, "str");
		LogUtil.debug(LogEnum.BIZ_DATASET, "concat({}--{});", "str2", "str3");

		// warn 日志级别
		LogUtil.warn(LogEnum.BIZ_TRAIN, "str");
		LogUtil.warn(LogEnum.BIZ_DATASET, "concat({}--{});", "str2", "str3");

		// error 日志级别
		LogUtil.error(LogEnum.BIZ_TRAIN, "str");
		LogUtil.error(LogEnum.BIZ_DATASET, "concat({}--{});", "str2", "str3");

	}

	/**
	 * 
	 * 对象使用场景
	 * 
	 */
	@Test
	public void logObjectTest() {
		LogTest logTest1 = new LogTest();
		// info 日志级别
		// 单对象场景
		LogUtil.info(LogEnum.BIZ_DATASET, new LogTest());
		// 对象属性替换{}场景
		LogUtil.info(LogEnum.BIZ_DATASET, "object:{}", logTest1.getNameString());

		// trace 日志级别
		LogUtil.trace(LogEnum.BIZ_DATASET, new LogTest());
		LogUtil.trace(LogEnum.BIZ_DATASET, "object:{}", logTest1.getNameString());

		// debug 日志级别
		LogUtil.debug(LogEnum.BIZ_DATASET, new LogTest());
		LogUtil.debug(LogEnum.BIZ_DATASET, "object:{}", logTest1.getNameString());

		// warn 日志级别
		LogUtil.warn(LogEnum.BIZ_DATASET, new LogTest());
		LogUtil.warn(LogEnum.BIZ_DATASET, "object:{}", logTest1.getNameString());

		// error 日志级别
		LogUtil.error(LogEnum.BIZ_DATASET, new LogTest());
		LogUtil.error(LogEnum.BIZ_DATASET, "object:{}", logTest1.getNameString());
	}

	/**
	 * 异常使用场景
	 * 
	 */
	@Test
	public void logExceptionTest() {
		try {
			throw new RuntimeException();
		} catch (Exception e) {
			// 单个异常场景
			LogUtil.error(LogEnum.SYS_ERR, e);
			// 字符串+异常场景
			LogUtil.error(LogEnum.SYS_ERR, "errorStr", e);
			// 多字符串+异常场景
			LogUtil.error(LogEnum.SYS_ERR, " errorStr1为{}, errorStr2为{}, errorinfo:", "testStr1", "testStr2", e);
		}
	}

	/**
	 * 其他场景测试
	 * 
	 */
	@Test
	public void logOtherTest() {
		// LogEnum logType 为null的情况
		LogUtil.info(null, new LogTest());
		// Object[] 为空的情况
		LogUtil.info(LogEnum.BIZ_TRAIN);
		// Object[]为 null的情况
		Integer integer = null;
		LogUtil.info(LogEnum.BIZ_TRAIN, integer);
	}

	class LogTest {
		String nameString = "testInfo";
		Integer pacInteger = 1;
		long base = 7;

		public String getNameString() {
			return nameString;
		}

		public void setNameString(String nameString) {
			this.nameString = nameString;
		}

		public Integer getPacInteger() {
			return pacInteger;
		}

		public void setPacInteger(Integer pacInteger) {
			this.pacInteger = pacInteger;
		}

		public long getBase() {
			return base;
		}

		public void setBase(long base) {
			this.base = base;
		}

	}

}
