package com.network;

public class ServerInfos {
    protected String ip;
    protected int port;

    public ServerInfos(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
