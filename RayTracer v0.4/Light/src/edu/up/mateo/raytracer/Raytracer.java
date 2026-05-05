package edu.up.mateo.raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Raytracer {
    public Scene scene;
    public BufferedImage image;

    public Raytracer(Scene scene) {
        this.scene = scene;
        int width = scene.getCamera().width;
        int height = scene.getCamera().height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void render() {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Ray ray = scene.getCamera().createRay(j, i);
                Color color = castRay(ray);
                image.setRGB(j, i, color.getRGB());
            }
        }
    }

    public Color castRay(Ray ray) {
        Intersection closestIntersection = null;
        float minT = Float.MAX_VALUE;
        float near = scene.getCamera().near;
        float far = scene.getCamera().far;

        for (Object3D object : scene.getObjects()) {
            Intersection intersection = object.intersect(ray);
            if (intersection.hit && intersection.t < minT && intersection.t >= near && intersection.t <= far) {
                minT = intersection.t;
                closestIntersection = intersection;
            }
        }

        if (closestIntersection == null) {
            return scene.getBg();
        }

        Vector3D hitPoint = new Vector3D(
            ray.O.X + ray.D.X * minT,
            ray.O.Y + ray.D.Y * minT,
            ray.O.Z + ray.D.Z * minT
        );

        Vector3D N = closestIntersection.object.getNormal(hitPoint);
        
        Color objectColor = closestIntersection.object.color;
        float totalR = 0, totalG = 0, totalB = 0;

        for (Light light : scene.getLights()) {
            Vector3D L = light.position.VectorSubstract(hitPoint);
            L.Normalize();

            Vector3D lightDir = new Vector3D(-light.direction.X, -light.direction.Y, -light.direction.Z);
            lightDir.Normalize();
            
            float angleCos = L.DotProduct(lightDir);
            
            if (angleCos > light.cutoff) {
                float nDotL = Math.max(0, N.DotProduct(L));
                
                totalR += (light.lightColor.getRed() / 255f) * (objectColor.getRed() / 255f) * light.intensity * nDotL;
                totalG += (light.lightColor.getGreen() / 255f) * (objectColor.getGreen() / 255f) * light.intensity * nDotL;
                totalB += (light.lightColor.getBlue() / 255f) * (objectColor.getBlue() / 255f) * light.intensity * nDotL;
            }
        }

        int r = (int)(Math.min(1f, totalR) * 255);
        int g = (int)(Math.min(1f, totalG) * 255);
        int b = (int)(Math.min(1f, totalB) * 255);

        return new Color(r, g, b);
    }

    public BufferedImage getImage() {
        return image;
    }
}