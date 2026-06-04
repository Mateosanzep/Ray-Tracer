package edu.up.mateosanchez.geometry;

import edu.up.mateosanchez.acceleration.AABB; // Nueva importación obligatoria
import edu.up.mateosanchez.materials.Material;
import edu.up.mateosanchez.math.Ray;
import edu.up.mateosanchez.math.Vector3d;

public class Sphere implements Intersectable {
    public Vector3d center;
    public double radius;
    public double radiusSquared;
    public Material material;
    private final AABB boundingBox; // Nueva variable para la caja protectora

    public Sphere(Vector3d center, double radius, Material material) {
        this.center = center;
        this.radius = radius;
        this.radiusSquared = radius * radius;
        this.material = material;

        // Inicializamos la caja y calculamos sus límites usando el radio
        this.boundingBox = new AABB();
        this.boundingBox.min.set(center.x - radius, center.y - radius, center.z - radius);
        this.boundingBox.max.set(center.x + radius, center.y + radius, center.z + radius);
    }

    @Override
    public AABB getBoundingBox() {
        return this.boundingBox; // Cumplimos con el contrato de la interfaz
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
            hitRecord.material = this.material;
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
                hitRecord.material = this.material;
                return true;
            }
        }
        return false;
    }
}