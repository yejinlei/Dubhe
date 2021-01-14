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

package org.dubhe.k8s.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * @description k8s resource的label
 * @date 2020-04-14
 */
@Data
@EqualsAndHashCode
public class LabelBO implements Map.Entry<String, String> {

    private String key;
    private String value;

    public LabelBO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static LabelBO of(String key, String value) {
        return new LabelBO(key, value);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(String value) {
        String old = this.value;
        this.value = value;
        return old;
    }
}
