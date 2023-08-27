package com.scj.foolRpcServer.handler;

import com.scj.foolRpcBase.constant.Constant;
import com.scj.foolRpcBase.entity.FoolProtocol;
import com.scj.foolRpcBase.entity.FoolRegisterReq;
import com.scj.foolRpcBase.entity.FoolRegisterResp;
import com.scj.foolRpcServer.constant.PromiseCache;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;

/**
 * @author suchangjie.NANKE
 * @Title: FoolPingPongHandler
 * @date 2023/8/27 17:04
 * @description 心跳响应处理器
 */
public class FoolPingPongHandler extends SimpleChannelInboundHandler<FoolProtocol<FoolRegisterResp>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx
            , FoolProtocol<FoolRegisterResp> foolProtocol) {
        if (foolProtocol.getRemoteType() == Constant.REGISTER_PONG_RESP){
            Promise<Object> promise = PromiseCache.getPromise(foolProtocol);
            promise.setSuccess(foolProtocol.getData());
        }
        ctx.fireChannelRead(foolProtocol);
    }
}
