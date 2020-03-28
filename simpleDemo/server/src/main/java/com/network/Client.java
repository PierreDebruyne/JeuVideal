package com.network;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class Client {
    protected SocketChannel socketChannel;
    protected String addr;
    protected PacketReader packetReader;
    protected List<Packet> toSend;

    public Client(SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        this.addr = socketChannel.getLocalAddress().toString();
        this.packetReader = new PacketReader(socketChannel);
        this.toSend = new LinkedList<>();
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public String getAddr() {
        return addr;
    }

    void read() throws IOException {
        this.packetReader.readPackets();
    }

    public boolean hasInput() {
        return !packetReader.getPackets().isEmpty();
    }

    public ReceivedPacket readInput() {
        return packetReader.getPackets().remove(0);
    }

    public void send(Packet packet) {
        this.toSend.add(packet);
    }

    List<ReceivedPacket> getInputs() {
        return packetReader.getPackets();
    }

    List<Packet> getToSend() {
        return toSend;
    }
}
