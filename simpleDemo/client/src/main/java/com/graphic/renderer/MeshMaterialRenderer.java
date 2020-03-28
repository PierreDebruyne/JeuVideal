package com.graphic.renderer;

import com.MyClient;
import com.graphic.light.DirectionalLight;
import com.graphic.material.Material;
import com.graphic.mesh.Mesh;
import com.graphic.scene.Camera;
import com.graphic.scene.SceneItem;
import com.graphic.shaderProgram.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.InputStream;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class MeshMaterialRenderer {

    protected Map<Mesh, Map<Material, List<SceneItem>>> itemsByMaterialByMesh;
    protected Map<SceneItem, Mesh> meshesByItem;
    protected Map<SceneItem, Material> materialsByItem;

    protected ShaderProgram shaderProgram;

    public MeshMaterialRenderer() {
        this.itemsByMaterialByMesh = new HashMap<>();
        this.meshesByItem = new HashMap<>();
        this.materialsByItem = new HashMap<>();
    }

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(loadResource("/shaders/vertex.vs"));
        shaderProgram.createFragmentShader(loadResource("/shaders/fragment.fs"));
        shaderProgram.link();
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        // Create uniform for material
        shaderProgram.createMaterialUniform("material");
        // Create lighting related uniforms
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createDirectionalLightUniform("directionalLight");
    }

    public void linkMeshAndMaterialToItem(Mesh mesh, Material material, SceneItem item) {
        Map<Material, List<SceneItem>> itemsByMaterial = this.itemsByMaterialByMesh.computeIfAbsent(mesh, k -> new HashMap<>());
        List<SceneItem> items = itemsByMaterial.computeIfAbsent(material, k -> new LinkedList<>());
        items.add(item);
        this.meshesByItem.put(item, mesh);
        this.materialsByItem.put(item, material);
    }

    public void switchMesh(SceneItem item, Mesh newMesh) {
        Mesh currentMesh = this.meshesByItem.get(item);
        Material material = this.materialsByItem.get(item);
        Map<Material, List<SceneItem>> itemsByMaterial = this.itemsByMaterialByMesh.get(currentMesh);
        List<SceneItem> items = itemsByMaterial.get(material);
        items.remove(item);
        itemsByMaterial = this.itemsByMaterialByMesh.computeIfAbsent(newMesh, k -> new HashMap<>());
        items = itemsByMaterial.computeIfAbsent(material, k -> new LinkedList<>());
        items.add(item);
        this.meshesByItem.replace(item, newMesh);
    }

    public void switchMaterial(SceneItem item, Material newMaterial) {
        Mesh mesh = this.meshesByItem.get(item);
        Material currentMaterial = this.materialsByItem.get(item);
        Map<Material, List<SceneItem>> itemsByMaterial = this.itemsByMaterialByMesh.get(mesh);
        List<SceneItem> items = itemsByMaterial.get(currentMaterial);
        items.remove(item);
        items = itemsByMaterial.computeIfAbsent(newMaterial, k -> new LinkedList<>());
        items.add(item);
        this.materialsByItem.replace(item, newMaterial);
    }

    public void unlinkItem(SceneItem item) {
        Mesh mesh = this.meshesByItem.get(item);
        Material material = this.materialsByItem.get(item);
        Map<Material, List<SceneItem>> itemsByMaterial = this.itemsByMaterialByMesh.get(mesh);
        List<SceneItem> items = itemsByMaterial.get(material);
        items.remove(item);
        this.meshesByItem.remove(item);
        this.materialsByItem.remove(item);
    }

    public void render(Camera camera, Vector3f ambientLight, DirectionalLight directionalLight) {

        shaderProgram.bind();
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Matrix4f projectionMatrix = new Matrix4f().identity().setPerspective(camera.getFov(), camera.getAspect(), camera.getZ_near(), camera.getZ_far());
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", 10f);

        DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(camera.getViewMatrix());
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        shaderProgram.setUniform("directionalLight", currDirLight);

        this.itemsByMaterialByMesh.forEach((mesh, materials) -> {
            glBindVertexArray(mesh.getVaoId());
            materials.forEach((material, items) -> {

                shaderProgram.setUniform("material.ambient", material.getAmbientColour());
                shaderProgram.setUniform("material.diffuse", material.getDiffuseColour());
                shaderProgram.setUniform("material.specular", material.getSpecularColour());
                shaderProgram.setUniform("material.hasTexture", material.isTextured() ? 1 : 0);
                shaderProgram.setUniform("material.reflectance", material.getReflectance());

                if (material.getTexture() != null) {
                    // Activate firs texture bank
                    glActiveTexture(GL_TEXTURE0);
                    // Bind the texture
                    glBindTexture(GL_TEXTURE_2D, material.getTexture().getId());
                }
                items.forEach((item) -> {
                    shaderProgram.setUniform("modelViewMatrix", item.getNodeViewMatrix());
                    glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
                });

                glBindTexture(GL_TEXTURE_2D, 0);
            });
        });
        glBindVertexArray(0);
        shaderProgram.unbind();
    }

    public void destroy() {
        this.shaderProgram.cleanup();
    }

    private static String loadResource(String fileName) throws Exception {
        String result;
        try (InputStream in = Class.forName(MyClient.class.getName()).getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, "UTF-8")) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }
}
