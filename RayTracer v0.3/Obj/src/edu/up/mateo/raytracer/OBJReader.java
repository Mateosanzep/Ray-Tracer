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
                    } else if (tokens.length == 5) {
                        int v3 = getIndex(tokens[4]);
                        
                        this.faces.add(new Triangle(Color.GRAY, vertex.get(v1), vertex.get(v0), vertex.get(v2)));
                        this.faces.add(new Triangle(Color.GRAY, vertex.get(v2), vertex.get(v0), vertex.get(v3)));
                    }
                }
            }
        }
    }

    private int getIndex(String token) {
        String[] parts = token.split("/");
        String indexStr = parts[0];
        return Integer.parseInt(indexStr) - 1;
    }
}