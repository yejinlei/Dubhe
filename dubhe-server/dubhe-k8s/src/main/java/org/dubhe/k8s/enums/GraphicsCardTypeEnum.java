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

package org.dubhe.k8s.enums;

/**
 * @description graphics card type enum
 * @date 2020-05-09
 */
public enum GraphicsCardTypeEnum {
    /**
     * 显卡型号 英伟达泰坦v
     */
    TITAN_V("Titan V", "英伟达泰坦v"),
    /**
     * 显卡型号 特斯拉v100
     */
    TESLA_V100("Tesla V100", "特斯拉v100");

    GraphicsCardTypeEnum(String type, String caption) {
        this.type = type;
        this.caption = caption;
    }

    private String type;
    private String caption;

    public String getType() {
        return type;
    }

    public String getCaption() {
        return caption;
    }

}
