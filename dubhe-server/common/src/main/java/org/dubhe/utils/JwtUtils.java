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

package org.dubhe.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.dubhe.domain.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @description  JWT
 * @date 2020-03-14
 */
@Component
public class JwtUtils {


    /**
     * 请求头
     */
    public static final String AUTH_HEADER = "Authorization";
    /**
     * 过期时间20分钟
     */
    private static final long EXPIRE_TIME = 20 * 60 * 1000;
    private static final String FIELD_NAME = "USER_NAME";
    private static JwcConfig jwcConfig;
    private static RedisUtils redisUtils;


    @Autowired
    public void setJwcConfig(JwcConfig mtJwcConfig) {
        jwcConfig = mtJwcConfig;
    }

    @Autowired
    public void setRedisUtils(RedisUtils myredisUtils) {
        redisUtils = myredisUtils;
    }

    /**
     * 验证token是否正确
     */
    public static boolean verify(String token, String username) {
        try {
            Algorithm algorithm = null;
            algorithm = Algorithm.HMAC256(jwcConfig.secret);
            JWTVerifier verifier = JWT.require(algorithm).withClaim(FIELD_NAME, username).build();
            verifier.verify(token);
            return true;
        } catch (UnsupportedEncodingException exception) {
            return false;
        }
    }

    /**
     * 获得token中的自定义信息，无需secret解密也能获得
     */
    public static String getClaimFiled(String token, String filed) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(filed).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 生成签名
     */
    public static String sign(String userName) {
        try {
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(jwcConfig.secret);
            // 附带username，
            String token = JWT.create().withClaim(FIELD_NAME, userName).withExpiresAt(date).sign(algorithm);
            saveToken(userName, token);
            return token;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * 获取用户名
     */
    public static String getUserName(String token) {
        return getClaimFiled(token, FIELD_NAME);
    }

    /**
     * 获取 token的签发时间
     */
    public static Date getIssuedAt(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getIssuedAt();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 验证 token是否过期
     */
    public static boolean isTokenExpired(String token) {
        Date now = Calendar.getInstance().getTime();
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getExpiresAt() == null || jwt.getExpiresAt().before(now);
    }

    /**
     * 刷新 token的过期时间
     */
    public static String refreshTokenExpired(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Map<String, Claim> claims = jwt.getClaims();
        try {
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(jwcConfig.secret);
            Builder builer = JWT.create().withExpiresAt(date);
            for (Entry<String, Claim> entry : claims.entrySet()) {
                builer.withClaim(entry.getKey(), entry.getValue().asString());
            }
            String newToken = builer.sign(algorithm);
            String userName = claims.get(FIELD_NAME).asString();
            String tokenKey = getUserTokenKey(userName);
            if (redisUtils.get(tokenKey) == null) {
                return null;
            }
            saveToken(userName, newToken);
            return newToken;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * 生成16位随机盐
     */
    public static String generateSalt() {
        SecureRandomNumberGenerator secureRandom = new SecureRandomNumberGenerator();
        String hex = secureRandom.nextBytes(16).toHex();
        return hex;
    }

    /**
     * 获取当前用户
     */
    public static UserDTO getCurrentUserDto() {
        Object principal = null;
        SecurityManager manager = ThreadContext.getSecurityManager();
        if (!Objects.isNull(manager)) {
            principal = SecurityUtils.getSubject().getPrincipal();
        }
        return Objects.isNull(principal) ? null : (UserDTO) principal;
    }

    /**
     * 获取用户token key
     */
    private static String getUserTokenKey(String userName) {
        return jwcConfig.onlineKey + userName;
    }

    private static void saveToken(String userName, String token) {
        String tokenKey = getUserTokenKey(userName);
        redisUtils.set(tokenKey, token, jwcConfig.jwtExpiration, TimeUnit.MINUTES);
    }
}
