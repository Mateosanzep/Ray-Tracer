package edu.up.mateosanchez.acceleration;

import edu.up.mateosanchez.geometry.Intersectable;
import edu.up.mateosanchez.geometry.HitRecord;
import edu.up.mateosanchez.math.Ray;
import java.util.Comparator;
import java.util.List;

public class BVHNode implements Intersectable {
    private final Intersectable left;
    private final Intersectable right;
    private final AABB boundingBox;

    // Constructor to recursively build the BVH tree
    public BVHNode(List<Intersectable> objects, int start, int end) {
        this.boundingBox = new AABB();

        // Calculate the bounding box for all current objects
        AABB span = new AABB();
        for (int i = start; i < end; i++) {
            span.setFromMerge(span, objects.get(i).getBoundingBox());
        }

        // Choose the longest axis to split along
        double dx = span.max.x - span.min.x;
        double dy = span.max.y - span.min.y;
        double dz = span.max.z - span.min.z;
        
        int axis = 0; 
        if (dy > dx && dy > dz) axis = 1; 
        else if (dz > dx && dz > dy) axis = 2; 

        // Create a comparator to sort objects along the chosen axis
        final int finalAxis = axis;
        Comparator<Intersectable> comparator = (a, b) -> {
            double minA = finalAxis == 0 ? a.getBoundingBox().min.x : (finalAxis == 1 ? a.getBoundingBox().min.y : a.getBoundingBox().min.z);
            double minB = finalAxis == 0 ? b.getBoundingBox().min.x : (finalAxis == 1 ? b.getBoundingBox().min.y : b.getBoundingBox().min.z);
            return Double.compare(minA, minB);
        };

        int objectSpan = end - start;

        // Base cases and recursive division of the list
        if (objectSpan == 1) {
            left = right = objects.get(start);
        } else if (objectSpan == 2) {
            if (comparator.compare(objects.get(start), objects.get(start + 1)) < 0) {
                left = objects.get(start);
                right = objects.get(start + 1);
            } else {
                left = objects.get(start + 1);
                right = objects.get(start);
            }
        } else {
            objects.subList(start, end).sort(comparator);
            int mid = start + objectSpan / 2;
            left = new BVHNode(objects, start, mid);
            right = new BVHNode(objects, mid, end);
        }

        // Merge children bounding boxes into the parent node box
        this.boundingBox.setFromMerge(left.getBoundingBox(), right.getBoundingBox());
    }

    @Override
    public AABB getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public boolean intersect(Ray ray, double tMin, double tMax, HitRecord hitRecord) {
        // Early exit if the ray misses this node's bounding box
        if (!this.boundingBox.intersect(ray, tMin, tMax)) {
            return false;
        }

        // Check intersection with left and right children
        boolean hitLeft = left.intersect(ray, tMin, tMax, hitRecord);
        boolean hitRight = right.intersect(ray, tMin, hitLeft ? hitRecord.t : tMax, hitRecord);

        return hitLeft || hitRight;
    }
}