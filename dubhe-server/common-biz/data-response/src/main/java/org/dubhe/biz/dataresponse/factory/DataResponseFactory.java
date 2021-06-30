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

package org.dubhe.biz.dataresponse.factory;


import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.constant.ResponseCode;

/**
 * @description DataResponseBody 工厂类
 * @date 2020-05-28
 */
public class DataResponseFactory {

    private DataResponseFactory(){
        
    }

    /**
     * 成功响应
     *
     * @param <T>
     * @return
     */
    public static <T> DataResponseBody success(){
        return success(null,null);
    }

    /**
     * 成功响应
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> DataResponseBody success(T data){
        return success(null,data);
    }

    /**
     * 成功响应
     *
     * @param msg
     * @return
     */
    public static DataResponseBody successWithMsg(String msg){
        return success(msg,null);
    }

    /**
     * 成功响应
     *
     * @param msg
     * @param data
     * @param <T>
     * @return
     */
    public static <T> DataResponseBody success(String msg, T data){
        return new DataResponseBody(ResponseCode.SUCCESS,msg,data);
    }

    /**
     * 失败响应 msg
     *
     * @param msg
     * @return
     */
    public static DataResponseBody failed(String msg){
        return failed(ResponseCode.ERROR,msg,null);
    }

    /**
     * 失败响应
     *
     * @param failedCode
     * @param msg
     * @return
     */
    public static DataResponseBody failed(Integer failedCode, String msg){
        return failed(failedCode,msg,null);
    }

    /**
     * 失败响应
     *
     * @param failedCode
     * @return
     */
    public static DataResponseBody failed(Integer failedCode){
        return failed(failedCode,null,null);
    }

    /**
     * 失败响应
     *
     * @param failedCode
     * @param msg
     * @param data
     * @param <T>
     * @return
     */
    public static <T> DataResponseBody failed(Integer failedCode, String msg, T data){
        return new DataResponseBody(failedCode,msg,data);
    }



}
