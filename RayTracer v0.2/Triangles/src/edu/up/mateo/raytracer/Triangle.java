package edu.up.mateo.raytracer;

import java.awt.Color;

public class Triangle extends Object3D {
    public Vector3D v0, v1, v2;

    public Triangle(Color color, Vector3D v0, Vector3D v1, Vector3D v2) {
        super(color, v0); 
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public Intersection intersect(Ray ray) {
        Vector3D v1v0 = v1.VectorSubstract(v0);
        Vector3D v2v0 = v2.VectorSubstract(v0);

        Vector3D P = ray.D.CrossProduct(v1v0);
        float determinant = v2v0.DotProduct(P);

        if (determinant > -0.0001f && determinant < 0.0001f) {
            return new Intersection(0, this, false);
        }

        float invDet = 1.0f / determinant;

        Vector3D T = ray.O.VectorSubstract(v0);
        float u = invDet * T.DotProduct(P);

        if (u < 0 || u > 1) {
            return new Intersection(0, this, false);
        }

        Vector3D Q = T.CrossProduct(v2v0);
        float v = invDet * ray.D.DotProduct(Q);

        if (v < 0 || (u + v) > 1.0f) {
            return new Intersection(0, this, false);
        }

        float t = invDet * Q.DotProduct(v1v0);
        if (t > 0.0001f) {
            return new Intersection(t, this, true);
        }

        return new Intersection(0, this, false);
    }
}
