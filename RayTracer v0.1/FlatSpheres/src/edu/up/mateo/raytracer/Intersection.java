package edu.up.mateo.raytracer;

public class Intersection {
    public float t;
    public Object3D object;
    public boolean hit;
    
    public Intersection(float t, Object3D object, boolean hit) {
        this.t = t;
        this.object = object;
        this.hit = hit;
    }
}
