package edu.up.mateo.raytracer;

import java.awt.Color;

public class DirectionalLight extends Light {
    public Vector3D direction;

    public DirectionalLight(Color lightColor, float intensity, Vector3D direction) {
        super(lightColor, intensity);
        this.direction = direction.Normalize();
    }

    @Override
    public Vector3D getDirection(Vector3D hitPoint) {
        return new Vector3D(-direction.X, -direction.Y, -direction.Z).Normalize();
    }

    @Override
    public boolean illuminates(Vector3D hitPoint) {
        return true;
    }
}
