package edu.up.mateo.raytracer;

import java.awt.Color;

public class PointLight extends Light {
    public Vector3D position;
    public float attConst = 1f;
    public float attLinear = 0f;
    public float attQuadratic = 0f;

    public PointLight(Color lightColor, float intensity, Vector3D position) {
        super(lightColor, intensity);
        this.position = position;
    }

    public PointLight(Color lightColor, float intensity, Vector3D position, float attConst, float attLinear, float attQuadratic) {
        super(lightColor, intensity);
        this.position = position;
        this.attConst = attConst;
        this.attLinear = attLinear;
        this.attQuadratic = attQuadratic;
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

