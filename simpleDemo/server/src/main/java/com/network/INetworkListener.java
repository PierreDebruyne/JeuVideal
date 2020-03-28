package com.network;


public interface INetworkListener {

    void onClientConnected(Client client);
    void onClientDisconnected(Client client);
}
