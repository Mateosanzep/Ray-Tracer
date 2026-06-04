package edu.up.mateosanchez.materials;

import edu.up.mateosanchez.math.Vector3d;

public class Material {
    // Base material properties
    public Vector3d diffuseColor;
    public Vector3d specularColor;
    public double shininess;
    public double reflectivity;
    public double transparency;
    public double ior;
    public Vector3d emissiveColor = new Vector3d(0.0, 0.0, 0.0); 

    // ====== TEXTURE MAPS ======
    public Texture texture;         // map_Kd  - Diffuse color map
    public Texture specularMap;     // map_Ks  - Specular color map
    public Texture shininessMap;    // map_Ns  - Shininess map
    public Texture bumpMap;         // map_Bump / bump - Bump mapping normal distortion map
    public double bumpStrength = 1.0; // -bm bump map multiplier strength
    public Texture ambientMap;      // map_Ka  - Ambient occlusion / color map
    public Texture emissiveMap;     // map_Ke  - Emissive light map
    public Texture alphaMap;        // map_d   - Alpha transparency map

    // Solid diffuse color constructor
    public Material(Vector3d diffuseColor, Vector3d specularColor, double shininess, double reflectivity, double transparency, double ior) {
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.shininess = shininess;
        this.reflectivity = reflectivity;
        this.transparency = transparency;
        this.ior = ior;
        this.texture = null;
    }

    // Textured diffuse constructor
    public Material(Texture texture, Vector3d specularColor, double shininess, double reflectivity, double transparency, double ior) {
        // Set solid diffuse base to white to prevent altering the texture image colors
        this.diffuseColor = new Vector3d(1.0, 1.0, 1.0); 
        this.specularColor = specularColor;
        this.shininess = shininess;
        this.reflectivity = reflectivity;
        this.transparency = transparency;
        this.ior = ior;
        this.texture = texture; 
    }

    // Get the final shininess factor at specific UV coordinates
    public double getEffectiveShininess(double u, double v) {
        if (shininessMap != null && shininessMap.isLoaded()) {
            double mapValue = shininessMap.getValue(u, v);
            return mapValue * shininess; 
        }
        return shininess;
    }

    // Get the modulated specular color at specific UV coordinates
    public void getEffectiveSpecular(double u, double v, Vector3d result) {
        if (specularMap != null && specularMap.isLoaded()) {
            specularMap.getColor(u, v, result);
            result.set(result.x * specularColor.x, result.y * specularColor.y, result.z * specularColor.z);
        } else {
            result.set(specularColor.x, specularColor.y, specularColor.z);
        }
    }

    // Get the modulated emissive glow color at specific UV coordinates
    public void getEffectiveEmissive(double u, double v, Vector3d result) {
        if (emissiveMap != null && emissiveMap.isLoaded()) {
            emissiveMap.getColor(u, v, result);
            result.set(result.x * emissiveColor.x, result.y * emissiveColor.y, result.z * emissiveColor.z);
        } else {
            result.set(emissiveColor.x, emissiveColor.y, emissiveColor.z);
        }
    }

    // Get the final transparency alpha value at specific UV coordinates
    public double getEffectiveAlpha(double u, double v) {
        // Check explicit alpha map (map_d) first
        if (alphaMap != null && alphaMap.isLoaded()) {
            return alphaMap.getAlpha(u, v);
        }

        // Fallback to diffuse PNG embedded alpha channel channel if available
        if (texture != null && texture.isLoaded()) {
            return texture.getAlpha(u, v);
        }

        return 1.0 - transparency;
    }
}