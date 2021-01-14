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

package org.dubhe.k8s.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dubhe.k8s.domain.entity.K8sResource;

/**
 * @description k8s resource mapper
 * @date 2020-07-10
 */
public interface K8sResourceMapper extends BaseMapper<K8sResource> {
}
