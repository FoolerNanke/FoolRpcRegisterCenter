package com.scj.foolRpcServer.register;

import com.scj.foolRpcBase.constant.Constant;
import com.scj.foolRpcBase.handler.in.AddTimeHandler;
import com.scj.foolRpcBase.handler.in.FoolProtocolDecode;
import com.scj.foolRpcBase.handler.in.PingPongReqHandler;
import com.scj.foolRpcBase.handler.out.FoolProtocolEncode;
import com.scj.foolRpcBase.handler.resp.FoolRespHandler;
import com.scj.foolRpcServer.constant.FRSConstant;
import com.scj.foolRpcServer.handler.FoolCommonReqHandler;
import com.scj.foolRpcServer.handler.NginxIpHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author suchangjie.NANKE
 * @Title: FoolRegServer
 * @date 2023/8/25 00:30
 * @description 注册中心核心
 */

@Component
public class FoolRegServer implements InitializingBean {

    @Autowired
    private FoolCommonReqHandler foolCommonReqHandler;

    @Autowired
    private NginxIpHandler nginxIpHandler;

    @Override
    public void afterPropertiesSet() {
        new ServerBootstrap()
                .group(FRSConstant.EXECUTORS)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) {
                        channel.pipeline()
                        // nginx 真实ip传递解码器
                        .addLast(new HAProxyMessageDecoder())
                        // 处理真实 ip
                        .addLast(nginxIpHandler)
                        // 解码
                        .addLast(new FoolProtocolDecode())
                        // 编码
                        .addLast(new FoolProtocolEncode<>())
                        // 请求处理器
                        .addLast(foolCommonReqHandler)
                        // 心跳响应处理器
                        .addLast(new PingPongReqHandler())
                        // 心跳加时处理器
                        .addLast(new AddTimeHandler())
                        // 响应结果设值处理器
                        .addLast(new FoolRespHandler());
                    }
                }).bind(Constant.REGISTER_PORT);
    }
}
