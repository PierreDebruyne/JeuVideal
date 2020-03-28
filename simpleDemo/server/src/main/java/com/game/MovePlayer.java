package com.game;

import com.network.Client;
import org.joml.Vector3f;

public class MovePlayer {
    protected Client client;
    protected String name;
    protected Vector3f position;
    protected Vector3f rotation;

    public MovePlayer(Client client, String name, Vector3f position, Vector3f rotation) {
        this.client = client;
        this.name = name;
        this.position = position;
        this.rotation = rotation;
    }

    public Client getClient() {
        return client;
    }

    public String getName() {
        return name;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }
}
