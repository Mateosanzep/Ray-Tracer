package edu.up.mateo.raytracer;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OBJReader {
    public List<Vector3D> vertex = new ArrayList<>();
    public List<Triangle> faces = new ArrayList<>();
    public List<int[]> faceIndices = new ArrayList<>();

    public void read(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] tokens = line.split("\\s+");
                String type = tokens[0];

                if (type.equals("v")) {
                    float newX = Float.parseFloat(tokens[1]);
                    float newY = Float.parseFloat(tokens[2]);
                    float newZ = Float.parseFloat(tokens[3]);
                    this.vertex.add(new Vector3D(newX, newY, newZ));

                } else if (type.equals("f")) {
                    int v0 = getIndex(tokens[1]);
                    int v1 = getIndex(tokens[2]);
                    int v2 = getIndex(tokens[3]);

                    if (tokens.length == 4) {
                        this.faces.add(new Triangle(Color.GRAY, vertex.get(v0), vertex.get(v1), vertex.get(v2)));
                        this.faceIndices.add(new int[]{v0, v1, v2});
                    } else if (tokens.length == 5) {
                        int v3 = getIndex(tokens[4]);
                        
                        this.faces.add(new Triangle(Color.GRAY, vertex.get(v1), vertex.get(v0), vertex.get(v2)));
                        this.faceIndices.add(new int[]{v1, v0, v2});
                        this.faces.add(new Triangle(Color.GRAY, vertex.get(v2), vertex.get(v0), vertex.get(v3)));
                        this.faceIndices.add(new int[]{v2, v0, v3});
                    }
                }
            }
        }

        // If we have faces and vertices, compute smooth normals per vertex
        if (!faceIndices.isEmpty() && !vertex.isEmpty()) {
            List<Vector3D> sumNormals = new ArrayList<>(vertex.size());
            for (int i = 0; i < vertex.size(); i++) {
                sumNormals.add(new Vector3D(0f, 0f, 0f));
            }

            for (int i = 0; i < faceIndices.size(); i++) {
                int[] idx = faceIndices.get(i);
                Vector3D v0v = vertex.get(idx[0]);
                Vector3D v1v = vertex.get(idx[1]);
                Vector3D v2v = vertex.get(idx[2]);

                Vector3D edge1 = v1v.VectorSubstract(v0v);
                Vector3D edge2 = v2v.VectorSubstract(v0v);
                Vector3D faceNormal = edge1.CrossProduct(edge2).Normalize();

                sumNormals.set(idx[0], sumNormals.get(idx[0]).VectorAdd(faceNormal));
                sumNormals.set(idx[1], sumNormals.get(idx[1]).VectorAdd(faceNormal));
                sumNormals.set(idx[2], sumNormals.get(idx[2]).VectorAdd(faceNormal));
            }

            // Normalize summed normals
            for (int i = 0; i < sumNormals.size(); i++) {
                sumNormals.set(i, sumNormals.get(i).Normalize());
            }

            // Assign normals to faces
            for (int i = 0; i < faceIndices.size(); i++) {
                int[] idx = faceIndices.get(i);
                Triangle t = faces.get(i);
                t.setVertexNormals(sumNormals.get(idx[0]), sumNormals.get(idx[1]), sumNormals.get(idx[2]));
            }
        }
    }

    private int getIndex(String token) {
        String[] parts = token.split("/");
        String indexStr = parts[0];
        return Integer.parseInt(indexStr) - 1;
    }
}