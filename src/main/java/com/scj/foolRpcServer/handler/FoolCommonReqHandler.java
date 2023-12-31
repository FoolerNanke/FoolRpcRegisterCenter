package com.scj.foolRpcServer.handler;

import com.scj.foolRpcBase.entity.FoolProtocol;
import com.scj.foolRpcBase.constant.Constant;
import com.scj.foolRpcBase.entity.FoolCommonReq;
import com.scj.foolRpcBase.entity.FoolCommonResp;
import com.scj.foolRpcBase.exception.ExceptionEnum;
import com.scj.foolRpcServer.cache.FoolCache;
import com.scj.foolRpcServer.constant.FRSConstant;
import com.scj.foolRpcServer.pingpong.RegPingPong;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author suchangjie.NANKE
o * @Title: FoolCommonReqHandler
 * @date 2023/8/25 19:46
 * @description 下游注册信息处理类
 */

@Component
@ChannelHandler.Sharable
public class FoolCommonReqHandler extends SimpleChannelInboundHandler<FoolProtocol<FoolCommonReq>> {

    @Autowired
    private FoolCache localCache;

    @Autowired
    private RegPingPong regPingPong;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx
            , FoolProtocol<FoolCommonReq> foolProtocol) {
        if (foolProtocol.getRemoteType() == Constant.PONG_RESP){
            ctx.fireChannelRead(foolProtocol);
            return;
        }
        // 来自下游的注册信息
        FoolCommonReq req = foolProtocol.getData();
        // 获取发送-处理的时间差
        long gap = System.currentTimeMillis() - req.getTimeStamp();
        // 拼接key
        String ip = ctx.channel().remoteAddress().toString();
        if (ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_IP)).get() != null){
            String real_ip = ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_IP)).get().toString();
            String real_port = ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_PORT)).get().toString();
            ip = "/" + real_ip + ":" + real_port;
        }
        switch (foolProtocol.getRemoteType()) {
            // 获取下游服务IP
            case Constant.REGISTER_REQ_GET_IP:
                handReqForIp(req, ctx, gap, foolProtocol.getReqId());
                break;
            // 注册服务
            case Constant.REGISTER_REQ_REG_CLASS:
                handReqForRegister(req, ctx, gap, foolProtocol.getReqId(), ip);
                break;
        }
        ctx.fireChannelRead(foolProtocol);
    }

    /**
     * 处理IP请求
     * @param req 请求体
     * @param ctx 通道
     * @param gap 间隔
     * @param reqId 请求ID
     */
    public void handReqForIp(FoolCommonReq req, ChannelHandlerContext ctx
            , long gap, long reqId) {
        FoolProtocol<FoolCommonResp> foolProtocol =
                buildResp(reqId, Constant.REGISTER_RESP_GET_IP);
        // 获取IP
        String serviceIp = localCache.getService(
                req.getFullClassName(), req.getVersion());
        // TODO 异常
        foolProtocol.getData().setCode(ExceptionEnum.SUCCESS.getErrorCode());
        foolProtocol.getData().setMessage(ExceptionEnum.SUCCESS.getErrorMessage());
        foolProtocol.getData().setIP(serviceIp);
        ctx.writeAndFlush(foolProtocol);
    }

    /**
     * 处理注册请求
     * @param req 请求体
     * @param ctx 通道
     * @param gap 间隔
     * @param reqId 请求ID
     * @param ip 服务IP
     */
    public void handReqForRegister(FoolCommonReq req, ChannelHandlerContext ctx
            , long gap, long reqId, String ip){
        FoolProtocol<FoolCommonResp> foolProtocol =
                buildResp(reqId, Constant.REGISTER_RESP_REG_CLASS);
        // 注册信息
        boolean firstTimeAdd = localCache.register(req.getAppName()
                , req.getFullClassName()
                , ip, req.getVersion(), ctx.channel());
        foolProtocol.getData().setCode(ExceptionEnum.SUCCESS.getErrorCode());
        foolProtocol.getData().setMessage(ExceptionEnum.SUCCESS.getErrorMessage());
        ctx.writeAndFlush(foolProtocol);
        // IP完成注册 添加一个IP心跳检测任务
        regPingPong.addPingPong(ctx.channel(), true);
    }

    /**
     * 构建响应
     * @param reqId 请求ID
     * @param type 类型
     * @return FoolProtocol<FoolCommonResp>
     */
    public FoolProtocol<FoolCommonResp> buildResp(long reqId, byte type) {
        // 构造响应
        FoolProtocol<FoolCommonResp> foolProtocol = new FoolProtocol<>();
        foolProtocol.setRemoteType(type);
        foolProtocol.setReqId(reqId);
        foolProtocol.setData(new FoolCommonResp());
        return foolProtocol;
    }
}
