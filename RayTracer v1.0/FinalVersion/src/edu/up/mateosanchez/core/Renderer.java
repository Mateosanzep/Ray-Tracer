package edu.up.mateosanchez.core;

import edu.up.mateosanchez.camera.Camera;
import edu.up.mateosanchez.math.Ray;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.utils.ImageWriter;

public class Renderer {
    public Camera camera;
    public ImageWriter imagewriter;

    public Renderer(Camera camara, ImageWriter imagewriter) {
        this.camera = camara;
        this.imagewriter = imagewriter;
    }

    public void render(){
        Vector3d directionBuffer = new Vector3d();
        Vector3d originBuffer = camera.origin;
        Ray ray = new Ray(originBuffer, directionBuffer);
        Vector3d pixelColor = new Vector3d();

        for (int y = 0; y < imagewriter.height; y++){
            for (int x = 0; x < imagewriter.width; x++){
            double u = (double) x / (imagewriter.width - 1);
            double v = (double) (imagewriter.height - 1 - y) / (imagewriter.height - 1);

            camera.getRayDirection(u, v, directionBuffer);
            
            ray.setDirection(directionBuffer);

            rayColor(ray, pixelColor);

            imagewriter.setPixel(x, y, pixelColor.x, pixelColor.y, pixelColor.z);

            }
        }
    }

    private void rayColor(Ray ray, Vector3d resultColor) {
        double t = 0.5 * (ray.direction.y + 1.0);
        
        double r = (1.0 - t) * 1.0 + t * 0.5;
        double g = (1.0 - t) * 1.0 + t * 0.7;
        double b = (1.0 - t) * 1.0 + t * 1.0;
        
        resultColor.set(r, g, b);
    }

}
