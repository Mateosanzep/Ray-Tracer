package edu.up.mateosanchez.core;

import edu.up.mateosanchez.camera.Camera;
import edu.up.mateosanchez.math.Ray;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.utils.ImageWriter;
import edu.up.mateosanchez.geometry.HitRecord;
import edu.up.mateosanchez.geometry.Intersectable;
import java.util.ArrayList;

public class Renderer {
    public Camera camera;
    public ImageWriter imagewriter;
    public ArrayList<Intersectable> objects;

    public Renderer(Camera camara, ImageWriter imagewriter, ArrayList<Intersectable> objects) {
        this.camera = camara;
        this.imagewriter = imagewriter;
        this.objects = objects;
    }

    HitRecord record = new HitRecord();
    HitRecord tempRecord = new HitRecord();

    public void render(double tMin, double tMax){
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

            rayColor(ray, pixelColor, tMin, tMax);

            imagewriter.setPixel(x, y, pixelColor.x, pixelColor.y, pixelColor.z);

            }
        }
    }

    private void rayColor(Ray ray, Vector3d resultColor, double tMin, double tMax) {
        boolean hittedSome = false;
        double tNear = tMax;


        for (Intersectable intersectable : objects) {
            if (intersectable.intersect(ray, tMin, tNear, tempRecord)) {
                hittedSome = true;
                tNear = tempRecord.t;
                record.t = tempRecord.t;
                record.point.setVector(tempRecord.point);
                record.normal.setVector(tempRecord.normal);
            }
        }

        if (hittedSome) {
            Vector3d n = record.normal;
            double r = 0.5 * (n.x + 1.0);
            double g = 0.5 * (n.y + 1.0);
            double b = 0.5 * (n.z + 1.0);
            resultColor.set(r, g, b);
            return;
        }

        double t = 0.5 * (ray.direction.y + 1.0);
        double r = (1.0 - t) * 1.0 + t * 0.5;
        double g = (1.0 - t) * 1.0 + t * 0.7;
        double b = (1.0 - t) * 1.0 + t * 1.0;

        resultColor.set(r, g, b);
    }

}
