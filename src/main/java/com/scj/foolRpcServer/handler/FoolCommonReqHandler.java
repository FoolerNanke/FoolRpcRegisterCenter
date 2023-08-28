package com.scj.foolRpcServer.handler;

import com.scj.foolRpcBase.entity.FoolProtocol;
import com.scj.foolRpcBase.constant.Constant;
import com.scj.foolRpcBase.entity.FoolRegisterReq;
import com.scj.foolRpcBase.entity.FoolRegisterResp;
import com.scj.foolRpcBase.exception.ExceptionEnum;
import com.scj.foolRpcServer.constant.FRSConstant;
import com.scj.foolRpcServer.pingpong.PingPongRunnable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
/**
 * @author suchangjie.NANKE
 * @Title: FoolRegisterHandler
 * @date 2023/8/25 19:46
 * @description 下游注册信息处理类
 */
public class FoolCommonReqHandler extends SimpleChannelInboundHandler<FoolProtocol<FoolRegisterReq>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx
            , FoolProtocol<FoolRegisterReq> foolProtocol) {
        if (foolProtocol.getRemoteType() == Constant.REGISTER_PONG_RESP){
            ctx.fireChannelRead(foolProtocol);
            return;
        }
        // 来自下游的注册信息
        FoolRegisterReq req = foolProtocol.getData();
        // 获取发送-处理的时间差
        long gap = System.currentTimeMillis() - req.getTimeStamp();
        // 拼接key
        String ip = ctx.channel().remoteAddress().toString();
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
    public void handReqForIp(FoolRegisterReq req, ChannelHandlerContext ctx
            , long gap, long reqId) {
        FoolProtocol<FoolRegisterResp> foolProtocol =
                buildResp(reqId, Constant.REGISTER_RESP_GET_IP);
        // 获取IP
        String serviceIp = FRSConstant.foolCache.getService(
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
    public void handReqForRegister(FoolRegisterReq req, ChannelHandlerContext ctx
            , long gap, long reqId, String ip){
        FoolProtocol<FoolRegisterResp> foolProtocol =
                buildResp(reqId, Constant.REGISTER_RESP_REG_CLASS);
        // 注册信息
        boolean firstTimeAdd = FRSConstant.foolCache.register(req.getAppName()
                , req.getFullClassName()
                , ip, req.getVersion(), ctx.channel());
        foolProtocol.getData().setCode(ExceptionEnum.SUCCESS.getErrorCode());
        foolProtocol.getData().setMessage(ExceptionEnum.SUCCESS.getErrorMessage());
        ctx.writeAndFlush(foolProtocol);

        // IP完成注册 添加一个IP心跳检测任务
        if (firstTimeAdd){
            FRSConstant.EXECUTORS.schedule(
                    new PingPongRunnable(FRSConstant.EXECUTORS
                            , ctx.channel())
                    , FRSConstant.PING_PONG_TIME_GAP
                    , FRSConstant.PING_PONG_TIME_UNIT);
        }
    }

    /**
     * 构建响应
     * @param reqId 请求ID
     * @param type 类型
     * @return FoolProtocol<FoolRegisterResp>
     */
    public FoolProtocol<FoolRegisterResp> buildResp(long reqId, byte type) {
        // 构造响应
        FoolProtocol<FoolRegisterResp> foolProtocol = new FoolProtocol<>();
        foolProtocol.setRemoteType(type);
        foolProtocol.setReqId(reqId);
        foolProtocol.setData(new FoolRegisterResp());
        return foolProtocol;
    }
}
