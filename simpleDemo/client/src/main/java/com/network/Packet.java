package com.network;

import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Packet {

    protected String type;
    protected Object data;

    public Packet(String type, Object data) throws Exception {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public ByteBuffer toByteBuffer() throws Exception {
        Gson gson = new Gson();
        String json = gson.toJson(this.data);

        int bufferLength = 4 + 4 + type.getBytes(StandardCharsets.UTF_8).length + 4 + json.getBytes(StandardCharsets.UTF_8).length;
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);

        buffer.putInt(bufferLength).putInt(type.getBytes(StandardCharsets.UTF_8).length).put(type.getBytes(StandardCharsets.UTF_8)).putInt(json.getBytes(StandardCharsets.UTF_8).length).put(json.getBytes(StandardCharsets.UTF_8));
        return buffer;
    }
}
