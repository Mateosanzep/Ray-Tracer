package edu.up.mateosanchez.core;

import java.util.ArrayList;

import edu.up.mateosanchez.camera.Camera;
import edu.up.mateosanchez.geometry.Intersectable;
import edu.up.mateosanchez.geometry.Sphere;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.utils.ImageWriter;
import edu.up.mateosanchez.materials.Material;
import edu.up.mateosanchez.lights.Light;
import edu.up.mateosanchez.lights.PointLight;
import edu.up.mateosanchez.lights.DirectionalLight;

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

        Material shinyRed = new Material(
            new Vector3d(1.0, 0.2, 0.2), new Vector3d(1.0, 1.0, 1.0), 100.0
        );
        Material shinyBlue = new Material(
            new Vector3d(0.2, 0.2, 1.0), new Vector3d(0.8, 0.8, 0.8), 15.0
        );
        Material matteGray = new Material(
            new Vector3d(0.5, 0.5, 0.5), new Vector3d(0.1, 0.1, 0.1), 4.0
        );

        ArrayList<Intersectable> scene = new ArrayList<>();
        scene.add(new Sphere(new Vector3d(-1.2, 0.0, -4.0), 1.0, shinyRed));
        scene.add(new Sphere(new Vector3d(1.2, 0.5, -5.0), 1.2, shinyBlue));
        scene.add(new Sphere(new Vector3d(0.0, -1001.0, -4.0), 1000.0, matteGray));

        ArrayList<Light> lights = new ArrayList<>();

        Vector3d lightPos = new Vector3d(0.0, 5.0, -2.0);
        Vector3d lightColor = new Vector3d(0.5, 0.5, 0.0);
        lights.add(new PointLight(lightPos, lightColor));

        Vector3d sunDirection = new Vector3d(-1.0, -1.0, -2.0); 
        Vector3d sunColor = new Vector3d(0.2, 0.2, 0.25);
        lights.add(new DirectionalLight(sunDirection, sunColor));


        double ambientIntensity = 0.1;

        Renderer renderer = new Renderer(camera, imageWriter, scene, lights, ambientIntensity);

        long startTime = System.currentTimeMillis();
        
        renderer.render(tMin, tMax);
        
        long endTime = System.currentTimeMillis();
        System.out.println("Rendering time: " + (endTime - startTime) + " ms");

        imageWriter.save("Lights and Shading Sphere.png");
        }
}