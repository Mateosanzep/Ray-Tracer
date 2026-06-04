package edu.up.mateosanchez.geometry;

import edu.up.mateosanchez.acceleration.AABB; 
import edu.up.mateosanchez.materials.Material;
import edu.up.mateosanchez.math.Ray;
import edu.up.mateosanchez.math.Vector3d;

public class Sphere implements Intersectable {
    public Vector3d center;
    public double radius;
    public double radiusSquared;
    public Material material;
    private final AABB boundingBox; 

    public Sphere(Vector3d center, double radius, Material material) {
        this.center = center;
        this.radius = radius;
        this.radiusSquared = radius * radius;
        this.material = material;

        // Initialize and calculate bounding box limits based on radius
        this.boundingBox = new AABB();
        this.boundingBox.min.set(center.x - radius, center.y - radius, center.z - radius);
        this.boundingBox.max.set(center.x + radius, center.y + radius, center.z + radius);
    }

    @Override
    public AABB getBoundingBox() {
        return this.boundingBox; 
    }

    @Override
    public boolean intersect(Ray ray, double tMin, double tMax, HitRecord hitRecord) {
        double ocX = ray.origin.x - center.x;
        double ocY = ray.origin.y - center.y;
        double ocZ = ray.origin.z - center.z;
        double a = (ray.direction.x * ray.direction.x + ray.direction.y * ray.direction.y + ray.direction.z * ray.direction.z);
        double b = (ocX * ray.direction.x + ocY * ray.direction.y + ocZ * ray.direction.z) * 2.0;
        double c = (ocX * ocX + ocY * ocY + ocZ * ocZ) - radiusSquared;
        
        // Discriminant calculation
        double d = b * b - (4 * a * c);
        if (d < 0){
            return false;
        }
        
        // Check closest intersection hit
        double t = (-b - Math.sqrt(d)) / (2.0 * a);
        if (t <= tMax && t >= tMin){
            hitRecord.t = t;
            ray.getPoint(t, hitRecord.point);
            double pcX = (hitRecord.point.x - center.x) / radius;
            double pcY = (hitRecord.point.y - center.y) / radius;
            double pcZ = (hitRecord.point.z - center.z) / radius;
            hitRecord.normal.set(pcX, pcY, pcZ);
            hitRecord.material = this.material;
            return true;
        }
        else{
            // Check furthest intersection hit
            t = (-b + Math.sqrt(d)) / (2.0 * a);
            if (t <= tMax && t >= tMin){
                hitRecord.t = t;
                ray.getPoint(t, hitRecord.point);
                double pcX = (hitRecord.point.x - center.x) / radius;
                double pcY = (hitRecord.point.y - center.y) / radius;
                double pcZ = (hitRecord.point.z - center.z) / radius;
                hitRecord.normal.set(pcX, pcY, pcZ);
                hitRecord.material = this.material;
                return true;
            }
        }
        return false;
    }
}