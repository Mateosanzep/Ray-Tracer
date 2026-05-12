package edu.up.mateo.raytracer;

import java.awt.Color;

public class Spotlight extends Light {
    public Vector3D direction;
    public Vector3D position;
    public float cutoffCos;

    public Spotlight(Color lightColor, float intensity, Vector3D direction, Vector3D position, float cutoffRadians) {
        super(lightColor, intensity);
        this.direction = direction.Normalize();
        this.position = position;
        this.cutoffCos = (float)Math.cos(cutoffRadians);
    }

    @Override
    public Vector3D getDirection(Vector3D hitPoint) {
        return position.VectorSubstract(hitPoint).Normalize();
    }

    @Override
    public boolean illuminates(Vector3D hitPoint) {
        Vector3D L = getDirection(hitPoint);
        Vector3D lightDir = new Vector3D(-direction.X, -direction.Y, -direction.Z).Normalize();
        float angleCos = L.DotProduct(lightDir);
        return angleCos > cutoffCos;
    }
}
