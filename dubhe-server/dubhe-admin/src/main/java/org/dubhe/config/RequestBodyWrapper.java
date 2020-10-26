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

package org.dubhe.config;

import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @description 用于获取请求body参数的包装类
 * @date 2020-08-20
 */
public class RequestBodyWrapper extends HttpServletRequestWrapper {

    private ByteArrayInputStream byteArrayInputStream;

    private String bodyString;

    public RequestBodyWrapper(HttpServletRequest request)
            throws IOException {
        super(request);
        try (BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            if (sb.length() > 0) {
                bodyString = sb.toString();
            } else {
                bodyString = "";
            }
            byteArrayInputStream = new ByteArrayInputStream(bodyString.getBytes());
        } catch (Exception e) {
            LogUtil.error(LogEnum.GLOBAL_REQ, "request get reader error : {}", e);
            throw e;
        }

    }

    /**
     * 获取请求体的json数据
     *
     * @return
     */
    public String getBodyString() {
        return bodyString;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }
}  