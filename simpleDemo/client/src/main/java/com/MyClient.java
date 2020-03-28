package com;

import com.cmd.Command;
import com.google.gson.Gson;
import com.moveGame.CameraController;
import com.moveGame.MouseInput;
import com.graphic.Window;
import com.graphic.heightmap.HeightMapMngr;
import com.graphic.light.DirectionalLight;
import com.graphic.material.Material;
import com.graphic.material.MaterialMngr;
import com.graphic.mesh.Mesh;
import com.graphic.mesh.MeshMngr;
import com.graphic.scene.Camera;
import com.graphic.renderer.MeshMaterialRenderer;
import com.graphic.scene.Scene;
import com.graphic.scene.SceneItem;
import com.graphic.texture.Texture;
import com.graphic.texture.TextureMngr;
import com.moveGame.MoveGame;
import com.network.Network;
import com.network.Packet;
import com.network.ReceivedPacket;
import com.network.ServerInfos;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.io.FileReader;
import java.io.InputStream;
import java.util.Scanner;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.opengl.GL11.*;

public class MyClient extends ClientLogic {

    protected Window window;

    //private final String resourceFolder = "C:\\Users\\Pierre\\Desktop\\Projects\\JeuVideal\\master\\resources\\";
    private final String resourceFolder = "resources\\";

    protected MeshMngr meshMngr;
    protected TextureMngr textureMngr;
    protected MaterialMngr materialMngr;
    protected HeightMapMngr heightMapMngr;

    protected Mesh mapMesh;
    protected Texture mapTexture;
    protected Material mapMaterial;

    protected Mesh bunnyMesh;
    protected Material bunnyMaterial;
    protected Material playerMaterial;

    protected Scene scene;
    protected MeshMaterialRenderer renderer;

    protected Camera camera;
    protected Vector3f ambientLight;
    protected DirectionalLight directionalLight;
    protected SceneItem mapItem;
    protected SceneItem bunnyItem;

    protected Network network;
    protected Command command;

    protected int updateCount = 1;
    protected double lastTime;
    protected int fps;

    protected MoveGame game;

    @Override
    protected void init() throws Exception {

        // INIT MANAGERS

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        window = new Window("MyClient", 800,600, false);
        window.init();

        meshMngr = new MeshMngr();
        textureMngr = new TextureMngr();
        materialMngr = new MaterialMngr();
        scene = new Scene();
        renderer = new MeshMaterialRenderer();
        renderer.init();

        heightMapMngr = new HeightMapMngr(meshMngr);
        mapMesh = heightMapMngr.loadHeightMap(0f,20f, resourceFolder + "heightmaps/heightmap2.png", 2);
        mapTexture = textureMngr.loadTexture(resourceFolder + "textures/grass.jpg");
        mapMaterial = materialMngr.createMaterial(mapTexture, 0.0f);

        bunnyMesh = meshMngr.loadObj(resourceFolder + "models/bunny.obj");
        bunnyMaterial = materialMngr.createMaterial(new Vector4f(1f,1f,1f,1f), 0.5f);
        playerMaterial = materialMngr.createMaterial(new Vector4f(0.5f,0.5f,1f,1f), 0.5f);


        // CREATE SCENE

        camera = new Camera(new Vector3f(0,0,0), new Vector3f(0,0,0), (float) Math.toRadians(60.0f), (float) window.getWidth() / (float) window.getHeight(), 0.01f, 1000f);
        ambientLight = new Vector3f(0.5f,0.5f,0.5f);
        directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(-1, 0, 0), 0.5f);
        mapItem = scene.createItem("map", new Vector3f(0,-10,0), new Vector3f(0,0,0), new Vector3f(0.5f,1,0.5f));
        renderer.linkMeshAndMaterialToItem(mapMesh, mapMaterial, mapItem);
        bunnyItem = scene.createItem("bunny", new Vector3f(0,10,0), new Vector3f(0,0,0), new Vector3f(1,1,1));
        renderer.linkMeshAndMaterialToItem(bunnyMesh, bunnyMaterial, bunnyItem);

        network = new Network();
        command = new Command(this);
        network.init();

        game = new MoveGame(window, bunnyMesh, playerMaterial, scene, renderer, camera, network);
        game.init();

        Gson gson = new Gson();
        ServerInfos serverInfos = gson.fromJson(new FileReader("server.json"), ServerInfos.class);
        network.connect(serverInfos.getIp(), serverInfos.getPort());

        lastTime = System.nanoTime() / 1000_000_000.0;
        fps = 0;


    }

    @Override
    protected void update(double timeElapsed) throws Exception {
        // READ
        network.read();

        // INPUT
        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            this.stop();
        }
        if (window.windowShouldClose()) {
            this.stop();
        }
        command.input();
        game.input();

        // UPDATE

        while (network.isConnected() && network.hasInput()) {
            ReceivedPacket packet = network.readInput();

            if (packet.getType().equals("playerMove")) {
                game.onReceive(packet);
            } else if (packet.getType().equals("playerConnected")) {
                game.onPlayerConnected(packet);
            } else if (packet.getType().equals("playerDisconnected")) {
                game.onPlayerDisconnected(packet);
            }
        }
        if (updateCount % 1000 == 0) {
            if (network.isConnected()) {
                network.send(new Packet("ping", "Bonjour serveur!"));
            } else {
                network.reconnect();
            }
        }
        updateCount++;

        bunnyItem.getRotation().y -= 0.1f;
        game.update();

        double time = System.nanoTime() / 1000_000_000.0;
        fps++;
        if (time - lastTime >= 1.0) {
            window.setTitle("MyClient | " + fps + " FPS");
            fps = 0;
            lastTime = time;
        }

        // WRITE
        network.write();

        //RENDER

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            camera.setAspect((float) window.getWidth() / (float) window.getHeight());
            window.setResized(false);
        }

        if (window.getWidth() > 0 && window.getHeight() > 0) {
            scene.calcAllViewMatrix(camera);
            renderer.render(camera, ambientLight, directionalLight);

        }
        window.update();
    }

    @Override
    protected void destroy() {
        window.setVisible(false);
        renderer.destroy();
        meshMngr.destroy();
        textureMngr.destroy();
        command.destroy();
        network.destroy();
    }
}
