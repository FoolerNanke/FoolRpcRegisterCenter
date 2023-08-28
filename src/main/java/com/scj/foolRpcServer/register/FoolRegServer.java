package com.scj.foolRpcServer.register;

import com.scj.foolRpcBase.constant.Constant;
import com.scj.foolRpcBase.handler.in.FoolProtocolDecode;
import com.scj.foolRpcBase.handler.out.FoolProtocolEncode;
import com.scj.foolRpcServer.constant.FRSConstant;
import com.scj.foolRpcServer.handler.FoolPingPongHandler;
import com.scj.foolRpcServer.handler.FoolCommonReqHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;


/**
 * @author suchangjie.NANKE
 * @Title: FoolRegServer
 * @date 2023/8/25 00:30
 * @description 注册中心核心
 */

@Component
public class FoolRegServer implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        new ServerBootstrap()
                .group(FRSConstant.EXECUTORS)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) {
                        channel.pipeline()
                                // 解码
                                .addLast(new FoolProtocolDecode())
                                // 编码
                                .addLast(new FoolProtocolEncode<>())
                                // 请求处理器
                                .addLast(new FoolCommonReqHandler())
                                // 心跳处理器
                                .addLast(new FoolPingPongHandler());
                    }
                }).bind(Constant.REGISTER_PORT);
    }
}
