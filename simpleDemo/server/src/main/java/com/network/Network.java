package com.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Network {

    private final int READ_SIZE = 256;

    protected int port;
    protected ServerSocketChannel channel;
    protected Selector selector;
    protected Map<SocketChannel, Client> clients;
    protected List<INetworkListener> listeners;

    public void init(int port) throws Exception {
        this.port = port;
        clients = new HashMap<>();
        listeners = new LinkedList<>();
        selector = Selector.open();
        channel = ServerSocketChannel.open();
        channel.socket().bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
        int ops = channel.validOps();
        SelectionKey selectKey = channel.register(selector, ops, null);

    }

    public void read() throws Exception {

        selector.selectNow();
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {

            SelectionKey myKey = iterator.next();
            if (myKey.isAcceptable()) {
                SocketChannel clientChannel = channel.accept();

                // Adjusts this channel's blocking mode to false
                clientChannel.configureBlocking(false);

                // Operation-set bit for read operations
                clientChannel.register(selector, SelectionKey.OP_READ);

                this.createClient(clientChannel);

                // Tests whether this key's channel is ready for reading
            } else if (myKey.isReadable()) {
                SocketChannel clientChannel = (SocketChannel) myKey.channel();
                try {
                    Client client = clients.get(clientChannel);
                    client.read();
                } catch (IOException e) {
                    this.removeClient(clientChannel);
                }

            }
            iterator.remove();
        }
    }

    public void write() {
        this.clients.forEach((socketChannel, client) -> {
            if (!client.toSend.isEmpty()) {
                try {
                    for (Packet packet : client.toSend) {
                        ByteBuffer buffer = packet.toByteBuffer();
                        buffer.position(0);
                        client.getSocketChannel().write(buffer);
                    }
                    client.toSend.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void destroy() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createClient(SocketChannel socketChannel) throws IOException {
        Client client = new Client(socketChannel);
        clients.put(socketChannel, client);
        System.out.println("Connexion d'un client: " + client.getAddr());
        for (INetworkListener listener : listeners) {
            listener.onClientConnected(client);
        }
    }

    private void removeClient(SocketChannel socketChannel) throws IOException {
        Client client = clients.remove(socketChannel);
        client.getSocketChannel().close();
        System.out.println("Déconnexion d'un client: " + client.getAddr());
        for (INetworkListener listener : listeners) {
            listener.onClientDisconnected(client);
        }
    }

    public Collection<Client> getClients() {
        return this.clients.values();
    }

    public void addListener(INetworkListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(INetworkListener listener) {
        this.listeners.remove(listener);
    }
}
