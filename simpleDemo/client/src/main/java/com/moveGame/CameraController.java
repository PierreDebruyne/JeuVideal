package com.moveGame;

import com.graphic.Window;
import com.graphic.scene.Camera;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController {


    protected Camera camera;
    protected MouseInput mouseInput;
    protected Window window;

    protected float mouseSensitivity;
    protected float moveSpeed;

    protected Vector3f cameraInc;
    protected Vector2f cameraRot;

    public CameraController(Camera camera, MouseInput mouseInput, Window window,float mouseSensitivity, float moveSpeed) {
        this.camera = camera;
        this.mouseInput = mouseInput;
        this.window = window;
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.mouseSensitivity = mouseSensitivity;
        this.moveSpeed = moveSpeed;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setMouseInput(MouseInput mouseInput) {
        this.mouseInput = mouseInput;
    }

    public void input() {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            cameraInc.y = 1;
        }
        cameraRot = mouseInput.getDisplVec();
    }

    public void update() {
        if (mouseInput.isRightButtonPressed()) {
            camera.getRotation().x += cameraRot.x * mouseSensitivity;

            if (camera.getRotation().x > 90) {
                camera.getRotation().x = 90;
            } else if (camera.getRotation().x < -90) {
                camera.getRotation().x = -90;
            }
            camera.getRotation().y += cameraRot.y * mouseSensitivity;
            camera.getRotation().z += 0;
        }

        // Update camera position
        float moveX = cameraInc.x * moveSpeed;
        float moveY = cameraInc.y * moveSpeed;
        float moveZ = cameraInc.z * moveSpeed;
        if ( moveZ != 0 ) {
            camera.getPosition().x += (float)Math.sin(Math.toRadians(camera.getRotation().y)) * -1.0f * moveZ;
            camera.getPosition().z += (float)Math.cos(Math.toRadians(camera.getRotation().y)) * moveZ;
        }
        if ( moveX != 0) {
            camera.getPosition().x += (float)Math.sin(Math.toRadians(camera.getRotation().y - 90)) * -1.0f * moveX;
            camera.getPosition().z += (float)Math.cos(Math.toRadians(camera.getRotation().y - 90)) * moveX;
        }
        camera.getPosition().y += moveY;
    }

    public boolean haveMoved() {
        return !cameraInc.equals(0,0,0);
    }

    public boolean haveRotated() {
        return !cameraRot.equals(0,0);
    }
}
