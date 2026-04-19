package edu.up.mateo.raytracer;

import java.awt.Color;

public class Sphere extends Object3D{
    public float R;

    public Sphere(Color color, Vector3D center, float r) {
        super(color, center);
        this.R = r;
    }

    @Override
    public Intersection intersect(Ray ray) {
        boolean hit;
        float t = 0;
        Object3D object = null;
        Vector3D L = center.VectorSubstract(ray.O);
        float tca = L.DotProduct(ray.D);
        float d = L.DotProduct(L)-tca*tca;
        if (d > R*R) {
            hit = false;
        } else{
            float thc = (float)Math.sqrt(R*R-d);
            float t0 = tca - thc;
            float t1 = tca + thc;
            hit = true;
            if (t0 > 0){
                t = t0;
            } else if(t0 <= 0 && t1 > 0){
                t = t1;
            }else {
                hit = false;
            }
            object = this;
        }

        return new Intersection(t, object, hit);
    }
}
