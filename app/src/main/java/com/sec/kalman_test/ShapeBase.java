package com.sec.kalman_test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

abstract class ShapeBase {

    public FloatBuffer mVertexBuffer;
    public FloatBuffer mColorBuffer;
    public ByteBuffer mIndexBuffer;
    public int mNumOfIndex;
    public float mRotateAngle;

    abstract void draw(GL10 gl);

    protected void setVertices(float[] vertex) {
        // a float is 4 bytes, therefore we multiply the number if
        // vertices with 4.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertex.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertex);
        mVertexBuffer.position(0);
    }

    protected void setIndices(byte[] index) {
        // short is 2 bytes, therefore we multiply the number if
        // vertices with 2.
        mIndexBuffer = ByteBuffer.allocateDirect(index.length);
        mIndexBuffer.put(index);
        mIndexBuffer.position(0);
        mNumOfIndex = index.length;
    }

    protected void setColors(float[] color) {
        // float has 4 bytes.
        ByteBuffer cbb = ByteBuffer.allocateDirect(color.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer();
        mColorBuffer.put(color);
        mColorBuffer.position(0);
    }
}
