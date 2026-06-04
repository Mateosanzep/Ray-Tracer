package edu.up.mateosanchez.geometry;

import edu.up.mateosanchez.acceleration.AABB;
import edu.up.mateosanchez.math.Ray;

public interface Intersectable {
    public AABB getBoundingBox();
    boolean intersect(Ray ray, double tMin, double tMax, HitRecord hitRecord);
}
