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

package org.dubhe.biz.base.utils;

import cn.hutool.core.util.HexUtil;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @description AES加解密工具
 * @date 2020-06-01
 */
public class AesUtil {

    private static final String AES = "AES";


    private AesUtil(){

    }


    /**
     *
     * @param mode    Cipher mode
     * @param key      秘钥
     * @return Cipher
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    private static Cipher getCipher(int mode,String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
        SecretKeySpec secretKeySpec = new SecretKeySpec(md5Digest.digest(key.getBytes(StandardCharsets.UTF_8)), AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(mode, secretKeySpec);
        return cipher;
    }

    /**
     * 加密
     *
     * @param data  原文
     * @param key   秘钥
     * @return String     密文
     */
    public static String encrypt(String data, String key) {
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE,key);
            byte[] content = data.getBytes(StandardCharsets.UTF_8);
            return new String(HexUtil.encodeHex(cipher.doFinal(content), false));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解密
     * @param hexData  十六进制密文
     * @param key   秘钥
     * @return  String    密文
     */
    public static String decrypt(String hexData, String key) {
        try {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE,key);
            byte[] content = HexUtil.decodeHex(hexData);
            return new String(cipher.doFinal(content), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

}
