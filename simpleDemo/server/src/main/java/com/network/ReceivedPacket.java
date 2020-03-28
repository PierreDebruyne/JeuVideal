package com.network;

public class ReceivedPacket {

    protected String type;
    protected String dataJson;

    public ReceivedPacket(String type, String dataJson) {
        this.type = type;
        this.dataJson = dataJson;
    }

    public String getType() {
        return type;
    }

    public String getDataJson() {
        return dataJson;
    }
}
