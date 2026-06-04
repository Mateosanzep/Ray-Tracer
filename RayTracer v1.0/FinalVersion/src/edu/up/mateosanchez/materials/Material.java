package edu.up.mateosanchez.materials;

import edu.up.mateosanchez.math.Vector3d;

public class Material {
    // Propiedades base del material
    public Vector3d diffuseColor;
    public Vector3d specularColor;
    public double shininess;
    public double reflectivity;
    public double transparency;
    public double ior;
    public Vector3d emissiveColor = new Vector3d(0.0, 0.0, 0.0); // Color emisivo para objetos que brillan

    // ====== MAPAS DE TEXTURA ======
    public Texture texture;         // map_Kd  - Mapa de color difuso (ya existía)
    public Texture specularMap;     // map_Ks  - Mapa de color especular
    public Texture shininessMap;    // map_Ns  - Mapa de brillo/shininess
    public Texture bumpMap;         // map_Bump / bump - Mapa de relieve (bump mapping)
    public double bumpStrength = 1.0; // -bm valor del bump map
    public Texture ambientMap;      // map_Ka  - Mapa de color ambiente
    public Texture emissiveMap;     // map_Ke  - Mapa de emisión
    public Texture alphaMap;        // map_d   - Mapa de transparencia/alfa

    // Constructor con color difuso sólido
    public Material(Vector3d diffuseColor, Vector3d specularColor, double shininess, double reflectivity, double transparency, double ior) {
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.shininess = shininess;
        this.reflectivity = reflectivity;
        this.transparency = transparency;
        this.ior = ior;
        this.texture = null;
    }

    // Constructor con textura difusa
    public Material(Texture texture, Vector3d specularColor, double shininess, double reflectivity, double transparency, double ior) {
        // Usamos blanco como color difuso base para que no altere los colores de tu imagen
        this.diffuseColor = new Vector3d(1.0, 1.0, 1.0); 
        this.specularColor = specularColor;
        this.shininess = shininess;
        this.reflectivity = reflectivity;
        this.transparency = transparency;
        this.ior = ior;
        this.texture = texture; // Guardamos la textura
    }

    // Obtiene el shininess efectivo en un punto UV (usa el mapa si existe)
    public double getEffectiveShininess(double u, double v) {
        if (shininessMap != null && shininessMap.isLoaded()) {
            // El mapa de Ns mapea el valor de la textura (0-1) a un rango de shininess
            // Típicamente Blender exporta valores en escala de grises
            double mapValue = shininessMap.getValue(u, v);
            return mapValue * shininess; // Escalar por el valor Ns base
        }
        return shininess;
    }

    // Obtiene el color especular efectivo en un punto UV
    public void getEffectiveSpecular(double u, double v, Vector3d result) {
        if (specularMap != null && specularMap.isLoaded()) {
            specularMap.getColor(u, v, result);
            // Modular con el color especular base
            result.set(result.x * specularColor.x, result.y * specularColor.y, result.z * specularColor.z);
        } else {
            result.set(specularColor.x, specularColor.y, specularColor.z);
        }
    }

    // Obtiene el color emisivo efectivo en un punto UV
    public void getEffectiveEmissive(double u, double v, Vector3d result) {
        if (emissiveMap != null && emissiveMap.isLoaded()) {
            emissiveMap.getColor(u, v, result);
            // Modular con el color emisivo base
            result.set(result.x * emissiveColor.x, result.y * emissiveColor.y, result.z * emissiveColor.z);
        } else {
            result.set(emissiveColor.x, emissiveColor.y, emissiveColor.z);
        }
    }

    // Obtiene la transparencia efectiva en un punto UV
    public double getEffectiveAlpha(double u, double v) {
    // Si existe un mapa de alfa explícito (map_d)
    if (alphaMap != null && alphaMap.isLoaded()) {
        return alphaMap.getAlpha(u, v);
    }

    // Si la textura difusa es un PNG con canal alfa
    if (texture != null && texture.isLoaded()) {
        return texture.getAlpha(u, v);
    }

    return 1.0 - transparency;
}
}
