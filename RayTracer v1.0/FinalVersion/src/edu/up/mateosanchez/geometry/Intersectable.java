package edu.up.mateosanchez.geometry;

import edu.up.mateosanchez.math.Ray;

public interface Intersectable {
    boolean intersect(Ray ray, double tMin, double tMax, HitRecord hitRecord);
}
