package com.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Network {

    protected String hostIp;
    protected int hostPort;

    protected SocketChannel channel;
    protected Selector selector;
    protected PacketReader packetReader;
    protected List<Packet> toSend;
    protected List<INetworkListener> listeners;
    protected boolean connected = false;

    public Network() {
        this.toSend = new LinkedList<>();
        this.listeners = new LinkedList<>();
    }

    public void init() throws Exception {

        selector = Selector.open();
    }

    public void read() throws Exception {
        if (this.isConnected()) {
            selector.selectNow();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey myKey = iterator.next();
                if (myKey.isReadable()) {
                    try {
                        this.packetReader.readPackets();
                    } catch (IOException e) {
                        System.out.println("Connection au serveur perdue.");
                        disconnect();
                    }
                }
                iterator.remove();
            }
        }

    }

    public void write() {
        if (!this.toSend.isEmpty() && this.isConnected()) {
            try {
                for (Packet packet : this.toSend) {
                    ByteBuffer buffer = packet.toByteBuffer();
                    buffer.position(0);
                    channel.write(buffer);
                }
                this.toSend.clear();
            } catch (IOException e) {
                System.out.println("Connection au serveur perdue.");
                disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void destroy() {
       disconnect();
    }

    public void reconnect() {
        this.connect(this.hostIp, this.hostPort);
    }

    public void connect(String hostIp, int hostPort) {
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        Thread daemonThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    channel = SocketChannel.open(new InetSocketAddress(hostIp, hostPort));
                    channel.configureBlocking(false);
                    int ops = channel.validOps();
                    SelectionKey selectKey = channel.register(selector, ops, null);
                    packetReader = new PacketReader(channel);
                    System.out.println("Connecté au serveur: " + hostIp + ":" + hostPort);
                    connected = true;
                    for (INetworkListener listener : listeners) {
                        listener.onConnected();
                    }
                } catch (IOException e) {
                    System.out.println("Impossible de se connecter au serveur: " + hostIp + ":" + hostPort);
                    channel = null;
                }
            }
        }, "Demon");

        daemonThread.setDaemon(true);
        daemonThread.start();

    }

    public void disconnect() {
        this.connected = false;
        this.toSend.clear();
        for (INetworkListener listener : listeners) {
            listener.onDisconnected();
        }
        try {
            if (channel != null) {
                channel.close();
                channel = null;
            }
        } catch (IOException e) { }
    }

    public boolean isConnected() {
        return connected;
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

    public void addListener(INetworkListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(INetworkListener listener) {
        this.listeners.remove(listener);
    }
}
