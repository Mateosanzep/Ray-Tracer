package edu.up.mateosanchez.geometry;

import edu.up.mateosanchez.acceleration.AABB;
import edu.up.mateosanchez.materials.Material;
import edu.up.mateosanchez.math.Ray;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.math.Vector2d;
import edu.up.mateosanchez.geometry.HitRecord;

public class Triangle implements Intersectable {
    public Vector3d v0, v1, v2;
    public Vector3d n0, n1, n2; // Normales por vértice para Smooth Shading
    public Material material; 
    public Vector2d uv0, uv1, uv2;
    private final AABB boundingBox;

    // Constructor principal con el parche de seguridad anticaídas integrado
    public Triangle(Vector3d v0, Vector3d v1, Vector3d v2, 
                    Vector3d n0, Vector3d n1, Vector3d n2, 
                    Vector2d uv0, Vector2d uv1, Vector2d uv2, Material material) {
        this.v0 = v0; this.v1 = v1; this.v2 = v2;
        this.uv0 = uv0; this.uv1 = uv1; this.uv2 = uv2;
        this.material = material;
        
        // --- PARCHE DE ROBUSTEZ ---
        // Si el .obj carece de normales (null), calculamos la normal geométrica plana por defecto
        if (n0 == null || n1 == null || n2 == null) {
            Vector3d flatNormal = new Vector3d();
            double e1x = v1.x - v0.x, e1y = v1.y - v0.y, e1z = v1.z - v0.z;
            double e2x = v2.x - v0.x, e2y = v2.y - v0.y, e2z = v2.z - v0.z;
            flatNormal.set(e1y * e2z - e1z * e2y, e1z * e2x - e1x * e2z, e1x * e2y - e1y * e2x);
            flatNormal.normalize();
            
            this.n0 = flatNormal;
            this.n1 = flatNormal;
            this.n2 = flatNormal;
        } else {
            // Si el archivo sí contenía normales válidas, las asignamos directamente
            this.n0 = n0; this.n1 = n1; this.n2 = n2;
        }

        this.boundingBox = new AABB();
        this.boundingBox.setFromTriangle(v0, v1, v2);
    }

    // Constructor de respaldo simplificado (reutiliza el constructor principal de arriba)
    public Triangle(Vector3d v0, Vector3d v1, Vector3d v2, Material material) {
        this(v0, v1, v2, null, null, null, null, null, null, material);
    }

    @Override
    public boolean intersect(Ray ray, double tMin, double tMax, HitRecord record) {
        double e1x = v1.x - v0.x, e1y = v1.y - v0.y, e1z = v1.z - v0.z;
        double e2x = v2.x - v0.x, e2y = v2.y - v0.y, e2z = v2.z - v0.z;

        double px = ray.direction.y * e2z - ray.direction.z * e2y;
        double py = ray.direction.z * e2x - ray.direction.x * e2z;
        double pz = ray.direction.x * e2y - ray.direction.y * e2x;

        double det = e1x * px + e1y * py + e1z * pz;

        if (det > -1e-6 && det < 1e-6) {
            return false;
        }

        double invDet = 1.0 / det;

        double tx = ray.origin.x - v0.x, ty = ray.origin.y - v0.y, tz = ray.origin.z - v0.z;

        double u = (tx * px + ty * py + tz * pz) * invDet;
        if (u < 0.0 || u > 1.0) return false;

        double qx = ty * e1z - tz * e1y;
        double qy = tz * e1x - tx * e1z;
        double qz = tx * e1y - ty * e1x;

        double v = (ray.direction.x * qx + ray.direction.y * qy + ray.direction.z * qz) * invDet;
        if (v < 0.0 || u + v > 1.0) return false;

        double t = (e2x * qx + e2y * qy + e2z * qz) * invDet;

        if (t > tMin && t < tMax) {
    double w = 1.0 - u - v;

    // Calculamos UV temporalmente para verificar transparencia
    double tempU = 0.0;
    double tempV = 0.0;

    if (uv0 != null) {
        tempU = w * uv0.x + u * uv1.x + v * uv2.x;
        tempV = w * uv0.y + u * uv1.y + v * uv2.y;
    }

    // Alpha Clipping
    if (this.material != null) {
        double alpha = 1.0;

        if (this.material.alphaMap != null &&
            this.material.alphaMap.isLoaded()) {

            alpha = this.material.alphaMap.getAlpha(tempU, tempV);

        } else if (this.material.texture != null &&
                   this.material.texture.isLoaded()) {

            alpha = this.material.texture.getAlpha(tempU, tempV);
        }

        if (alpha < 0.5) {
            return false;
        }
    }

    record.t = t;

    record.point.set(
        ray.origin.x + t * ray.direction.x,
        ray.origin.y + t * ray.direction.y,
        ray.origin.z + t * ray.direction.z
    );

    record.normal.set(
        w * n0.x + u * n1.x + v * n2.x,
        w * n0.y + u * n1.y + v * n2.y,
        w * n0.z + u * n1.z + v * n2.z
    );

    record.normal.normalize();

    record.u = tempU;
    record.v = tempV;
    record.material = this.material;

    return true;
}

return false;
    }

    @Override
    public AABB getBoundingBox() {
        return this.boundingBox;
    }
}