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
package org.dubhe.tadl.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties("tadl")
public class CmdConf {

private Map<String,Algorithm> cmd;

    public Map<String,Algorithm> getCmd(){
        return cmd;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Algorithm{
        private Key key;
        private Val val;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Key{
            private String key;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Val{
            private String val;

            public List<String> getVal() {
                return Arrays.asList(val.split(","));
            }
        }
    }
}
