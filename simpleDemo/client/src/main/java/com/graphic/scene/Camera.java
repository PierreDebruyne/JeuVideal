package com.graphic.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    protected Vector3f position;
    protected Vector3f rotation;
    protected float fov;
    protected float aspect;
    protected float Z_near;
    protected float Z_far;
    protected Matrix4f viewMatrix;

    public Camera(Vector3f position, Vector3f rotation, float fov, float aspect, float z_near, float z_far) {
        this.position = position;
        this.rotation = rotation;
        this.fov = fov;
        this.aspect = aspect;
        Z_near = z_near;
        Z_far = z_far;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public float getFov() {
        return fov;
    }

    public float getAspect() {
        return aspect;
    }

    public float getZ_near() {
        return Z_near;
    }

    public float getZ_far() {
        return Z_far;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public void setAspect(float aspect) {
        this.aspect = aspect;
    }

    public void setZ_near(float z_near) {
        Z_near = z_near;
    }

    public void setZ_far(float z_far) {
        Z_far = z_far;
    }

    public void setViewMatrix(Matrix4f viewMatrix) {
        this.viewMatrix = viewMatrix;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }
}
