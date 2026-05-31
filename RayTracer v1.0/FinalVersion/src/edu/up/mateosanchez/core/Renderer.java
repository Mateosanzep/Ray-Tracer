package edu.up.mateosanchez.core;

import edu.up.mateosanchez.camera.Camera;
import edu.up.mateosanchez.math.Ray;
import edu.up.mateosanchez.math.Vector3d;
import edu.up.mateosanchez.utils.ImageWriter;
import edu.up.mateosanchez.geometry.HitRecord;
import edu.up.mateosanchez.geometry.Intersectable;
import edu.up.mateosanchez.lights.Light;

import java.util.ArrayList;

public class Renderer {
    public Camera camera;
    public ImageWriter imagewriter;
    public ArrayList<Intersectable> objects;
    public ArrayList<Light> lights;
    public double ambientIntensity;

    private final Vector3d lightDirBuffer = new Vector3d();
    private final Vector3d lightColorBuffer = new Vector3d();
    private final Vector3d viewDirBuffer = new Vector3d();
    private final Vector3d halfwayBuffer = new Vector3d();
    private final Ray shadowRay = new Ray(new Vector3d(), new Vector3d());

    public Renderer(Camera camara, ImageWriter imagewriter, ArrayList<Intersectable> objects, ArrayList<Light> lights, double ambientIntensity) {
        this.camera = camara;
        this.imagewriter = imagewriter;
        this.objects = objects;
        this.lights = lights;
        this.ambientIntensity = ambientIntensity;
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
                record.material = tempRecord.material;
            }
        }

        if (hittedSome) {
            Vector3d n = record.normal;
            
            double totalR = record.material.diffuseColor.x * this.ambientIntensity;
            double totalG = record.material.diffuseColor.y * this.ambientIntensity;
            double totalB = record.material.diffuseColor.z * this.ambientIntensity;

            viewDirBuffer.set(-ray.direction.x, -ray.direction.y, -ray.direction.z);
            viewDirBuffer.normalize();

            for (Light light : lights) {
                light.getDirection(record.point, lightDirBuffer);
                light.getColor(lightColorBuffer);
                double distanceToLight = light.getDistance(record.point);

                shadowRay.setOrigin(record.point);
                shadowRay.setDirection(lightDirBuffer);

                boolean inShadow = false;
                for (Intersectable object : objects) {
                    if (object.intersect(shadowRay, tMin, distanceToLight, tempRecord)) {
                        inShadow = true;
                        break;
                    }
                }

                if (inShadow) {
                    continue; 
                }

                double diffuseFactor = Math.max(0.0, n.dot(lightDirBuffer));
                
                totalR += diffuseFactor * lightColorBuffer.x * record.material.diffuseColor.x;
                totalG += diffuseFactor * lightColorBuffer.y * record.material.diffuseColor.y;
                totalB += diffuseFactor * lightColorBuffer.z * record.material.diffuseColor.z;

                halfwayBuffer.set(lightDirBuffer.x + viewDirBuffer.x, 
                                lightDirBuffer.y + viewDirBuffer.y, 
                                lightDirBuffer.z + viewDirBuffer.z);
                halfwayBuffer.normalize();

                double specularFactor = Math.max(0.0, n.dot(halfwayBuffer));
                double specularSpecular = Math.pow(specularFactor, record.material.shininess);

                totalR += specularSpecular * lightColorBuffer.x * record.material.specularColor.x;
                totalG += specularSpecular * lightColorBuffer.y * record.material.specularColor.y;
                totalB += specularSpecular * lightColorBuffer.z * record.material.specularColor.z;
            }

            resultColor.set(totalR, totalG, totalB);
            return;
        }

        double t = 0.5 * (ray.direction.y + 1.0);
        double r = (1.0 - t) * 1.0 + t * 0.5;
        double g = (1.0 - t) * 1.0 + t * 0.7;
        double b = (1.0 - t) * 1.0 + t * 1.0;

        resultColor.set(r, g, b);
    }

}
