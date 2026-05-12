package edu.up.mateo.raytracer;

import java.awt.Color;

public class Light {
    public Color lightColor;
    public float intensity;
    public Vector3D direction;
    public Vector3D position;
    public float cutoff;

    public Light(Color lightColor, float intensity, Vector3D direction, Vector3D position, float cutoff) {
        this.lightColor = lightColor;
        this.intensity = intensity;
        this.direction = direction;
        this.position = position;
        this.cutoff = (float) Math.cos(cutoff);;
    }
    
}
