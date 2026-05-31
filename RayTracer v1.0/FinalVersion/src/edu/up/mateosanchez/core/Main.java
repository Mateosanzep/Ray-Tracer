package edu.up.mateosanchez.core;

// Importamos todas las piezas que creamos en otros paquetes
import edu.up.mateosanchez.camera.Camera;
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
        
        Camera camera = new Camera(lookFrom, fov, aspectRatio, focalLength);

        ImageWriter imageWriter = new ImageWriter(width, height);
        Renderer renderer = new Renderer(camera, imageWriter);

        long startTime = System.currentTimeMillis();
        
        renderer.render();
        
        long endTime = System.currentTimeMillis();
        System.out.println("Rendering time: " + (endTime - startTime) + " ms");

        imageWriter.save("Sky.png");
        }
}