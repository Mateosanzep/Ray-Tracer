package edu.up.mateo.raytracer;

import java.io.File;
import java.io.IOException;
import java.awt.Color;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        try {
            Camera camera = new Camera(new Vector3D(0, 1, 5), 800, 800, (float)Math.PI/2f, 0.1f, 10000f);
            
            Scene scene = new Scene(camera);
            scene.setBg(Color.BLACK);


                PointLight pointLight = new PointLight(Color.WHITE, 1.2f, new Vector3D(0, 3, 8), 1f, 0.09f, 0.032f);
                scene.addLight(pointLight);

                Plane ground = new Plane(Color.LIGHT_GRAY, new Vector3D(0f, -1f, 0f), new Vector3D(0f, 1f, 0f));
                scene.addObject(ground);

            OBJReader reader = new OBJReader();
            String objPath = "car.obj";
            if (args != null && args.length > 0) objPath = args[0];
            reader.read(objPath);

            for (Triangle face : reader.faces) {
                face.color = Color.CYAN;
                scene.addObject(face);
            }

            Raytracer rt = new Raytracer(scene);
            rt.render();
            
            File output = new File("render_v07_shadow.png");
            ImageIO.write(rt.getImage(), "png", output);
            
        } catch (IOException e) {
            System.err.println("Error on " + e.getMessage());
        }
    }
}