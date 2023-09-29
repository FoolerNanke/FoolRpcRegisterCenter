package com.scj.foolRpcServer.handler;

import com.scj.foolRpcServer.constant.FRSConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author: shushu
 * @Title: 代理协议
 * @date: 2023/9/24
 * @description:
 * 代理协议解析器
 * HA 代理协议
 * 在TCP协议头会增加一段数据
 * PROXY IP IP PORT PORT\t\n
 */
@Slf4j
public class HAProxyHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf)msg;
        // 判断可解析出PROXY
        if (byteBuf.readableBytes() < FRSConstant.HAP_HEAD.length()){
            // 数据不足以解析出HA的头 流程终止
            return;
        }
        // 记录当前起点 方便回溯
        byteBuf.markReaderIndex();
        // HA 头解析
        String head = byteBuf.readBytes(FRSConstant.HAP_HEAD.length()).toString(StandardCharsets.UTF_8);
        if (!head.equals(FRSConstant.HAP_HEAD)){
            // 当前请求的头不存在HA协议
            byteBuf.resetReaderIndex();
            ctx.pipeline().remove(HAProxyHandler.class);
            ctx.fireChannelRead(msg);
        }
        byteBuf.resetReaderIndex();
        // 解析全部数据
        String data = byteBuf.toString(StandardCharsets.UTF_8);
        // 判断是否存在\t\n
        int index = data.indexOf("\r\n");
        if (index == -1){
            // 数据量不够
            // 半包情况
            byteBuf.resetReaderIndex();
            return;
        }
        // 数据量足够
        byte[] saveByte = new byte[index];
        byteBuf.readBytes(saveByte);
        String[] ipAndPorts = new String(saveByte).split(FRSConstant.GAP_SPACE);
        // 将真实ip写入到 channel 的属性当中
        ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_IP)).set(ipAndPorts[2]);
        // 将真实port写入到 channel 的属性中
        ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_PORT)).set(ipAndPorts[4]);
        log.info("检测到代理协议，解析后的真实IP = {} PORT = {}", ipAndPorts[2], ipAndPorts[4]);
        // 再读两位 将\r\n读取了
        byteBuf.readByte();
        byteBuf.readByte();
        // 移除这个handler
        ctx.pipeline().remove(HAProxyHandler.class);
        ctx.fireChannelRead(msg);
    }
}
