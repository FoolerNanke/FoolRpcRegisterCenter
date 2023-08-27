package com.scj.foolRpcServer.cache.local;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author suchangjie.NANKE
 * @Title: IpList
 * @date 2023/8/26 13:03
 * @description 存储Ip的列表，支持随机访问和并发读写
 */
public class IpList {

    /**
     * 下标
     */
    private final AtomicInteger index;

    /**
     * 存储ip:port
     * 本次不实现动态扩容
     * 本次容量固定
     */
    private final String[] ip_ports;

    public IpList(){
        index = new AtomicInteger(0);
        ip_ports = new String[256];
    }

    /**
     * 填充 ip_port
     * 如果 ip_port 已经存在则不插入
     * @param ip_port ip+port
     */
    public synchronized void add(String ip_port){
        int ind = index.get();
        for (int i = 0; i < ind; i++) {
            if (ip_ports[i].equals(ip_port)) return;
        }
        ind = index.getAndIncrement();
        ip_ports[ind] = ip_port;
    }

    /**
     * 移除一个port
     * @param ip_port 移除的ip_port
     */
    public synchronized void remove(String ip_port){
        int ind = index.get();
        for (int i = 0; i < ind; i++) {
            if (ip_ports[i].equals(ip_port)){
                // 数组拷贝
                System.arraycopy(ip_ports, i+1, ip_ports, i, ind - i);
                // 下标 --
                index.decrementAndGet();
                return;
            }
        }
    }

    /**
     * 获取一个IP
     * @return IP
     */
    // TODO 选择规则未定
    public String get(){
        return ip_ports[0].substring(1).split(":")[0];
    }
}
