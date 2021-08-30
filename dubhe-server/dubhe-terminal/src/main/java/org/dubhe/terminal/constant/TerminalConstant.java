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
package org.dubhe.terminal.constant;
/**
 * @description
 * @date 2021-7-19
 */
public class TerminalConstant {

    public static final String DATASET_VOLUME_MOUNTS = "/dataset";

    public static final String WORKSPACE_VOLUME_MOUNTS = "/workspace";

    public static final String SSH_USER_COMMAND = "ssh -p {} {}@{}";

    public static final String SSH_COMMAND = "ssh -p {} {}";
}
