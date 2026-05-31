package edu.up.mateosanchez.geometry;

import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.math.Ray;


public class Sphere implements Intersectable{
    public Vector3d center;
    double radius;
    double radiusSquared;

    public Sphere(Vector3d center, double radius) {
        this.center = center;
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    @Override
    public boolean intersect(Ray ray, double tMin, double tMax, HitRecord hitRecord) {
        double ocX = ray.origin.x - center.x;
        double ocY = ray.origin.y - center.y;
        double ocZ = ray.origin.z - center.z;
        double a = (ray.direction.x * ray.direction.x + ray.direction.y * ray.direction.y + ray.direction.z * ray.direction.z);
        double b = (ocX * ray.direction.x + ocY * ray.direction.y + ocZ * ray.direction.z) * 2.0;
        double c = (ocX * ocX + ocY * ocY + ocZ * ocZ) - radiusSquared;
        double d = b * b - (4 * a * c);
        if (d < 0){
            return false;
        }
        double t = (-b - Math.sqrt(d)) / (2.0 * a);
        if (t <= tMax && t >= tMin){
            hitRecord.t = t;
            ray.getPoint(t, hitRecord.point);
            double pcX = (hitRecord.point.x - center.x) / radius;
            double pcY = (hitRecord.point.y - center.y) / radius;
            double pcZ = (hitRecord.point.z - center.z) / radius;
            hitRecord.normal.set(pcX, pcY, pcZ);
            return true;
        }
        else{
            t = (-b + Math.sqrt(d)) / (2.0 * a);
            if (t <= tMax && t >= tMin){
                hitRecord.t = t;
                ray.getPoint(t, hitRecord.point);
                double pcX = (hitRecord.point.x - center.x) / radius;
                double pcY = (hitRecord.point.y - center.y) / radius;
                double pcZ = (hitRecord.point.z - center.z) / radius;
                hitRecord.normal.set(pcX, pcY, pcZ);
                return true;
            }
        }
        return false;
    }
}
