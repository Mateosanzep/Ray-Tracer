package edu.up.mateosanchez.materials;

import edu.up.mateosanchez.math.Vector3d;

public class Material {
    public Vector3d diffuseColor;
    public Vector3d specularColor;
    public double shininess;

    public Material(Vector3d diffuseColor, Vector3d specularColor, double shininess) {
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.shininess = shininess;
    }
}
