package com.graphic.heightmap;

import com.graphic.material.MaterialMngr;
import com.graphic.mesh.Mesh;
import com.graphic.mesh.MeshMngr;
import com.graphic.texture.Texture;
import com.graphic.texture.TextureMngr;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.stb.STBImage.*;

public class HeightMapMngr {

    protected MeshMngr meshMngr;

    public HeightMapMngr(MeshMngr meshMngr) {
        this.meshMngr = meshMngr;
    }

    public Mesh loadHeightMap(float minY, float maxY, String heightMapFile, int textFactor) throws Exception {
        ByteBuffer buf = null;
        int width;
        int height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load(heightMapFile, w, h, channels, 4);
            if (buf == null) {
                throw new Exception("Image file [" + heightMapFile  + "] not loaded: " + stbi_failure_reason());
            }

            width = w.get();
            height = h.get();
        }


        /*BufferedImage originalImage = ImageIO.read(new File(TextureMngr.class.getResource(heightMapFile).toURI()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( originalImage, "png", baos );
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        ByteBuffer buf = ByteBuffer.wrap(imageInByte);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
*/
        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                // Create vertex for current position
                positions.add((float) col - (width / 2f)); // x
                positions.add(getHeight(minY, maxY, col, row, width, buf)); //y
                positions.add((float) row - (height / 2f)); //z

                // Set texture coordinates
                textCoords.add((float) col / textFactor);
                textCoords.add((float) row / textFactor);

                // Create indices
                if (col < width - 1 && row < height - 1) {
                    int leftTop = row * width + col;
                    int leftBottom = (row + 1) * width + col;
                    int rightBottom = (row + 1) * width + col + 1;
                    int rightTop = row * width + col + 1;

                    indices.add(leftTop);
                    indices.add(leftBottom);
                    indices.add(rightTop);

                    indices.add(rightTop);
                    indices.add(leftBottom);
                    indices.add(rightBottom);
                }
            }
        }
        float[] posArr = listToArray(positions);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();
        float[] textCoordsArr = listToArray(textCoords);
        float[] normalsArr = calcNormals(posArr, width, height);
        Mesh mesh = meshMngr.createMesh(posArr, textCoordsArr, normalsArr, indicesArr);

        stbi_image_free(buf);
        return mesh;
    }

    private float getHeight(float minY, float maxY, int x, int z, int width, ByteBuffer buffer) {
        byte r = buffer.get(x * 4 + 0 + z * 4 * width);
        byte g = buffer.get(x * 4 + 1 + z * 4 * width);
        byte b = buffer.get(x * 4 + 2 + z * 4 * width);
        byte a = buffer.get(x * 4 + 3 + z * 4 * width);

        int argb = ((0xFF & a) << 24) | ((0xFF & r) << 16)
                | ((0xFF & g) << 8) | (0xFF & b);
        return minY + Math.abs(maxY - minY) * ((float) -argb / (255 * 255 * 255));

    }

    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

    private float[] calcNormals(float[] posArr, int width, int height) {
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        Vector3f v4 = new Vector3f();
        Vector3f v12 = new Vector3f();
        Vector3f v23 = new Vector3f();
        Vector3f v34 = new Vector3f();
        Vector3f v41 = new Vector3f();
        List<Float> normals = new ArrayList<>();
        Vector3f normal = new Vector3f();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row > 0 && row < height - 1 && col > 0 && col < width - 1) {
                    int i0 = row * width * 3 + col * 3;
                    v0.x = posArr[i0];
                    v0.y = posArr[i0 + 1];
                    v0.z = posArr[i0 + 2];

                    int i1 = row * width * 3 + (col - 1) * 3;
                    v1.x = posArr[i1];
                    v1.y = posArr[i1 + 1];
                    v1.z = posArr[i1 + 2];
                    v1 = v1.sub(v0);

                    int i2 = (row + 1) * width * 3 + col * 3;
                    v2.x = posArr[i2];
                    v2.y = posArr[i2 + 1];
                    v2.z = posArr[i2 + 2];
                    v2 = v2.sub(v0);

                    int i3 = (row) * width * 3 + (col + 1) * 3;
                    v3.x = posArr[i3];
                    v3.y = posArr[i3 + 1];
                    v3.z = posArr[i3 + 2];
                    v3 = v3.sub(v0);

                    int i4 = (row - 1) * width * 3 + col * 3;
                    v4.x = posArr[i4];
                    v4.y = posArr[i4 + 1];
                    v4.z = posArr[i4 + 2];
                    v4 = v4.sub(v0);

                    v1.cross(v2, v12);
                    v12.normalize();

                    v2.cross(v3, v23);
                    v23.normalize();

                    v3.cross(v4, v34);
                    v34.normalize();

                    v4.cross(v1, v41);
                    v41.normalize();

                    normal = v12.add(v23).add(v34).add(v41);
                    normal.normalize();
                } else {
                    normal.x = 0;
                    normal.y = 1;
                    normal.z = 0;
                }
                normal.normalize();
                normals.add(normal.x);
                normals.add(normal.y);
                normals.add(normal.z);
            }
        }
        return listToArray(normals);
    }

}
