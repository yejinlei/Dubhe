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

package org.dubhe.config;

import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @description  用于获取response 的 json 返回值
 * @date 2020-08-19
 */
public class ResponseBodyWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream byteArrayOutputStream = null;

    private ServletOutputStream servletOutputStream = null;

    private PrintWriter printWriter = null;

    private HttpServletResponse response;

    public ResponseBodyWrapper(HttpServletResponse response) throws IOException {
        super(response);
        this.response = response;
        byteArrayOutputStream = new ByteArrayOutputStream();
        printWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, "UTF-8"));
        servletOutputStream = new ServletOutputStream() {

            @Override
            public void write(int b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }
        };
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (servletOutputStream != null) {
            servletOutputStream.flush();
        }
        if (printWriter != null) {
            printWriter.flush();
        }
    }

    @Override
    public void reset() {
        byteArrayOutputStream.reset();
    }

    /**
     * 获取json返回值
     * @return
     * @throws IOException
     */
    public String getResponseBody() throws IOException {
        //清空response的流，之后再添加进去
        flushBuffer();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(LogEnum.GLOBAL_REQ, e);
        } finally {
            response.getOutputStream().write(bytes);
        }
        return "";
    }

    /**
     * 清掉缓冲
     * @throws IOException
     */
    public void flush() throws IOException {
        //清空response的流，之后再添加进去
        flushBuffer();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        response.getOutputStream().write(bytes);
    }

}