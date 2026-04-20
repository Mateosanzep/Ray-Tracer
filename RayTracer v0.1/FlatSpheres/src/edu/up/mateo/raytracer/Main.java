package edu.up.mateo.raytracer;

import java.io.File;
import java.io.IOException;
import java.awt.Color;

import javax.imageio.ImageIO;

public class Main {
        public static void main(String[] args) throws IOException {
        Camera camera = new Camera(new Vector3D(0,0,0), 800, 800, (float)Math.PI/2);
        
        Scene scene = new Scene(camera);

        scene.addObject(new Sphere(Color.RED, new Vector3D(-1, -2, -15), 2.0f));
        scene.addObject(new Sphere(Color.BLUE, new Vector3D(20, 20, -100), 7.0f));
        
        Raytracer rt = new Raytracer(scene);
        rt.render();
        
        File output = new File("render.png");
        ImageIO.write(rt.getImage(), "png", output);
        }
}
