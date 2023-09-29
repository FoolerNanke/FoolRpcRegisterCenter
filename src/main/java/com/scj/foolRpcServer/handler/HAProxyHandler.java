package com.scj.foolRpcServer.handler;

import com.scj.foolRpcServer.constant.FRSConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: suchangjie.NANKE
 * @Title: ss
 * @date: 2023/9/24
 * @description: //TODO
 */
public class HAProxyHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf)msg;
        ByteBuf copy = byteBuf.copy();
        byte[] bytes = new byte[copy.readableBytes()];
        copy.readBytes(bytes);
        /*消息打印--------------------------*/

        List<String> ipList = new ArrayList<>();
        StringBuilder sep = new StringBuilder();
        int flag = 0;
        byteBuf.resetReaderIndex();
        for (int i = 0; i < bytes.length; i++) {
            byteBuf.readByte();
            if (bytes[i] == 13 && bytes[i + 1] == 10) {
                byteBuf.readByte();
                ipList.add(sep.toString());
                break;
            }
            if (bytes[i] == 32) {
                if (0 == flag && !sep.toString().equals("PROXY")) {
                    byteBuf.resetReaderIndex();
                    break;
                }
                flag  = flag + 1;
                ipList.add(sep.toString());
                sep.setLength(0);
                continue;
            }
            sep.append((char) bytes[i]);
        }
        if (flag >= 3 ) {
            // 将真实ip写入到 channel 的属性当中
            ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_IP)).set(ipList.get(2));
            // 将真实port写入到 channel 的属性中
            ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_PORT)).set(ipList.get(3));
            // TODO 移除本handler
            //TODO 沾包黏包
        } else {
            byteBuf.resetReaderIndex();
        }
        ctx.fireChannelRead(msg);
    }
}
