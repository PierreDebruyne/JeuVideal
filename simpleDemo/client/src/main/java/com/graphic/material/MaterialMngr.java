package com.graphic.material;

import com.graphic.texture.Texture;
import org.joml.Vector4f;

import java.util.LinkedList;
import java.util.List;

public class MaterialMngr {

    protected List<Material> materials;

    public MaterialMngr() {
        this.materials = new LinkedList<>();
    }

    public Material createMaterial(Vector4f color, float reflectance) {
        Material material = new Material(color, reflectance);
        this.materials.add(material);
        return material;
    }

    public Material createMaterial(Texture texture, float reflectance) {
        Material material = new Material(texture, reflectance);
        this.materials.add(material);
        return material;
    }

    public void removeMaterial(Material material) {
        this.materials.remove(material);
    }
}
