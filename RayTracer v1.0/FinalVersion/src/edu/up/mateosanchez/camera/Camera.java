package edu.up.mateosanchez.camera;

import edu.up.mateosanchez.math.Vector3d;

public class Camera {
    public Vector3d origin, horizontal, vertical, lowerLeftCorner;

    public Camera(Vector3d lookFrom, double fovDegrees, double aspectRatio, double focalLenght) {
        this.origin = new Vector3d();
        this.horizontal = new Vector3d();
        this.vertical = new Vector3d();
        this.lowerLeftCorner = new Vector3d();

        this.origin.setVector(lookFrom);
        double radians = Math.toRadians(fovDegrees);
        double viewportHeight = 2.0 * Math.tan(radians/2);
        double viewportWidth = aspectRatio * viewportHeight;
        this.horizontal.set(viewportWidth, 0.0, 0.0);
        this.vertical.set(0.0, viewportHeight, 0.0);
        lowerLeftCorner.x = origin.x - (horizontal.x/2) - (vertical.x/2);
        lowerLeftCorner.y = origin.y - (horizontal.y/2) - (vertical.y/2);
        lowerLeftCorner.z = origin.z - (horizontal.z/2) - (vertical.z/2) - focalLenght; 
    }

    public void getRayDirection(double u, double v, Vector3d result) {
        double targetX = lowerLeftCorner.x + (u * horizontal.x) + (v * vertical.x);
        double targetY = lowerLeftCorner.y + (u * horizontal.y) + (v * vertical.y);
        double targetZ = lowerLeftCorner.z + (u * horizontal.z) + (v * vertical.z);

        result.set(targetX - origin.x, targetY - origin.y, targetZ - origin.z);
    }

    
}
