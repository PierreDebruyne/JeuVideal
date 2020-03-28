package com.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class PacketReader {

    private final int READ_SIZE = 4096;

    protected SocketChannel socketChannel;
    protected ByteBuffer cacheBuffer;
    protected List<ReceivedPacket> packets;

    public PacketReader(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.packets = new LinkedList<>();
    }

    public void readPackets() throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(READ_SIZE);
        int readCount = socketChannel.read(readBuffer);
        if (readCount < 0) {
            throw new IOException();
        }
        while (readCount > 0) {
            int newCacheBufferSize = ((cacheBuffer != null) ? cacheBuffer.capacity() : 0) + readCount;
            ByteBuffer newCacheBuffer = ByteBuffer.allocate(newCacheBufferSize);
            if (cacheBuffer != null) {
                cacheBuffer.position(0);
                newCacheBuffer.put(cacheBuffer);
            }
            newCacheBuffer.put(readBuffer.array(), 0, readCount);
            cacheBuffer = newCacheBuffer;

            readBuffer.position(0);
            readCount = socketChannel.read(readBuffer);
        }

        boolean continueToRead = true;

        while (continueToRead && cacheBuffer.capacity() >= 4) {
            cacheBuffer.position(0);

            byte[] packetLengthBytes = new byte[4];
            for (int i = 0; i < 4; ++i) {
                packetLengthBytes[i] = cacheBuffer.get(i);
            }
            int packetLength = ByteBuffer.wrap(packetLengthBytes).getInt();

            if (packetLength <= cacheBuffer.capacity()) {
                int offset = 4;

                byte[] typeLengthBytes = new byte[4];
                for (int i = 0; i < 4; ++i) {
                    typeLengthBytes[i] = cacheBuffer.get(offset + i);
                }
                int typeLength = ByteBuffer.wrap(typeLengthBytes).getInt();

                offset += 4;

                byte[] typeBytes = new byte[typeLength];
                for (int i = 0; i < typeLength; ++i) {
                    typeBytes[i] = cacheBuffer.get( offset + i);
                }
                String type = new String(typeBytes, StandardCharsets.UTF_8);

                offset += typeLength;

                byte[] dataLengthBytes = new byte[4];
                for (int i = 0; i < 4; ++i) {
                    dataLengthBytes[i] = cacheBuffer.get(offset + i);
                }
                int dataLength = ByteBuffer.wrap(dataLengthBytes).getInt();

                offset += 4;

                byte[] dataBytes = new byte[dataLength];
                for (int i = 0; i < dataLength; ++i) {
                    dataBytes[i] = cacheBuffer.get( offset + i);
                }
                String data = new String(dataBytes, StandardCharsets.UTF_8);

                offset += dataLength;

                int newCacheBufferSize = cacheBuffer.capacity() - offset;
                ByteBuffer newCacheBuffer = ByteBuffer.allocate(newCacheBufferSize);
                newCacheBuffer.put(cacheBuffer.array(), offset, cacheBuffer.capacity() - offset);
                cacheBuffer = newCacheBuffer;

                this.packets.add(new ReceivedPacket(type, data));
            } else {
                continueToRead = false;
            }
        }



    }

    List<ReceivedPacket> getPackets() {
        return packets;
    }
}
