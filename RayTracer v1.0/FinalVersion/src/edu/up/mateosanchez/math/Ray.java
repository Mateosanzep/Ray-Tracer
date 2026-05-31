package edu.up.mateosanchez.math;

public class Ray {
    public Vector3d origin;
    public Vector3d direction;
    
    public Ray(Vector3d origin, Vector3d direction) {
        this.origin = new Vector3d();
        this.direction = new Vector3d();
        this.origin.setVector(origin);
        this.direction.setVector(direction);
        this.direction.normalize();
    }

    public void setOrigin(Vector3d newOrigin) {
        this.origin.setVector(newOrigin);
    }

    public void setDirection(Vector3d newDirection) {
        this.direction.setVector(newDirection);
    }
    
    public void getPoint(double t, Vector3d result){
        double x = origin.x + direction.x * t;
        double y = origin.y + direction.y * t;
        double z = origin.z + direction.z * t;
        result.set(x, y, z);
    }
}
