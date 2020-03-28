package com.game;

import org.joml.Vector3f;

public class PlayerMoved {

    protected String playerName;
    protected Vector3f position;
    protected Vector3f rotation;

    public PlayerMoved(String playerName, Vector3f position, Vector3f rotation) {
        this.playerName = playerName;
        this.position = position;
        this.rotation = rotation;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }
}
