package edu.up.mateosanchez.core;

import edu.up.mateosanchez.camera.Camera;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.utils.ImageWriter;
import edu.up.mateosanchez.lights.*;
import edu.up.mateosanchez.geometry.Triangle;
import edu.up.mateosanchez.materials.Material;

public class Main {
    public static void main(String[] args) {
        int width = 4096;
        int height = 2160;
        double aspectRatio = (double) width / height;

        // Camera settings imported from Blender
        // Coordinates and vectors defining position and orientation
        Vector3d lookFrom = new Vector3d(11.2860, 3.7314, -1.9612);
        Vector3d lookAt = new Vector3d(10.3451, 3.3929, -1.9644);
        Vector3d vUp = new Vector3d(-0.3385, 0.9410, -0.0016);
        double fov = 23.21;
        double focalLength = 1.0;
        
        Camera camera = new Camera(lookFrom, lookAt, vUp, fov, aspectRatio, focalLength);
        ImageWriter imageWriter = new ImageWriter(width, height);

        Scene scene = new Scene(camera, 0.01);
        
        System.out.println("Loading model...");
        scene.importObj("objects/modelo.obj"); 

        // Area Light settings imported from Blender
        Vector3d corner_Area = new Vector3d(-3.8882, 2.6986, 0.5831);
        Vector3d u_Area = new Vector3d(7.7763, 0.0000, -0.0000);
        Vector3d v_Area = new Vector3d(0.0000, 0.0000, -4.3721);
        Vector3d color_Area = new Vector3d(1.000, 1.000, 1.000);
        
        // Emissive material for the light geometry
        Material mat_Area = new Material(new Vector3d(0.0, 0.0, 0.0), new Vector3d(0.0, 0.0, 0.0), 0.0, 0.0, 0.0, 1.0);
        mat_Area.emissiveColor = color_Area;
        
        // Calculate the four vertices of the area light plane
        Vector3d v00_Area = corner_Area;
        Vector3d v10_Area = new Vector3d(corner_Area.x + u_Area.x, corner_Area.y + u_Area.y, corner_Area.z + u_Area.z);
        Vector3d v01_Area = new Vector3d(corner_Area.x + v_Area.x, corner_Area.y + v_Area.y, corner_Area.z + v_Area.z);
        Vector3d v11_Area = new Vector3d(corner_Area.x + u_Area.x + v_Area.x, corner_Area.y + u_Area.y + v_Area.y, corner_Area.z + u_Area.z + v_Area.z);
        
        // Add physical geometry and light source to the scene
        scene.addObject(new Triangle(v00_Area, v10_Area, v11_Area, mat_Area));
        scene.addObject(new Triangle(v00_Area, v11_Area, v01_Area, mat_Area));
        scene.addLight(new AreaLight(corner_Area, v_Area, u_Area, color_Area, 1.0, 0.05, 0.01));

        // Renderer setup
        Renderer renderer = new Renderer(camera, imageWriter, scene);
        renderer.samplesPerPixel = 1;

        System.out.println("Starting render...");
        long startTime = System.currentTimeMillis();
        
        renderer.render(0.001, Double.MAX_VALUE);
        
        System.out.println("Render time: " + (System.currentTimeMillis() - startTime) + " ms");
        imageWriter.save("Render_Modelo_Blender_3.png");
    }
}