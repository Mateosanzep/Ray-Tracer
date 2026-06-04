package edu.up.mateosanchez.acceleration;

import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.math.Ray;

public class AABB {
    public final Vector3d min;
    public final Vector3d max;

    // Constructor base: Inicializa vectores vacíos para cumplir el "Zero Allocation"
    public AABB() {
        this.min = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        this.max = new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    // Configura la caja para que envuelva perfectamente a un triángulo
    public void setFromTriangle(Vector3d v0, Vector3d v1, Vector3d v2) {
        this.min.x = Math.min(v0.x, Math.min(v1.x, v2.x));
        this.min.y = Math.min(v0.y, Math.min(v1.y, v2.y));
        this.min.z = Math.min(v0.z, Math.min(v1.z, v2.z));

        this.max.x = Math.max(v0.x, Math.max(v1.x, v2.x));
        this.max.y = Math.max(v0.y, Math.max(v1.y, v2.y));
        this.max.z = Math.max(v0.z, Math.max(v1.z, v2.z));

        // --- TRUCO DE SEGURIDAD ---
        // Si un triángulo es perfectamente plano en un eje (ej. una pared), la caja mediría 0.
        // Le damos un grosor mínimo (pad) para evitar errores de división por cero.
        double padding = 0.0001;
        if (Math.abs(max.x - min.x) < padding) { min.x -= padding; max.x += padding; }
        if (Math.abs(max.y - min.y) < padding) { min.y -= padding; max.y += padding; }
        if (Math.abs(max.z - min.z) < padding) { min.z -= padding; max.z += padding; }
    }

    // Fusiona dos cajas existentes en una sola caja grande (Para el BVH)
    public void setFromMerge(AABB box0, AABB box1) {
        this.min.x = Math.min(box0.min.x, box1.min.x);
        this.min.y = Math.min(box0.min.y, box1.min.y);
        this.min.z = Math.min(box0.min.z, box1.min.z);

        this.max.x = Math.max(box0.max.x, box1.max.x);
        this.max.y = Math.max(box0.max.y, box1.max.y);
        this.max.z = Math.max(box0.max.z, box1.max.z);
    }

    // MÉTODO DE INTERSECCIÓN ULTRA RÁPIDO (Slab Method / Método de las losas)
    // Cambia ray.origin.x o ray.direction.x por tus métodos getter si los usas así (ej: ray.getOrigin().x)
    public boolean intersect(Ray ray, double tMin, double tMax) {
        // --- EJE X ---
        double invD = 1.0 / ray.direction.x;
        double t0 = (min.x - ray.origin.x) * invD;
        double t1 = (max.x - ray.origin.x) * invD;
        if (invD < 0.0) {
            double temp = t0; t0 = t1; t1 = temp;
        }
        tMin = Math.max(t0, tMin);
        tMax = Math.min(t1, tMax);
        if (tMax <= tMin) return false;

        // --- EJE Y ---
        invD = 1.0 / ray.direction.y;
        t0 = (min.y - ray.origin.y) * invD;
        t1 = (max.y - ray.origin.y) * invD;
        if (invD < 0.0) {
            double temp = t0; t0 = t1; t1 = temp;
        }
        tMin = Math.max(t0, tMin);
        tMax = Math.min(t1, tMax);
        if (tMax <= tMin) return false;

        // --- EJE Z ---
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