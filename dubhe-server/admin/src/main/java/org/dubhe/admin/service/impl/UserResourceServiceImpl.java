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
package org.dubhe.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import org.dubhe.admin.client.ResourceNamespaceClient;
import org.dubhe.admin.dao.UserConfigMapper;
import org.dubhe.admin.dao.UserGpuConfigMapper;
import org.dubhe.admin.dao.UserMapper;
import org.dubhe.admin.domain.dto.UserResourceListDTO;
import org.dubhe.admin.domain.dto.UserResourceQueryDTO;
import org.dubhe.admin.domain.entity.UserConfig;
import org.dubhe.admin.domain.entity.UserGpuConfig;
import org.dubhe.admin.domain.vo.UserLimitConfigVO;
import org.dubhe.admin.domain.vo.UserResourceResVO;
import org.dubhe.admin.enums.ResourceTypeEnum;
import org.dubhe.admin.enums.StatTypeEnum;
import org.dubhe.admin.service.UserResourceService;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.constant.UserConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.MathUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.GpuAllotVO;
import org.dubhe.biz.base.vo.UserAllotVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 用户资源统计实现层
 * @date 2021-11-23
 */
@Service
public class UserResourceServiceImpl implements UserResourceService {

	@Autowired
	private UserConfigMapper userConfigMapper;

	@Autowired
	private UserGpuConfigMapper userGpuConfigMapper;

	@Autowired
	private ResourceNamespaceClient resourceNamespaceClient;

	@Autowired
	private UserMapper userMapper;

	private Map<String, Map<Long, String>> gpuMap = Maps.newHashMap();
	private Map<String, Map<Long, String>> cpuMap = Maps.newHashMap();
	private Map<String, Map<Long, String>> memMap = Maps.newHashMap();
	private Map<Long, UserAllotVO> gpuAllotMap = Maps.newHashMap();

	private final static List<String> SUMDAYS = new ArrayList();

	static {
		SUMDAYS.add(UserConstant.UNIT_7D);
		SUMDAYS.add(UserConstant.UNIT_15D);
	}

	/**
	 * 用户资源Top10统计
	 *
	 * @param resourceQueryDTO 请求DTO实体
	 * @return List<UserAllotVO> 用户Top10资源列表
	 */
	@Override
	public List<UserAllotVO> getResourceTotal(UserResourceQueryDTO resourceQueryDTO) {
		List<UserAllotVO> userAllotList;
		//获取资源配额
		if (resourceQueryDTO.getStatType().equals(StatTypeEnum.ALLOT_TYPE.getCode())) {
			userAllotList = toResourceAllot(resourceQueryDTO);
		} else {
			userAllotList = toResourceUsage(resourceQueryDTO);
		}
		return userAllotList;
	}

	/**
	 * 用户资源统计
	 *
	 * @return List<UserResourceResVO> 用户资源统计列表VO
	 */
	@Override
	public Map<String, Object> getResourceList(UserResourceListDTO resourceListDTO) {
		List<UserResourceResVO> userResourceResList = new ArrayList<>();
		Page page = resourceListDTO.toPage();
		String sort = StrUtil.isEmpty(resourceListDTO.getSort()) ? ResourceTypeEnum.GPU_TYPE.getDesc() : resourceListDTO.getSort();
		String order = StrUtil.isEmpty(resourceListDTO.getOrder()) ? StringConstant.SORT_DESC : resourceListDTO.getOrder();

		List<UserLimitConfigVO> userLimitConfigs = userConfigMapper.selectLimitSum(page, sort, order);
		List<Long> userIds = userLimitConfigs.stream().map(UserLimitConfigVO::getUserId).collect(Collectors.toList());
		String namespaces = userIds.stream().map(id -> UserConstant.NAMESPACE_PREFIX + id).collect(Collectors.joining("|"));
		//查询gpu具体型号的配额
		toGpuAllotMap(userIds);
		//查询不同条件下的资源使用峰值
		SUMDAYS.stream().forEach(day -> {
			EnumSet.allOf(ResourceTypeEnum.class).forEach(code -> {
				toResourceUsageMap(code.getCode(), day, namespaces);
			});
		});


		for (UserLimitConfigVO userLimitConfig : userLimitConfigs) {
			UserResourceResVO userResourceRes = new UserResourceResVO();
			userResourceRes.setId(userLimitConfig.getUserId())
					.setUserName(userLimitConfig.getUserName())
					.setNickName(userLimitConfig.getNickName())
					.setGpu(userLimitConfig.getGpu())
					.setCpu(userLimitConfig.getCpu())
					.setMem(userLimitConfig.getMem());
			if (gpuAllotMap.containsKey(userLimitConfig.getUserId())) {
				userResourceRes.setGpuModelAllots(new ArrayList<>());
			} else {
				userResourceRes.setGpuModelAllots(gpuAllotMap.get(userLimitConfig.getUserId()).getGpuAllotList());
			}
			userResourceRes.setGpu7unit(gpuMap.get(UserConstant.UNIT_7D).getOrDefault(userLimitConfig.getUserId(), SymbolConstant.ZERO));
			userResourceRes.setGpu15unit(gpuMap.get(UserConstant.UNIT_15D).getOrDefault(userLimitConfig.getUserId(), SymbolConstant.ZERO));
			userResourceRes.setGpu7(MathUtils.floatDivision(userResourceRes.getGpu7unit(), userResourceRes.getGpu(), NumberConstant.NUMBER_2).toString());
			userResourceRes.setGpu15(MathUtils.floatDivision(userResourceRes.getGpu15unit(), userResourceRes.getGpu(), NumberConstant.NUMBER_2).toString());

			userResourceRes.setCpu7unit(MathUtils.floatDivision(cpuMap.get(UserConstant.UNIT_7D).getOrDefault(userLimitConfig.getUserId(), SymbolConstant.ZERO), SymbolConstant.ONE, NumberConstant.NUMBER_2).toString());
			userResourceRes.setCpu15unit(MathUtils.floatDivision(cpuMap.get(UserConstant.UNIT_15D).getOrDefault(userLimitConfig.getUserId(), SymbolConstant.ZERO), SymbolConstant.ONE, NumberConstant.NUMBER_2).toString());
			userResourceRes.setCpu7(MathUtils.floatDivision(userResourceRes.getCpu7unit(), userResourceRes.getCpu(), NumberConstant.NUMBER_2).toString());
			userResourceRes.setCpu15(MathUtils.floatDivision(userResourceRes.getCpu15unit(), userResourceRes.getCpu(), NumberConstant.NUMBER_2).toString());

			userResourceRes.setMem7unit(MathUtils.floatDivision(memMap.get(UserConstant.UNIT_7D).getOrDefault(userLimitConfig.getUserId(), SymbolConstant.ZERO), StringConstant.MEM_UNIT, NumberConstant.NUMBER_2).toString());
			userResourceRes.setMem15unit(MathUtils.floatDivision(memMap.get(UserConstant.UNIT_15D).getOrDefault(userLimitConfig.getUserId(), SymbolConstant.ZERO), StringConstant.MEM_UNIT, NumberConstant.NUMBER_2).toString());
			userResourceRes.setMem7(MathUtils.floatDivision(userResourceRes.getMem7unit(), userResourceRes.getMem(), NumberConstant.NUMBER_2).toString());
			userResourceRes.setMem15(MathUtils.floatDivision(userResourceRes.getMem15unit(), userResourceRes.getMem(), NumberConstant.NUMBER_2).toString());

			userResourceResList.add(userResourceRes);
		}
		return PageUtil.toPage(page, userResourceResList);
	}

	/**
	 * 根据namespace-userId批量查询资源用量峰值
	 *
	 * @param resourceType 资源类型
	 * @param sumDay 查询周期
	 * @param namespaces 拼接的namespace字符串，例：namespace-1 | namespace-2 | namespace-3
	 */
	private void toResourceUsageMap(Integer resourceType, String sumDay, String namespaces) {
		DataResponseBody<Map<Long, String>> result = resourceNamespaceClient.getResourceUsageByUser(resourceType, sumDay, namespaces);
		if (!result.succeed()) {
			throw new BusinessException("查询某用户用量峰值远程调用失败");
		}
		if (resourceType.equals(ResourceTypeEnum.GPU_TYPE.getCode())) {
			gpuMap.put(sumDay, result.getData());
		} else if (resourceType.equals(ResourceTypeEnum.CPU_TYPE.getCode())) {
			cpuMap.put(sumDay, result.getData());
		} else if (resourceType.equals(ResourceTypeEnum.MEMORY_TYPE.getCode())) {
			memMap.put(sumDay, result.getData());
		}
	}

	/**
	 * 统计用户GPU具体型号的配额
	 *
	 * @param userIds 用户id集合
	 */
	private void toGpuAllotMap(List<Long> userIds) {
		//按型号获取gpu分配总量
		List<UserGpuConfig> userGpuConfigs = userGpuConfigMapper.selectList(new LambdaQueryWrapper<UserGpuConfig>().in(UserGpuConfig::getUserId, userIds));
		for (Long userId : userIds) {
			UserAllotVO userAllotVO = new UserAllotVO();
			List<GpuAllotVO> gpuAllots = new ArrayList<>();
			for (UserGpuConfig gpuConfig : userGpuConfigs) {
				if (userId.equals(gpuConfig.getUserId())) {
					GpuAllotVO gpuAllotVO = new GpuAllotVO();
					gpuAllotVO.setGpuModel(gpuConfig.getGpuModel());
					gpuAllotVO.setAllotTotal(gpuConfig.getGpuLimit());
					gpuAllots.add(gpuAllotVO);
					userAllotVO.setAllotTotal(MathUtils.add(userAllotVO.getAllotTotal(), gpuConfig.getGpuLimit().toString()));
					userAllotVO.setUserId(gpuConfig.getUserId());
					userAllotVO.setGpuAllotList(gpuAllots);
				}
			}
			gpuAllotMap.put(userId, userAllotVO);
		}
	}

	/**
	 * 远程调用prometheus统计用户资源使用峰值
	 *
	 * @param resourceQueryDTO 查询DTO实体
	 * @return List<UserAllotVO> GPU型号资源配额列表
	 */
	private List<UserAllotVO> toResourceUsage(UserResourceQueryDTO resourceQueryDTO) {
		List<UserAllotVO> userAllotList = new ArrayList<>();
		DataResponseBody<List<UserAllotVO>> result = resourceNamespaceClient.getResourceNamespace(resourceQueryDTO.getResourceType(), resourceQueryDTO.getSumDay());
		if (!result.succeed()) {
			throw new BusinessException("查询用户用量峰值远程调用失败");
		}
		if (CollUtil.isNotEmpty(result.getData())) {
			userAllotList = result.getData().stream().map(userAllotVO -> {
				Long userId = Long.valueOf(userAllotVO.getUserName().replaceAll(UserConstant.NAMESPACE_PREFIX, StrUtil.EMPTY));
				userAllotVO.setUserName(userMapper.findUserNameById(userId));
				userAllotVO.setUserId(userId);
				if (!resourceQueryDTO.getResourceType().equals(ResourceTypeEnum.GPU_TYPE.getCode())) {
					userAllotVO.setAllotTotal(MathUtils.floatDivision(userAllotVO.getAllotTotal(), StringConstant.MEM_UNIT, 2).toString());
				} else {
					userAllotVO.setAllotTotal(userAllotVO.getAllotTotal());
				}

				return userAllotVO;
			}).collect(Collectors.toList());
		}
		if (resourceQueryDTO.getStatType().equals(StatTypeEnum.USAGE_RATE_TYPE.getCode())) {
			userAllotList = toUserAllotById(resourceQueryDTO.getResourceType(), userAllotList);
		}
		return userAllotList;
	}

	/**
	 * TOP10资源统计配额
	 *
	 * @param resourceQueryDTO 用户资源统计DTO
	 * @return List<UserAllotVO> 用户Top10资源列表
	 */
	private List<UserAllotVO> toResourceAllot(UserResourceQueryDTO resourceQueryDTO) {
		List<UserAllotVO> userAllotList = new ArrayList<>();
		switch (resourceQueryDTO.getResourceType()) {
			case 2:
				userAllotList = userConfigMapper.selectCpuAllotTotal();
				break;
			case 3:
				userAllotList = userConfigMapper.selectMemoryAllotTotal();
				break;
			case 1:
				List<UserGpuConfig> gpuConfigList = userGpuConfigMapper.selectAllotTotal();
				for (UserGpuConfig gpuConfig : gpuConfigList) {
					UserAllotVO userAllotVO = new UserAllotVO();
					userAllotVO.setUserName(gpuConfig.getUserName());
					userAllotVO.setAllotTotal(gpuConfig.getGpuLimit().toString());
					userAllotVO.setGpuAllotList(userGpuConfigMapper.selectGpuModelTotal(gpuConfig.getUserId()));
					userAllotList.add(userAllotVO);
				}
			default:
				break;
		}
		return userAllotList;
	}

	/**
	 *  用户资源配额统计
	 *
	 * @param resourceType 资源类型
	 * @param userAllotList 用户Top10资源列表
	 * @return List<UserAllotVO> 用户Top10资源列表
	 */
	private List<UserAllotVO> toUserAllotById(Integer resourceType, List<UserAllotVO> userAllotList) {
		switch (resourceType) {
			//GPU配额总量
			case 1:
				userAllotList = userAllotList.stream().map(userAllotVO -> {
					int gpuSum = userGpuConfigMapper.selectGpuLimitSum(userAllotVO.getUserId());
					userAllotVO.setAllotTotal(MathUtils.floatDivision(userAllotVO.getAllotTotal(), String.valueOf(gpuSum), NumberConstant.NUMBER_2).toString());
					return userAllotVO;
				}).collect(Collectors.toList());
				break;
			//CPU配额总量
			case 2:
				userAllotList = userAllotList.stream().map(userAllotVO -> {
					UserConfig userConfig = userConfigMapper.selectLimitSumByUser(userAllotVO.getUserId());
					if (userConfig != null) {
						userAllotVO.setAllotTotal(MathUtils.floatDivision(userAllotVO.getAllotTotal(), String.valueOf(userConfig.getCpuLimit()), 2).toString());
					}
					return userAllotVO;
				}).collect(Collectors.toList());
				break;
			//内存配额总量
			case 3:
				userAllotList = userAllotList.stream().map(userAllotVO -> {
					UserConfig userConfig = userConfigMapper.selectLimitSumByUser(userAllotVO.getUserId());
					if (userConfig != null) {
						userAllotVO.setAllotTotal(MathUtils.floatDivision(userAllotVO.getAllotTotal(), String.valueOf(userConfig.getMemoryLimit()), NumberConstant.NUMBER_2).toString());
					}
					return userAllotVO;
				}).collect(Collectors.toList());
				break;
			default:
				break;
		}
		return userAllotList;
	}
}
