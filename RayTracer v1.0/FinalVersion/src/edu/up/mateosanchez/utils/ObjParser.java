package edu.up.mateosanchez.utils;

import edu.up.mateosanchez.geometry.Intersectable;
import edu.up.mateosanchez.geometry.Triangle;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.math.Vector2d;
import edu.up.mateosanchez.materials.Material;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ObjParser {
    
    // Legacy compatible signature: applies a single fallback material to the entire geometry
    public static ArrayList<Intersectable> parse(String filePath, Material material) {
        return parseInternal(filePath, new HashMap<>(), material);
    }

    // Automatically parses materials from the mtllib reference specified inside the .obj file
    public static ArrayList<Intersectable> parse(String filePath) {
        return parseInternal(filePath, new HashMap<>(), null);
    }

    // Explicitly overrides and loads an external .mtl file location
    public static ArrayList<Intersectable> parse(String filePath, String mtlFilePath) {
        HashMap<String, Material> preLoaded = MtlParser.parse(mtlFilePath);
        return parseInternal(filePath, preLoaded, null);
    }

    // Core OBJ file line evaluation logic and material binding loops
    private static ArrayList<Intersectable> parseInternal(String filePath, HashMap<String, Material> preLoadedMaterials, Material fallbackMaterial) {
        ArrayList<Intersectable> triangles = new ArrayList<>();
        ArrayList<Vector3d> vertices = new ArrayList<>();
        ArrayList<Vector3d> normals = new ArrayList<>();
        ArrayList<Vector2d> uvs = new ArrayList<>();

        HashMap<String, Material> materials = new HashMap<>(preLoadedMaterials);
        Material currentMaterial = fallbackMaterial;

        // Setup a neutral fallback material if none is specified or loaded
        if (currentMaterial == null) {
            currentMaterial = new Material(
                new Vector3d(0.8, 0.8, 0.8), // Diffuse
                new Vector3d(0.0, 0.0, 0.0), // Specular
                10.0, // Shininess
                0.0,  // Reflectivity
                0.0,  // Transparency
                1.0   // IOR
            );
        }

        File objFile = new File(filePath);
        File parentDir = objFile.getParentFile();

        try (BufferedReader br = new BufferedReader(new FileReader(objFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] tokens = line.split("\\s+");
                String type = tokens[0];

                if (type.equals("v")) {
                    vertices.add(new Vector3d(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])));
                } else if (type.equals("vn")) {
                    normals.add(new Vector3d(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])));
                } else if (type.equals("vt")) {
                    uvs.add(new Vector2d(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2])));
                } else if (type.equals("mtllib")) {
                    // Resolve and parse automatic material library reference if preLoaded map is empty
                    if (preLoadedMaterials.isEmpty()) {
                        String mtlFileName = tokens[1];
                        String mtlPath = mtlFileName;
                        if (parentDir != null) {
                            mtlPath = new File(parentDir, mtlFileName).getPath();
                        }
                        if (new File(mtlPath).exists()) {
                            HashMap<String, Material> loaded = MtlParser.parse(mtlPath);
                            materials.putAll(loaded);
                        } else {
                            System.err.println("Warning: MTL library file not found at " + mtlPath);
                        }
                    }
                } else if (type.equals("usemtl")) {
                    String matName = tokens[1];
                    if (materials.containsKey(matName)) {
                        currentMaterial = materials.get(matName);
                    } else if (fallbackMaterial != null) {
                        currentMaterial = fallbackMaterial;
                    }
                } else if (type.equals("f")) {
                    // Dynamic triangulation of arbitrary convex polygons via Triangle Fan routine
                    for (int i = 1; i < tokens.length - 2; i++) {
                        String[] part0 = tokens[1].split("/", -1);       
                        String[] part1 = tokens[i + 1].split("/", -1);   
                        String[] part2 = tokens[i + 2].split("/", -1);   

                        // 1. Extract 1-based index geometry structural vertices (Guaranteed to exist)
                        Vector3d v0 = vertices.get(Integer.parseInt(part0[0]) - 1);
                        Vector3d v1 = vertices.get(Integer.parseInt(part1[0]) - 1);
                        Vector3d v2 = vertices.get(Integer.parseInt(part2[0]) - 1);

                        // 2. Extract texture map coordinates (vt) elements if defined
                        Vector2d uv0 = null, uv1 = null, uv2 = null;
                        if (part0.length > 1 && !part0[1].isEmpty()) {
                            uv0 = uvs.get(Integer.parseInt(part0[1]) - 1);
                            uv1 = uvs.get(Integer.parseInt(part1[1]) - 1);
                            uv2 = uvs.get(Integer.parseInt(part2[1]) - 1);
                        }

                        // 3. Extract interpolated normal vectors (vn) attributes if defined
                        Vector3d n0 = null, n1 = null, n2 = null;
                        if (part0.length > 2 && !part0[2].isEmpty()) {
                            normals.get(Integer.parseInt(part0[2]) - 1);
                            n0 = normals.get(Integer.parseInt(part0[2]) - 1);
                            n1 = normals.get(Integer.parseInt(part1[2]) - 1);
                            n2 = normals.get(Integer.parseInt(part2[2]) - 1);
                        }

                        // Assemble and assign active material reference to the generated primitive face
                        triangles.add(new Triangle(v0, v1, v2, n0, n1, n2, uv0, uv1, uv2, currentMaterial));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading OBJ file data stream: " + e.getMessage());
            e.printStackTrace();
        }
        return triangles;
    }
}