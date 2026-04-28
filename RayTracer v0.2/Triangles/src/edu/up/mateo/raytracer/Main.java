package edu.up.mateo.raytracer;

import java.io.File;
import java.io.IOException;
import java.awt.Color;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) throws IOException {
        Camera camera = new Camera(
            new Vector3D(0, 0, 0), 800, 800, (float)Math.PI / 2f, 0.1f, 1000.0f);
        
        Scene scene = new Scene(camera);
        scene.setBg(Color.BLACK);

        scene.addObject(new Sphere(Color.RED, new Vector3D(-1, -2, -15), 2.0f));
        
        scene.addObject(new Sphere(Color.BLUE, new Vector3D(5, 5, -50), 7.0f));

        scene.addObject(new Triangle(Color.GREEN, new Vector3D(-5, 0, -20), new Vector3D(5, 0, -20), new Vector3D(0, 5, -60)));
        
        Raytracer rt = new Raytracer(scene);
        rt.render();
        
        File output = new File("render.png");
        ImageIO.write(rt.getImage(), "png", output);
    }
}
