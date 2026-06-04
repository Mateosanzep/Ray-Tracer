package edu.up.mateosanchez.acceleration;

import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.math.Ray;

public class AABB {
    public final Vector3d min;
    public final Vector3d max;

    // Initialize with inverted extreme values
    public AABB() {
        this.min = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        this.max = new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    // Create a bounding box around a triangle
    public void setFromTriangle(Vector3d v0, Vector3d v1, Vector3d v2) {
        this.min.x = Math.min(v0.x, Math.min(v1.x, v2.x));
        this.min.y = Math.min(v0.y, Math.min(v1.y, v2.y));
        this.min.z = Math.min(v0.z, Math.min(v1.z, v2.z));

        this.max.x = Math.max(v0.x, Math.max(v1.x, v2.x));
        this.max.y = Math.max(v0.y, Math.max(v1.y, v2.y));
        this.max.z = Math.max(v0.z, Math.max(v1.z, v2.z));

        // Add padding to flat boxes to avoid division by zero
        double padding = 0.0001;
        if (Math.abs(max.x - min.x) < padding) { min.x -= padding; max.x += padding; }
        if (Math.abs(max.y - min.y) < padding) { min.y -= padding; max.y += padding; }
        if (Math.abs(max.z - min.z) < padding) { min.z -= padding; max.z += padding; }
    }

    // Merge two boxes into one
    public void setFromMerge(AABB box0, AABB box1) {
        this.min.x = Math.min(box0.min.x, box1.min.x);
        this.min.y = Math.min(box0.min.y, box1.min.y);
        this.min.z = Math.min(box0.min.z, box1.min.z);

        this.max.x = Math.max(box0.max.x, box1.max.x);
        this.max.y = Math.max(box0.max.y, box1.max.y);
        this.max.z = Math.max(box0.max.z, box1.max.z);
    }

    // Ray-AABB intersection test (Slab Method)
    public boolean intersect(Ray ray, double tMin, double tMax) {
        // X Axis
        double invD = 1.0 / ray.direction.x;
        double t0 = (min.x - ray.origin.x) * invD;
        double t1 = (max.x - ray.origin.x) * invD;
        if (invD < 0.0) {
            double temp = t0; t0 = t1; t1 = temp;
        }
        tMin = Math.max(t0, tMin);
        tMax = Math.min(t1, tMax);
        if (tMax <= tMin) return false;

        // Y Axis
        invD = 1.0 / ray.direction.y;
        t0 = (min.y - ray.origin.y) * invD;
        t1 = (max.y - ray.origin.y) * invD;
        if (invD < 0.0) {
            double temp = t0; t0 = t1; t1 = temp;
        }
        tMin = Math.max(t0, tMin);
        tMax = Math.min(t1, tMax);
        if (tMax <= tMin) return false;

        // Z Axis
        invD = 1.0 / ray.direction.z;
        t0 = (min.z - ray.origin.z) * invD;
        t1 = (max.z - ray.origin.z) * invD;
        if (invD < 0.0) {
            double temp = t0; t0 = t1; t1 = temp;
        }
        tMin = Math.max(t0, tMin);
        tMax = Math.min(t1, tMax);
        if (tMax <= tMin) return false;

        return true;
    }
}