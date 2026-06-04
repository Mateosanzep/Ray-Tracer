package edu.up.mateosanchez.geometry;

import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.materials.Material;

public class HitRecord {
    public double t;
    public Vector3d point;
    public Vector3d normal;
    public Material material;
    public double u;
    public double v;

    public HitRecord() {
        this.t = 0.0;
        this.point = new Vector3d();
        this.normal = new Vector3d();
    }
}
