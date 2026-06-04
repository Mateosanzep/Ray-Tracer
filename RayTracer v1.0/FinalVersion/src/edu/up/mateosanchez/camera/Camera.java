package edu.up.mateosanchez.camera;

import edu.up.mateosanchez.math.Vector3d;

public class Camera {
    public Vector3d origin, horizontal, vertical, lowerLeftCorner;

    // Camera constructor with lookAt, vUp, and focalLength
    public Camera(Vector3d lookFrom, Vector3d lookAt, Vector3d vUp, double fovDegrees, double aspectRatio, double focalLength) {
        // Initialize empty vectors
        this.origin = new Vector3d();
        this.horizontal = new Vector3d();
        this.vertical = new Vector3d();
        this.lowerLeftCorner = new Vector3d();

        // Set camera origin position
        this.origin.setVector(lookFrom);

        // Calculate viewport dimensions
        double radians = Math.toRadians(fovDegrees);
        double viewportHeight = 2.0 * Math.tan(radians / 2.0);
        double viewportWidth = aspectRatio * viewportHeight;

        // Calculate camera local axes (w, u, v)
        Vector3d w = new Vector3d(lookFrom.x - lookAt.x, lookFrom.y - lookAt.y, lookFrom.z - lookAt.z);
        w.normalize();

        Vector3d u = new Vector3d();
        vUp.crossProduct(w, u);
        u.normalize();

        Vector3d v = new Vector3d();
        w.crossProduct(u, v);

        // Define horizontal and vertical viewport vectors
        this.horizontal.set(u.x * viewportWidth, u.y * viewportWidth, u.z * viewportWidth);
        this.vertical.set(v.x * viewportHeight, v.y * viewportHeight, v.z * viewportHeight);

        // Calculate the lower-left corner position of the viewport
        this.lowerLeftCorner.set(
            this.origin.x - (this.horizontal.x / 2.0) - (this.vertical.x / 2.0) - (w.x * focalLength),
            this.origin.y - (this.horizontal.y / 2.0) - (this.vertical.y / 2.0) - (w.y * focalLength),
            this.origin.z - (this.horizontal.z / 2.0) - (this.vertical.z / 2.0) - (w.z * focalLength)
        );
    }

    // Calculate ray direction based on viewport coordinates
    public void getRayDirection(double u, double v, Vector3d result) {
        double targetX = lowerLeftCorner.x + (u * horizontal.x) + (v * vertical.x);
        double targetY = lowerLeftCorner.y + (u * horizontal.y) + (v * vertical.y);
        double targetZ = lowerLeftCorner.z + (u * horizontal.z) + (v * vertical.z);

        result.set(targetX - origin.x, targetY - origin.y, targetZ - origin.z);
    }
}