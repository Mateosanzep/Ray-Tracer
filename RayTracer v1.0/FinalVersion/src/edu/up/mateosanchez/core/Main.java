package edu.up.mateosanchez.core;

import java.util.ArrayList;

// Importamos todas las piezas que creamos en otros paquetes
import edu.up.mateosanchez.camera.Camera;
import edu.up.mateosanchez.geometry.Intersectable;
import edu.up.mateosanchez.geometry.Sphere;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.utils.ImageWriter;

public class Main {
    public static void main(String[] args) {
        int width = 4096;
        int height = 2160;
        double aspectRatio = (double) width / height;

        Vector3d lookFrom = new Vector3d(0.0, 0.0, 0.0);
        double fov = 90.0;
        double focalLength = 1.0;
        double tMin = 0.001;
        double tMax = Double.MAX_VALUE;
        
        Camera camera = new Camera(lookFrom, fov, aspectRatio, focalLength);

        ImageWriter imageWriter = new ImageWriter(width, height);

        ArrayList<Intersectable> scene = new ArrayList<>();
        scene.add(new Sphere(new Vector3d(0.0, 0.0, -1.0), 0.5));
        scene.add(new Sphere(new Vector3d(0.5, 0.5, -1.0), 0.3));

        Renderer renderer = new Renderer(camera, imageWriter, scene);

        long startTime = System.currentTimeMillis();
        
        renderer.render(tMin, tMax);
        
        long endTime = System.currentTimeMillis();
        System.out.println("Rendering time: " + (endTime - startTime) + " ms");

        imageWriter.save("Sphere.png");
        }
}