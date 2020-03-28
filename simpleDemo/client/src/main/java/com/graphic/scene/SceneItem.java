package com.graphic.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SceneItem {
    protected String name;
    protected Vector3f position;
    protected Vector3f rotation;
    protected Vector3f scale;
    protected Matrix4f nodeViewMatrix;

    SceneItem(String name, Vector3f position, Vector3f rotation, Vector3f scale) {
        this.name = name;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
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

    public Vector3f getScale() {
        return scale;
    }

    public void setNodeViewMatrix(Matrix4f nodeViewMatrix) {
        this.nodeViewMatrix = nodeViewMatrix;
    }

    public Matrix4f getNodeViewMatrix() {
        return nodeViewMatrix;
    }
}
