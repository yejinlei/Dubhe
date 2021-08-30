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
package org.dubhe.dubhek8s.handler;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.WebsocketDataResponseBody;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.authconfig.dto.JwtUserDTO;
import org.dubhe.dubhek8s.service.SystemNamespaceService;
import org.dubhe.k8s.enums.WebsocketTopicEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @description WebSocket 服务处理类
 * @date 2021-7-19
 */
@ServerEndpoint("/ws")
@Component
public class WebSocketServer {

    // ConcurrentHashMap 用于保存 session 信息
    private static final ConcurrentMap<Long, WebSocketServer> USER_CLIENT_MAP = new ConcurrentHashMap<>();
    private Session session;
    private Long userId;

    // 需要注入的 bean 声明为静态变量，保证每一个用户连接创建的 websocket 对象都能使用
    private static SystemNamespaceService systemNamespaceService;
    private static UserDetailsService userDetailsService;

    @Autowired
    public void setSystemNamespaceService(SystemNamespaceService systemNamespaceService){
        WebSocketServer.systemNamespaceService = systemNamespaceService;
    }

    @Autowired
    public void setUserDetailsService(@Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService){
        WebSocketServer.userDetailsService = userDetailsService;
    }


    /**
     * 连接成功调用的方法
     *
     * @param session 连接 session
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        // 验证 session 是否合法
        userId = verify(session);
        // 如果为 null，则不合法，断开连接
        if (userId == null){
            close();
            return;
        }
        // 这里用 用户ID，不用 sessionId 是因为发送消息时需要通过用户Id查询消息内容
        USER_CLIENT_MAP.put(userId, this);
    }

    /**
     * 收到客户端发送的消息后调用的方法
     *
     * @param message 消息
     * @param session session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        LogUtil.debug(LogEnum.BIZ_K8S,"WebSocketServer onMessage, message:{}, session:{}, sessionId:{}", message, this.toString(), session.getId());
        // 验证 session 是否合法
        Long userId = verify(session);
        // 如果不合法，或者 USER_CLIENT_MAP 不包含这个 session，则关闭
        if (userId == null || !USER_CLIENT_MAP.containsKey(userId)){
            this.close();
            return;
        }
        // 解析客户端发过来的消息
        WebsocketDataResponseBody websocketDataResponseBody = JSON.parseObject(message, WebsocketDataResponseBody.class);
        // 校验 Topic
        if (WebsocketTopicEnum.RESOURCE_MONITOR.getTopic().equals(websocketDataResponseBody.getTopic())){
            sendMessage(JSON.toJSONString(new WebsocketDataResponseBody(WebsocketTopicEnum.RESOURCE_MONITOR.getTopic(), systemNamespaceService.findNamespace(userId))));
        } else {
            sendMessage(JSON.toJSONString(new WebsocketDataResponseBody(ResponseCode.BADREQUEST, null, null)));
        }
    }


    /**
     * 发生异常时的方法
     *
     * @param session   客户端 session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        if (this.session != null && this.session.isOpen()) {
            LogUtil.error(LogEnum.BIZ_K8S, "An error occurred on Websocket connection, sessionId:{}, session:{}, error:{}",session.getId(), this, throwable);
            this.close();
        } else {
            LogUtil.debug(LogEnum.BIZ_K8S,"An error occurred on the closed websocket connection, inputSession:{}, localSession:{}, error:{}", session.getId(), this, throwable);
        }
    }

    /**
     * 连接关闭时调用的方法
     */
    @OnClose
    public void onClose() {
        LogUtil.debug(LogEnum.BIZ_K8S,"WebSocketServer onClose, session:{}", this);
        this.close();
    }

    /**
     * 给所有 session 发消息的方法
     */
    public void sendToAll() {
        USER_CLIENT_MAP.keySet().parallelStream().forEach(userId -> USER_CLIENT_MAP.get(userId)
                .sendMessage(JSON.toJSONString(new WebsocketDataResponseBody(WebsocketTopicEnum.RESOURCE_MONITOR.getTopic(),
                        systemNamespaceService.findNamespace(userId)))));
    }

    public void sendToClient(Long userId) {
        if (USER_CLIENT_MAP.get(userId) != null){
            USER_CLIENT_MAP.get(userId).sendMessage(JSON.toJSONString(new WebsocketDataResponseBody(WebsocketTopicEnum.RESOURCE_MONITOR.getTopic(),
                    systemNamespaceService.findNamespace(userId))));
        }

    }

    /**
     * 获取存储 session 的 Map
     */
    public static ConcurrentMap getUserClientMap(){
        return USER_CLIENT_MAP;
    }

    /**
     * 推送消息
     *
     * @param message 消息内容
     */
    private void sendMessage(String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "WebSocketServer sendMessage error, message:{}, session:{}, error:{}",message, this, e);
        }
    }

    /**
     * 关闭session连接
     */
    private void close() {
        // 从 map 中删除 session
        if (userId != null){
            USER_CLIENT_MAP.remove(userId);
        }
        if (session == null) {
            LogUtil.debug(LogEnum.BIZ_K8S, "Websocket connection had been closed, session:{}", this);
            return;
        }
        // 关闭session
        try {
            if (session.isOpen()) {
                session.close();
            }
            LogUtil.info(LogEnum.BIZ_K8S, "Websocket connection is closed" );
        } catch (IOException e) {
            LogUtil.error(LogEnum.BIZ_K8S,"WebSocketServer close error, session:{}, error:{}" );
        }
    }

    /**
     * 验证 session 是否合法，合法返回用户 ID, 不合法返回 null
     *
     * @param session 连接 session
     * @return Long
     */
    private Long verify (Session session) {
        DecodedJWT jwt = null;
        Long curUserId = null;

        // 获取请求参数
        String queryString = session.getQueryString();
        if (StringUtils.isEmpty(queryString)){
            return curUserId;
        }
        // 获取 token
        String token = queryString.contains("Bearer%20") ? queryString.substring(9) : "";

        try {
            // 解析 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(AuthConst.CLIENT_SECRET)).build();
            jwt = jwtVerifier.verify(token);
            Map<String, Claim> claims = jwt.getClaims();

            // 获取用户名
            Claim userNameClaim = claims.get("user_name");
            String userName = userNameClaim.asString();
            // 获根据用户名取用户信息
            JwtUserDTO jwtUserDTO = (JwtUserDTO) userDetailsService.loadUserByUsername(userName);
            curUserId = jwtUserDTO.getCurUserId();

        }catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S,"WebSocketServer verify error, error:{}", e);
        }
        return curUserId;
    }
}
