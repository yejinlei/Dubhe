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

package org.dubhe.harbor.utils;
import org.apache.commons.io.IOUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;
import static org.dubhe.biz.base.constant.StringConstant.UTF8;
import static org.dubhe.biz.base.constant.SymbolConstant.BLANK;

/**
 * @description httpClient工具类，不校验SSL证书
 * @date 2020-05-21
 */
public class HttpClientUtils {

    public static String sendHttps(String path) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        String result = BLANK;
        HttpsURLConnection con = null;
        try {
            con = getConnection(path);
            con.connect();

            /**将返回的输入流转换成字符串**/
            inputStream = con.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, UTF8);
            bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                stringBuilder.append(str);
            }

            result = stringBuilder.toString();
            LogUtil.info(LogEnum.BIZ_SYS,"Request path:{}, SUCCESS, result:{}", path, result);

        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_SYS,"Request path:{}, ERROR, exception:{}", path, e);
            return result;
        } finally {
            closeResource(bufferedReader,inputStreamReader,inputStream,con);
        }

        return result;
    }
    public static String sendHttpsDelete(String path,String username,String password) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        String result = BLANK;
        HttpsURLConnection con = null;
        try {
            con = getConnection(path);
            String input =username+ ":" +password;
            String encoding=Base64.encodeBase64String(input.getBytes());
            con.setRequestProperty("Authorization", "Basic " + encoding);
            con.setRequestMethod("DELETE");
            con.connect();
            /**将返回的输入流转换成字符串**/
            inputStream = con.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, UTF8);
            bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                stringBuilder.append(str);
            }

            result = stringBuilder.toString();
            LogUtil.info(LogEnum.BIZ_SYS,"Request path:{}, SUCCESS, result:{}", path, result);

        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_SYS,"Request path:{}, ERROR, exception:{}", path, e);
            return result;
        } finally {
            closeResource(bufferedReader,inputStreamReader,inputStream,con);
        }

        return result;
    }

    private static void closeResource(BufferedReader bufferedReader,InputStreamReader inputStreamReader,InputStream inputStream,HttpsURLConnection con) {
        if (inputStream != null) {
            IOUtils.closeQuietly(inputStream);
        }

        if (inputStreamReader != null) {
            IOUtils.closeQuietly(inputStreamReader);
        }
        if (bufferedReader != null) {
            IOUtils.closeQuietly(bufferedReader);
        }
        if (con != null) {
            con.disconnect();
        }
    }


    private  static HttpsURLConnection getConnection(String path){
        HttpsURLConnection con = null;
        try {
            /**创建并初始化SSLContext对象**/
            TrustManager[] trustManagers = {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());

            /**得到SSLSocketFactory对象**/
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            URL url = new URL(path);
            con = (HttpsURLConnection) url.openConnection();
            con.setSSLSocketFactory(sslSocketFactory);
            con.setUseCaches(false);
        }catch (Exception e){
            LogUtil.error(LogEnum.BIZ_SYS,"Request path:{}, error, exception:{}", path, e);
        }
        return con;

    }

}


