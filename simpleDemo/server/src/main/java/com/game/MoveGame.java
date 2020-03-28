package com.game;

import com.google.gson.Gson;
import com.network.Client;
import com.network.INetworkListener;
import com.network.Packet;
import com.network.ReceivedPacket;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MoveGame implements INetworkListener {

    protected int i = 1;
    protected Map<String, MovePlayer> playersByName;
    protected Map<Client, MovePlayer> playersByClient;

    public MoveGame() {
        this.playersByName = new HashMap<>();
        this.playersByClient = new HashMap<>();
    }

    public void onMove(Client client, ReceivedPacket packet) {
        MovePlayer player = this.playersByClient.get(client);
        Gson gson = new Gson();
        Vector3f newPosition = gson.fromJson(packet.getDataJson(), Vector3f.class);
        player.getPosition().set(newPosition);
        for (MovePlayer otherPlayer : playersByName.values()) {
            if (otherPlayer != player) {
                try {
                    otherPlayer.getClient().send(new Packet("playerMove", new PlayerMoved(player.getName(), player.getPosition(), player.getRotation())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void onRotate(Client client, ReceivedPacket packet) {
        MovePlayer player = this.playersByClient.get(client);
        Gson gson = new Gson();
        Vector3f newRotation = gson.fromJson(packet.getDataJson(), Vector3f.class);
        player.getRotation().set(newRotation);
        for (MovePlayer otherPlayer : playersByName.values()) {
            if (otherPlayer != player) {
                try {
                    otherPlayer.getClient().send(new Packet("playerMove", new PlayerMoved(player.getName(), player.getPosition(), player.getRotation())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onClientConnected(Client client) {
        String playerName = "player" + i;
        MovePlayer player = new MovePlayer(client, playerName, new Vector3f(0,0,0), new Vector3f(0,0,0));

        for (MovePlayer otherPlayer : playersByName.values()) {
            try {
                otherPlayer.getClient().send(new Packet("playerConnected", playerName));
                player.getClient().send(new Packet("playerConnected", otherPlayer.getName()));
                player.getClient().send(new Packet("playerMove", new PlayerMoved(otherPlayer.getName(), otherPlayer.getPosition(),otherPlayer.getRotation())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.playersByName.put(playerName, player);
        this.playersByClient.put(client, player);
        i++;
        System.out.println("Nouveau joueur: " + playerName);
    }

    @Override
    public void onClientDisconnected(Client client) {
        MovePlayer player = this.playersByClient.remove(client);
        this.playersByName.remove(player.getName());
        for (MovePlayer otherPlayer : playersByName.values()) {
            try {
                otherPlayer.getClient().send(new Packet("playerDisconnected", player.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Le joueur: " + player.getName() + " est parti.");
    }
}
