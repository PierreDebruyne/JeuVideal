package com.moveGame;

import com.graphic.scene.SceneItem;

public class MovePlayer {

    protected String name;
    protected SceneItem sceneItem;

    public MovePlayer(String name, SceneItem sceneItem) {
        this.name = name;
        this.sceneItem = sceneItem;
    }

    public String getName() {
        return name;
    }

    public SceneItem getSceneItem() {
        return sceneItem;
    }
}
