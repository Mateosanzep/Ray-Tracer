package edu.up.mateo.raytracer;

import java.awt.Color;

public abstract class Light {
    public Color lightColor;
    public float intensity;

    public Light(Color lightColor, float intensity) {
        this.lightColor = lightColor;
        this.intensity = intensity;
    }

    public abstract Vector3D getDirection(Vector3D hitPoint);

    public abstract boolean illuminates(Vector3D hitPoint);
}
