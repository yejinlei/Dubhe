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

package org.dubhe.terminal.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @description 配置
 * @date 2021-07-19
 */
@Getter
@Configuration
public class TerminalConfig {
    //专业版终端模块目录
    @Value("${terminal.terminal-dir}")
    private String terminalDir;

    //用户workspace目录
    @Value("${terminal.workspace-dir}")
    private String workspaceDir;

    //用户workspace目录
    @Value("${terminal.ssh-host}")
    private String sshHost;

    //harbor 地址
    @Value("${harbor.address}")
    private String harborAddress;

    //服务端口
    @Value("${server.port}")
    private String serverPort;
}
