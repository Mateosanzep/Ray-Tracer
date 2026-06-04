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
    
    // Firma original compatible: aplica un único material por defecto a toda la geometría
    public static ArrayList<Intersectable> parse(String filePath, Material material) {
        return parseInternal(filePath, new HashMap<>(), material);
    }

    // NUEVO: Obtiene los materiales leyendo automáticamente el mtllib referenciado en el archivo .obj
    public static ArrayList<Intersectable> parse(String filePath) {
        return parseInternal(filePath, new HashMap<>(), null);
    }

    // NUEVO: Permite forzar o especificar de forma explícita dónde está el archivo .mtl
    public static ArrayList<Intersectable> parse(String filePath, String mtlFilePath) {
        HashMap<String, Material> preLoaded = MtlParser.parse(mtlFilePath);
        return parseInternal(filePath, preLoaded, null);
    }

    // Lógica interna de análisis para OBJ y vinculación de materiales
    private static ArrayList<Intersectable> parseInternal(String filePath, HashMap<String, Material> preLoadedMaterials, Material fallbackMaterial) {
        ArrayList<Intersectable> triangles = new ArrayList<>();
        ArrayList<Vector3d> vertices = new ArrayList<>();
        ArrayList<Vector3d> normals = new ArrayList<>();
        ArrayList<Vector2d> uvs = new ArrayList<>();

        HashMap<String, Material> materials = new HashMap<>(preLoadedMaterials);
        Material currentMaterial = fallbackMaterial;

        // Material neutro de respaldo por si no se especifica ninguno
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
                // Ignorar líneas vacías o comentarios
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
                    // Si no se precargaron materiales de forma manual, cargamos la referencia automática del .obj
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
                            System.err.println("Advertencia: No se encontró el archivo MTL en " + mtlPath);
                        }
                    }
                } else if (type.equals("usemtl")) {
                    String matName = tokens[1];
                    if (materials.containsKey(matName)) {
                        currentMaterial = materials.get(matName);
                    } else {
                        // Si el material no está en el mapa, intentamos usar el fallback
                        if (fallbackMaterial != null) {
                            currentMaterial = fallbackMaterial;
                        }
                    }
                } else if (type.equals("f")) {
                    // Triangulación usando Triangle Fan para polígonos
                    for (int i = 1; i < tokens.length - 2; i++) {
                        String[] part0 = tokens[1].split("/", -1);       
                        String[] part1 = tokens[i + 1].split("/", -1);   
                        String[] part2 = tokens[i + 2].split("/", -1);   

                        // 1. Extraer índices de Vértices (Siempre existen)
                        Vector3d v0 = vertices.get(Integer.parseInt(part0[0]) - 1);
                        Vector3d v1 = vertices.get(Integer.parseInt(part1[0]) - 1);
                        Vector3d v2 = vertices.get(Integer.parseInt(part2[0]) - 1);

                        // 2. Extraer índices de Texturas (vt) si existen
                        Vector2d uv0 = null, uv1 = null, uv2 = null;
                        if (part0.length > 1 && !part0[1].isEmpty()) {
                            uv0 = uvs.get(Integer.parseInt(part0[1]) - 1);
                            uv1 = uvs.get(Integer.parseInt(part1[1]) - 1);
                            uv2 = uvs.get(Integer.parseInt(part2[1]) - 1);
                        }

                        // 3. Extraer índices de Normales (vn) si existen
                        Vector3d n0 = null, n1 = null, n2 = null;
                        if (part0.length > 2 && !part0[2].isEmpty()) {
                            n0 = normals.get(Integer.parseInt(part0[2]) - 1);
                            n1 = normals.get(Integer.parseInt(part1[2]) - 1);
                            n2 = normals.get(Integer.parseInt(part2[2]) - 1);
                        }

                        // Crear el triángulo con el material activo para esta sección
                        triangles.add(new Triangle(v0, v1, v2, n0, n1, n2, uv0, uv1, uv2, currentMaterial));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error leyendo el archivo OBJ: " + e.getMessage());
            e.printStackTrace();
        }
        return triangles;
    }
}