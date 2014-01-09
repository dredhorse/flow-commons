/*
 * This file is part of Flow Commons, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.commons;

import org.spout.math.matrix.Matrix4f;
import org.spout.math.vector.Vector3f;

/**
 * A view frustum defined by the camera view and projection. Used to check for intersection with object to determine whether or not they're visible.
 */
public class ViewFrustum {
    private final float[][] frustum = new float[6][4];

    /**
     * Updates the frustum to match the view and projection matrix.
     *
     * @param projection The projection matrix
     * @param view The view matrix
     */
    public void update(Matrix4f projection, Matrix4f view) {
        // http://www.crownandcutlass.com/features/technicaldetails/frustum.html
        final float[] clip = projection.mul(view).toArray(true);
        // Extract the numbers for the RIGHT plane
        frustum[0][0] = clip[3] - clip[0];
        frustum[0][1] = clip[7] - clip[4];
        frustum[0][2] = clip[11] - clip[8];
        frustum[0][3] = clip[15] - clip[12];
        // Extract the numbers for the LEFT plane
        frustum[1][0] = clip[3] + clip[0];
        frustum[1][1] = clip[7] + clip[4];
        frustum[1][2] = clip[11] + clip[8];
        frustum[1][3] = clip[15] + clip[12];
        // Extract the BOTTOM plane
        frustum[2][0] = clip[3] + clip[1];
        frustum[2][1] = clip[7] + clip[5];
        frustum[2][2] = clip[11] + clip[9];
        frustum[2][3] = clip[15] + clip[13];
        // Extract the TOP plane
        frustum[3][0] = clip[3] - clip[1];
        frustum[3][1] = clip[7] - clip[5];
        frustum[3][2] = clip[11] - clip[9];
        frustum[3][3] = clip[15] - clip[13];
        // Extract the FAR plane
        frustum[4][0] = clip[3] - clip[2];
        frustum[4][1] = clip[7] - clip[6];
        frustum[4][2] = clip[11] - clip[10];
        frustum[4][3] = clip[15] - clip[14];
        // Extract the NEAR plane
        frustum[5][0] = clip[3] + clip[2];
        frustum[5][1] = clip[7] + clip[6];
        frustum[5][2] = clip[11] + clip[10];
        frustum[5][3] = clip[15] + clip[14];
    }

    /**
     * Compute the distance between a point and the given plane.
     *
     * @param i The id of the plane
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @param z The z coordinate of the point
     * @return The distance
     */
    private float distance(int i, float x, float y, float z) {
        return frustum[i][0] * x + frustum[i][1] * y + frustum[i][2] * z + frustum[i][3];
    }

    /**
     * Checks if the frustum of this camera intersects the given cuboid vertices.
     *
     * @param vertices The cuboid local vertices to check the frustum against
     * @param position The position of the cuboid
     * @return Whether or not the frustum intersects the cuboid
     */
    public boolean intersectsCuboid(Vector3f[] vertices, Vector3f position) {
        return intersectsCuboid(vertices, position.getX(), position.getY(), position.getZ());
    }

    /**
     * Checks if the frustum of this camera intersects the given cuboid vertices.
     *
     * @param vertices The cuboid local vertices to check the frustum against
     * @param x The x coordinate of the cuboid position
     * @param y The y coordinate of the cuboid position
     * @param z The z coordinate of the cuboid position
     * @return Whether or not the frustum intersects the cuboid
     */
    public boolean intersectsCuboid(Vector3f[] vertices, float x, float y, float z) {
        if (vertices.length != 8) {
            throw new IllegalArgumentException("A cuboid has 8 vertices, not " + vertices.length);
        }
        planes:
        for (int i = 0; i < 6; i++) {
            for (Vector3f vertex : vertices) {
                if (distance(i, vertex.getX() + x, vertex.getY() + y, vertex.getZ() + z) > 0) {
                    continue planes;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Checks if the frustum contains the given point.
     *
     * @param point The point to check if the frustum contains
     * @return True if the frustum contains the point
     */
    public boolean contains(Vector3f point) {
        return contains(point.getX(), point.getY(), point.getZ());
    }

    /**
     * Checks if the frustum contains the given point.
     *
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @param z The z coordinate of the point
     * @return True if the frustum contains the point
     */
    public boolean contains(float x, float y, float z) {
        for (int i = 0; i < 6; i++) {
            if (distance(i, x, y, z) <= 0) {
                return false;
            }
        }
        return true;
    }
}
