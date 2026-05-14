package edu.up.mateo.raytracer;

import java.awt.Color;

public class Plane extends Object3D {
    public Vector3D point;
    public Vector3D normal;

    public Plane(Color color, Vector3D point, Vector3D normal) {
        super(color, point);
        this.point = point;
        this.normal = normal.Normalize();
    }

    @Override
    public Intersection intersect(Ray ray) {
        float eps = 1e-6f;
        float denom = normal.DotProduct(ray.D);
        if (Math.abs(denom) < eps) {
            return new Intersection(0f, this, false);
        }
        Vector3D p0l0 = point.VectorSubstract(ray.O);
        float t = p0l0.DotProduct(normal) / denom;
        if (t >= 0f) {
            Vector3D hitPoint = new Vector3D(
                ray.O.X + ray.D.X * t,
                ray.O.Y + ray.D.Y * t,
                ray.O.Z + ray.D.Z * t
            );
            return new Intersection(t, this, true, hitPoint, normal);
        }
        return new Intersection(0f, this, false);
    }

    @Override
    public Vector3D getNormal(Vector3D hitPoint) {
        return normal;
    }
}
