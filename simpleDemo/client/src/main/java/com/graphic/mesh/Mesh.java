package com.graphic.mesh;

import java.util.List;

public class Mesh {

    private final int vaoId;

    private final List<Integer> vboIdList;

    private final int vertexCount;

    Mesh(int vaoId, List<Integer> vboIdList, int vertexCount) {
        this.vaoId = vaoId;
        this.vboIdList = vboIdList;
        this.vertexCount = vertexCount;
    }

    public int getVaoId() {
        return vaoId;
    }

    public List<Integer> getVboIdList() {
        return vboIdList;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
