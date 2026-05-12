package edu.up.mateo.raytracer;

import java.awt.Color;

public class PointLight extends Light {
    public Vector3D position;

    public PointLight(Color lightColor, float intensity, Vector3D position) {
        super(lightColor, intensity);
        this.position = position;
    }

    @Override
    public Vector3D getDirection(Vector3D hitPoint) {
        return position.VectorSubstract(hitPoint).Normalize();
    }

    @Override
    public boolean illuminates(Vector3D hitPoint) {
        return true;
    }
}

