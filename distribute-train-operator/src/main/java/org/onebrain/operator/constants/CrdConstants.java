/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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

package org.onebrain.operator.constants;

/**
 * @description crd 常量信息
 * @date 2020-09-23
 */
public class CrdConstants {
    public static final String CRD_GROUP = "onebrain.oneflow.org";
    public static final String CRD_SINGULAR_NAME = "distributetrain";
    public static final String CRD_PLURAL_NAME = "distributetrains";
    public static final String CRD_NAME = CRD_PLURAL_NAME + "." + CRD_GROUP;
    public static final String CRD_KIND = "DistributeTrain";
    public static final String CRD_SCOPE = "Namespaced";
    public static final String CRD_SHORT_NAME = "dt";
    public static final String CRD_VERSION = "v1alpha1";
    public static final String CRD_API_VERSION = "apiextensions.k8s.io/v1beta1";
}
