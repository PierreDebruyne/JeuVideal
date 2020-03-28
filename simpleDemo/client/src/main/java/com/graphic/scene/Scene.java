package com.graphic.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class Scene {
    protected Map<String, SceneItem> items;

    public Scene() {
        this.items = new HashMap<>();
    }

    public SceneItem createItem(String name, Vector3f position, Vector3f rotation, Vector3f scale) throws Exception {
        SceneItem item = this.items.get(name);
        if (item != null) {
            throw new Exception("Impossible de créer l'item: ce nom '" + name + "' est déjà utilisé.");
        }
        item = new SceneItem(name, position, rotation, scale);
        this.items.put(name, item);
        return item;
    }

    public void removeItem(String name) throws Exception {
        SceneItem item = this.items.get(name);
        if (item == null) {
            throw new Exception("Impossible de supprimer l'item: cet item '" + name + "' n'existe pas.");
        }
        this.removeItem(item);
    }

    public void removeItem(SceneItem item) {
        this.items.remove(item.getName());
    }

    public void calcAllViewMatrix(Camera camera) {
        Matrix4f viewMatrix = new Matrix4f().identity()
                .rotate((float)Math.toRadians(camera.getRotation().x), new Vector3f(1, 0, 0))
                .rotate((float)Math.toRadians(camera.getRotation().y), new Vector3f(0, 1, 0))
                .rotate((float)Math.toRadians(camera.getRotation().z), new Vector3f(0, 0, 1));
        viewMatrix.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
        camera.setViewMatrix(viewMatrix);

        for (SceneItem item : this.items.values()) {
            Matrix4f modelViewMatrix = new Matrix4f();
            modelViewMatrix.identity().translate(item.getPosition()).
                    rotateX((float)Math.toRadians(-item.getRotation().x)).
                    rotateY((float)Math.toRadians(-item.getRotation().y)).
                    rotateZ((float)Math.toRadians(-item.getRotation().z)).
                    scale(item.getScale());
            Matrix4f viewCurr = new Matrix4f(viewMatrix);
            viewCurr.mul(modelViewMatrix);
            item.setNodeViewMatrix(viewCurr);
        }
    }
}
