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
        Vector3D hitPoint;
        if (closestIntersection.point != null) {
            hitPoint = closestIntersection.point;
        } else {
            hitPoint = new Vector3D(
                ray.O.X + ray.D.X * minT,
                ray.O.Y + ray.D.Y * minT,
                ray.O.Z + ray.D.Z * minT
            );
        }

        Vector3D N = (closestIntersection.normal != null) ? closestIntersection.normal : closestIntersection.object.getNormal(hitPoint);

        Color objectColor = closestIntersection.object.color;

        float ambientCoeff = 0f;
        float specularCoeff = 0.5f;
        float shininess = 32f;

        float totalR = ambientCoeff * (objectColor.getRed() / 255f);
        float totalG = ambientCoeff * (objectColor.getGreen() / 255f);
        float totalB = ambientCoeff * (objectColor.getBlue() / 255f);

        Vector3D V = scene.getCamera().origin.VectorSubstract(hitPoint).Normalize();

        for (Light light : scene.getLights()) {
            if (!light.illuminates(hitPoint)) continue;

            Vector3D L = light.getDirection(hitPoint);
            if (L == null) continue;

            float lightDistance = Float.MAX_VALUE;
            Vector3D lightDir = L;
            if (light instanceof PointLight) {
                Vector3D toLight = ((PointLight)light).position.VectorSubstract(hitPoint);
                lightDistance = toLight.Magnitude();
                lightDir = toLight.Normalize();
            } else if (light instanceof Spotlight) {
                Vector3D toLight = ((Spotlight)light).position.VectorSubstract(hitPoint);
                lightDistance = toLight.Magnitude();
                lightDir = toLight.Normalize();
            } else {
                lightDir = L.Normalize();
            }

            // shadow check
            float shadowEps = 1e-4f;
            Vector3D shadowOrigin = hitPoint.VectorAdd(N.ScalarMultiply(shadowEps));
            Ray shadowRay = new Ray(shadowOrigin, lightDir);
            boolean inShadow = false;
            for (Object3D obj : scene.getObjects()) {
                Intersection si = obj.intersect(shadowRay);
                if (si.hit && si.t > shadowEps) {
                    if (lightDistance == Float.MAX_VALUE || si.t < lightDistance - shadowEps) {
                        inShadow = true;
                        break;
                    }
                }
            }
            if (inShadow) continue;

            float nDotL = Math.max(0f, N.DotProduct(lightDir));

            float lightR = (light.lightColor.getRed() / 255f) * light.intensity;
            float lightG = (light.lightColor.getGreen() / 255f) * light.intensity;
            float lightB = (light.lightColor.getBlue() / 255f) * light.intensity;

            float attenuation = 1f;
            if (light instanceof PointLight) {
                PointLight pl = (PointLight)light;
                float d = lightDistance;
                attenuation = 1f / (pl.attConst + pl.attLinear * d + pl.attQuadratic * d * d);
            } else if (light instanceof Spotlight) {
                Spotlight sl = (Spotlight)light;
                float d = lightDistance;
                attenuation = 1f / (sl.attConst + sl.attLinear * d + sl.attQuadratic * d * d);
            }

            totalR += attenuation * lightR * (objectColor.getRed() / 255f) * nDotL;
            totalG += attenuation * lightG * (objectColor.getGreen() / 255f) * nDotL;
            totalB += attenuation * lightB * (objectColor.getBlue() / 255f) * nDotL;

            float nDotLForRef = N.DotProduct(lightDir);
            Vector3D R = N.ScalarMultiply(2f * nDotLForRef).VectorSubstract(lightDir).Normalize();
            float rDotV = Math.max(0f, R.DotProduct(V));
            float spec = (float)Math.pow(rDotV, shininess);

            totalR += attenuation * lightR * specularCoeff * spec;
            totalG += attenuation * lightG * specularCoeff * spec;
            totalB += attenuation * lightB * specularCoeff * spec;
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