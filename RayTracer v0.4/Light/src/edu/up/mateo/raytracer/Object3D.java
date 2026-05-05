package edu.up.mateo.raytracer;
import java.awt.Color;

public abstract class Object3D {
    public Color color;
    public Vector3D center;

    public Object3D(Color color, Vector3D center) {
        this.color = color;
        this.center = center;
    }
    
    public abstract Intersection intersect(Ray ray);
    public abstract Vector3D getNormal(Vector3D hitPoint);
}
