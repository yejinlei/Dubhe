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

package org.dubhe.serving.utils;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.serving.domain.entity.DataInfo;
import org.dubhe.serving.proto.Inference;
import org.dubhe.serving.proto.InferenceServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @description Grpc配置类
 * @date 2020-12-09
 */
@Component
public class GrpcClient {

    /**
     * <服务id, grpc客户端channel>
     */
    public static ConcurrentHashMap<Long, ManagedChannel> channelMap = new ConcurrentHashMap<>();
    /**
     * k8s grpc端口
     */
    @Value("${k8s.https-port:31365}")
    private String grpcPort;
    /**
     * tls证书路径
     */
    @Value("${serving.crtPath}")
    private String crtPath;

    /**
     * 创建带tls认证的grpc通道
     *
     * @param url tls证书地址
     * @return ManagedChannel 通道
     * @throws SSLException
     */
    public ManagedChannel createTlsChannel(String url) throws Exception {
        SslContext sslContext = buildSslContext(crtPath);
        ManagedChannel channel = NettyChannelBuilder.forAddress(url, Integer.parseInt(grpcPort)).maxInboundMessageSize(NumberConstant.MAX_MESSAGE_LENGTH).negotiationType(NegotiationType.TLS).sslContext(sslContext).build();
        return channel;
    }


    /**
     * 关闭通道
     *
     * @param channel tls通道
     * @throws InterruptedException 异常
     */
    public static void shutdown(ManagedChannel channel) throws InterruptedException {
        channel.shutdown().awaitTermination(NumberConstant.NUMBER_5, TimeUnit.SECONDS);
    }

    /**
     * 执行推理
     *
     * @param imageInfoList 图片集合
     * @return Inference.DataResponse 预测结果
     */
    public static Inference.DataResponse getResult(ManagedChannel channel, List<DataInfo> imageInfoList) {
        //构建使用该channel的客户端stub，需要用到时创建，不可复用
        //阻塞存根，用于客户端本地调用
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(channel);
        Inference.DataRequest.Builder builder = Inference.DataRequest.newBuilder();
        for (DataInfo dataInfo : imageInfoList) {
            Inference.Data data = Inference.Data.newBuilder().setDataName(dataInfo.getDataName())
                    .setDataFile(dataInfo.getDataFile()).build();
            builder.addDataList(data);
        }
        Inference.DataRequest request = builder.build();
        //3.调用服务端方法
        Inference.DataResponse inference = blockingStub.inference(request);
        return inference;
    }

    /**
     * 创建grpc通道
     *
     * @param servingId 在线服务ID
     * @param url       api地址
     * @param user   用户信息
     */
    public void createChannel(Long servingId, String url, UserContext user) {
        //channel已存在，则销毁重建
        if (channelMap.containsKey(servingId)) {
            ManagedChannel channel = channelMap.get(servingId);
            try {
                GrpcClient.shutdown(channel);
            } catch (InterruptedException e) {
                LogUtil.error(LogEnum.SERVING, "An Exception occurred when user {} shutting down the grpc channel, service id：{}", user.getUsername(), servingId, e);
            }
        }
        ManagedChannel channel = null;
        try {
            channel = this.createTlsChannel(url);
            channelMap.put(servingId, channel);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SERVING, "An Exception occurred when user {} creating the grpc channel, service id：{}", user.getUsername(), servingId, e);
        }

    }

    /**
     * 获取grpc通道
     *
     * @param servingId 在线服务id
     * @param url       tls证书地址
     * @return grpc channel
     */
    public ManagedChannel getChannel(Long servingId, String url) {
        if (channelMap.containsKey(servingId)) {
            return channelMap.get(servingId);
        }
        ManagedChannel channel = null;
        try {
            channel = this.createTlsChannel(url);
            channelMap.put(servingId, channel);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SERVING, "An Exception occurred when getting grpc channel, service id：{}", servingId, e);
        }
        return channel;
    }

    /**
     * 关闭grpc通道
     *
     * @param servingId 在线服务id
     */
    public static void shutdownChannel(Long servingId) {
        if (channelMap.containsKey(servingId)) {
            ManagedChannel channel = channelMap.get(servingId);
            try {
                GrpcClient.shutdown(channel);
            } catch (InterruptedException e) {
                LogUtil.error(LogEnum.SERVING, "An Exception occurred when shutting down the grpc channel, service id：{}", servingId, e);
            }
        }
    }

    /**
     * 创建ssl连接
     *
     * @param crtPath 证书路径
     * @return SslContext对象
     * @throws Exception 异常
     */
    public static SslContext buildSslContext(String crtPath) throws Exception {
        SslContext sslContext;
        InputStream in = null;
        try {
            in = GrpcClient.class.getResourceAsStream(crtPath);
            sslContext = GrpcSslContexts.forClient().trustManager(in).build();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return sslContext;
    }
}
