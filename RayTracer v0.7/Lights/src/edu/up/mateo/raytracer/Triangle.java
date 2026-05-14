package edu.up.mateo.raytracer;

import java.awt.Color;

public class Triangle extends Object3D {
    public Vector3D v0, v1, v2;
    public Vector3D n0, n1, n2;

    public Triangle(Color color, Vector3D v0, Vector3D v1, Vector3D v2) {
        super(color, v0); 
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.n0 = null;
        this.n1 = null;
        this.n2 = null;
    }

    public void setVertexNormals(Vector3D n0, Vector3D n1, Vector3D n2) {
        this.n0 = n0;
        this.n1 = n1;
        this.n2 = n2;
    }

    @Override
    public Vector3D getNormal(Vector3D hitPoint) {
        Vector3D face = v1.VectorSubstract(v0);
        Vector3D face2 = v0.VectorSubstract(v2);
        Vector3D normal = face.CrossProduct(face2).Normalize();
        return normal;
    }

    @Override
    public Intersection intersect(Ray ray) {
        // Möller–Trumbore intersection algorithm
        Vector3D edge1 = v1.VectorSubstract(v0);
        Vector3D edge2 = v2.VectorSubstract(v0);

        Vector3D P = ray.D.CrossProduct(edge2);
        float det = edge1.DotProduct(P);

        if (det > -0.000001f && det < 0.000001f) {
            return new Intersection(0, this, false);
        }

        float invDet = 1.0f / det;

        Vector3D T = ray.O.VectorSubstract(v0);
        float u = T.DotProduct(P) * invDet;
        if (u < 0.0f || u > 1.0f) {
            return new Intersection(0, this, false);
        }

        Vector3D Q = T.CrossProduct(edge1);
        float v = ray.D.DotProduct(Q) * invDet;
        if (v < 0.0f || (u + v) > 1.0f) {
            return new Intersection(0, this, false);
        }

        float t = edge2.DotProduct(Q) * invDet;
        if (t > 0.0001f) {
            Vector3D hitPoint = ray.O.VectorAdd(ray.D.ScalarMultiply(t));
            Vector3D faceNormal = edge1.CrossProduct(edge2).Normalize();
            if (faceNormal.DotProduct(ray.D) > 0.0f) {
                faceNormal = faceNormal.ScalarMultiply(-1f);
            }

            Vector3D finalNormal = faceNormal;
            if (this.n0 != null && this.n1 != null && this.n2 != null) {
                Vector3D term0 = this.n0.ScalarMultiply(1.0f - u - v);
                Vector3D term1 = this.n1.ScalarMultiply(u);
                Vector3D term2 = this.n2.ScalarMultiply(v);
                finalNormal = term0.VectorAdd(term1).VectorAdd(term2).Normalize();
                // ensure the normal faces opposite the ray direction
                if (finalNormal.DotProduct(ray.D) > 0.0f) {
                    finalNormal = finalNormal.ScalarMultiply(-1f);
                }
            }

            return new Intersection(t, this, true, hitPoint, finalNormal);
        }

        return new Intersection(0, this, false);
    }
}
