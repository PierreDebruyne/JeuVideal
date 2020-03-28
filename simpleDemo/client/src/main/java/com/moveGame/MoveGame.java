package com.moveGame;

import com.google.gson.Gson;
import com.graphic.Window;
import com.graphic.material.Material;
import com.graphic.mesh.Mesh;
import com.graphic.renderer.MeshMaterialRenderer;
import com.graphic.scene.Camera;
import com.graphic.scene.Scene;
import com.graphic.scene.SceneItem;
import com.network.INetworkListener;
import com.network.Network;
import com.network.Packet;
import com.network.ReceivedPacket;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MoveGame implements INetworkListener {

    protected Window window;
    protected Mesh bunnyMesh;
    protected Material playerMaterial;
    protected Scene scene;
    protected MeshMaterialRenderer renderer;
    protected Camera camera;
    protected Network network;

    protected CameraController cameraController;
    protected MouseInput mouseInput;

    protected Map<String, MovePlayer> players;

    public MoveGame(Window window, Mesh bunnyMesh, Material playerMaterial, Scene scene, MeshMaterialRenderer renderer, Camera camera, Network network) {
        this.window = window;
        this.bunnyMesh = bunnyMesh;
        this.playerMaterial = playerMaterial;
        this.scene = scene;
        this.renderer = renderer;
        this.camera = camera;
        this.network = network;
        this.players = new HashMap<>();
    }

    public void init() {
        mouseInput = new MouseInput();
        mouseInput.init(window);
        cameraController = new CameraController(camera, mouseInput, window, 0.2f, 0.05f);
        network.addListener(this);
    }

    public void input() {
        mouseInput.input(window);
        cameraController.input();
    }

    public void onReceive(ReceivedPacket packet) {
        Gson gson = new Gson();
        PlayerMoved playerMoved = gson.fromJson(packet.getDataJson(), PlayerMoved.class);
        MovePlayer player = this.players.get(playerMoved.getPlayerName());
        Vector3f position = new Vector3f(playerMoved.getPosition());
        position.y -= 1;
        player.getSceneItem().getPosition().set(position);
        Vector3f rotation = new Vector3f(playerMoved.getRotation());
        rotation.y += 145;
        player.getSceneItem().getRotation().set(rotation);
    }

    public void onPlayerConnected(ReceivedPacket packet) throws Exception {
        Gson gson = new Gson();
        String playerName = gson.fromJson(packet.getDataJson(), String.class);
        SceneItem playerItem = scene.createItem(playerName, new Vector3f(0,0,0), new Vector3f(0,0,0), new Vector3f(1,1,1));
        renderer.linkMeshAndMaterialToItem(bunnyMesh, playerMaterial, playerItem);
        MovePlayer player = new MovePlayer(playerName, playerItem);
        this.players.put(playerName, player);
    }

    public void onPlayerDisconnected(ReceivedPacket packet) {
        Gson gson = new Gson();
        String playerName = gson.fromJson(packet.getDataJson(), String.class);
        MovePlayer player = this.players.remove(playerName);
        if (player != null) {
            renderer.unlinkItem(player.getSceneItem());
            scene.removeItem(player.getSceneItem());
        }
    }

    public void update() throws Exception {
        cameraController.update();

        if (cameraController.haveMoved()) {
            network.send(new Packet("move", camera.getPosition()));
        }
        if (cameraController.haveRotated()) {
            network.send(new Packet("rotate", camera.getRotation()));
        }
    }

    @Override
    public void onConnected() {
        try {
            network.send(new Packet("move", camera.getPosition()));
            network.send(new Packet("rotate", camera.getRotation()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnected() {
        for (MovePlayer player : players.values()) {
            renderer.unlinkItem(player.getSceneItem());
            scene.removeItem(player.getSceneItem());
        }
        players.clear();
    }
}
