package edu.up.mateosanchez.utils;

import edu.up.mateosanchez.materials.Material;
import edu.up.mateosanchez.materials.Texture;
import edu.up.mateosanchez.math.Vector3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class MtlParser {
    public static HashMap<String, Material> parse(String filePath) {
        HashMap<String, Material> materials = new HashMap<>();
        File mtlFile = new File(filePath);
        File parentDir = mtlFile.getParentFile();

        try (BufferedReader br = new BufferedReader(new FileReader(mtlFile))) {
            String line;
            String currentMatName = null;
            Vector3d diffuseColor = new Vector3d(0.8, 0.8, 0.8);
            Vector3d specularColor = new Vector3d(0.0, 0.0, 0.0);
            Vector3d emissiveColor = new Vector3d(0.0, 0.0, 0.0);
            double shininess = 10.0;
            double transparency = 0.0;
            double ior = 1.0;
            int illum = 2; // Default illumination model (illum 2 = diffuse + specular)
            
            String mapKdPath = null;
            String mapNsPath = null;
            String mapBumpPath = null;
            double bumpStrength = 1.0;
            String mapAlphaPath = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] tokens = line.split("\\s+");
                String type = tokens[0];

                if (type.equals("newmtl")) {
                    // Save previous material if it exists before initializing a new one
                    if (currentMatName != null) {
                        Material mat = buildMaterial(
                            diffuseColor,
                            specularColor,
                            emissiveColor,
                            shininess,
                            transparency,
                            ior,
                            illum,
                            mapKdPath,
                            mapNsPath,
                            mapBumpPath,
                            mapAlphaPath,
                            bumpStrength,
                            parentDir
                        );
                        materials.put(currentMatName, mat);
                    }
                    
                    // Reset properties for the new material entry
                    currentMatName = tokens[1];
                    diffuseColor = new Vector3d(0.8, 0.8, 0.8);
                    specularColor = new Vector3d(0.0, 0.0, 0.0);
                    emissiveColor = new Vector3d(0.0, 0.0, 0.0);
                    shininess = 10.0;
                    transparency = 0.0;
                    ior = 1.0;
                    illum = 2;
                    mapKdPath = null;
                    mapNsPath = null;
                    mapBumpPath = null;
                    mapAlphaPath = null;
                    bumpStrength = 1.0;
                    
                } else if (type.equals("Kd")) {
                    diffuseColor = new Vector3d(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]));
                } else if (type.equals("Ks")) {
                    specularColor = new Vector3d(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]));
                } else if (type.equals("Ke")) {
                    emissiveColor = new Vector3d(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]));
                } else if (type.equals("Ns")) {
                    shininess = Double.parseDouble(tokens[1]);
                } else if (type.equals("d")) {
                    transparency = 1.0 - Double.parseDouble(tokens[1]);
                } else if (type.equals("Tr")) {
                    transparency = Double.parseDouble(tokens[1]);
                } else if (type.equals("Ni")) {
                    ior = Double.parseDouble(tokens[1]);
                } else if (type.equals("illum")) {
                    illum = Integer.parseInt(tokens[1]);
                } 
                else if (type.equals("map_Kd")) {
                    mapKdPath = getTexturePath(tokens, 1);
                } 
                else if (type.equals("map_d")) {
                    mapAlphaPath = getTexturePath(tokens, 1);
                }
                else if (type.equals("map_Ns")) {
                    mapNsPath = getTexturePath(tokens, 1);
                } 
                else if (type.equals("map_Bump") || type.equals("bump")) {
                    int startIndex = 1;
                    // Extract optional bump multiplier option flag (-bm strength)
                    if (tokens.length > 2 && tokens[1].equals("-bm")) {
                        bumpStrength = Double.parseDouble(tokens[2]);
                        startIndex = 3; 
                    }
                    mapBumpPath = getTexturePath(tokens, startIndex);
                }
            }

            // Save the remaining final material definition in the file
            if (currentMatName != null) {
                Material mat = buildMaterial(
                    diffuseColor,
                    specularColor,
                    emissiveColor,
                    shininess,
                    transparency,
                    ior,
                    illum,
                    mapKdPath,
                    mapNsPath,
                    mapBumpPath,
                    mapAlphaPath,
                    bumpStrength,
                    parentDir
                );
                materials.put(currentMatName, mat);
            }

        } catch (Exception e) {
            System.err.println("Error reading MTL file: " + filePath + " - " + e.getMessage());
            e.printStackTrace();
        }

        return materials;
    }

    // Helper method to reconstruct the file path string in case it contains spaces
    private static String getTexturePath(String[] tokens, int startIndex) {
        if (startIndex >= tokens.length) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < tokens.length; i++) {
            if (i > startIndex) sb.append(" ");
            sb.append(tokens[i]);
        }
        return sb.toString();
    }

    // Resolve texture file path relative to parent directory and load it safely
    private static Texture loadTexture(String path, File parentDir) {
        if (path == null) return null;
        String resolvedPath = path;
        if (parentDir != null) {
            resolvedPath = new File(parentDir, path).getPath();
        }
        return new Texture(resolvedPath);
    }

    // Assemble the Material structure and bind all parsed optional textures
    private static Material buildMaterial(
        Vector3d diffuseColor,
        Vector3d specularColor,
        Vector3d emissiveColor,
        double shininess,
        double transparency,
        double ior,
        int illum,
        String mapKdPath,
        String mapNsPath,
        String mapBumpPath,
        String mapAlphaPath,
        double bumpStrength,
        File parentDir
    ) {
        double reflectivity = 0.0;
        // Apply ideal specular mirror reflection if illumination model allows (illum >= 3)
        if (illum >= 3) {
            reflectivity = (specularColor.x + specularColor.y + specularColor.z) / 3.0;
            if (reflectivity > 1.0) reflectivity = 1.0;
            if (reflectivity < 0.0) reflectivity = 0.0;
        }

        Material mat;
        
        // Initialize with either Diffuse Map or Solid color
        if (mapKdPath != null) {
            Texture texKd = loadTexture(mapKdPath, parentDir);
            mat = new Material(texKd, specularColor, shininess, reflectivity, transparency, ior);
        } else {
            mat = new Material(diffuseColor, specularColor, shininess, reflectivity, transparency, ior);
        }
        
        // Attach optional specular roughness maps
        if (mapNsPath != null) {
            mat.shininessMap = loadTexture(mapNsPath, parentDir);
        }
        
        // Attach optional surface normal distortion maps
        if (mapBumpPath != null) {
            mat.bumpMap = loadTexture(mapBumpPath, parentDir);
            mat.bumpStrength = bumpStrength;
        }
        
        // Attach optional alpha transparency maps
        if (mapAlphaPath != null) {
            mat.alphaMap = loadTexture(mapAlphaPath, parentDir);
        }       

        mat.emissiveColor = emissiveColor;
        return mat;
    }
}