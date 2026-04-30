package edu.up.mateo.raytracer;

public class Intersection {
    public float t;
    public Object3D object;
    public boolean hit;
    public Vector3D point;
    public Vector3D normal;
    
    public Intersection(float t, Object3D object, boolean hit) {
        this.t = t;
        this.object = object;
        this.hit = hit;
        this.point = null;
        this.normal = null;
    }

    public Intersection(float t, Object3D object, boolean hit, Vector3D point, Vector3D normal) {
        this.t = t;
        this.object = object;
        this.hit = hit;
        this.point = point;
        this.normal = normal;
    }
}
