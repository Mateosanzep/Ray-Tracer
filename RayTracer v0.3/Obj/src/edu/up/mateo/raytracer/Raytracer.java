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
    
    public void render(){
        int width = image.getWidth();
        int height = image.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++){
                Ray ray = scene.getCamera().createRay(j, i);
                Color color = castRay(ray);
                image.setRGB(j, i, color.getRGB());
            }
        }
    }

    public Color castRay(Ray ray){
        Intersection closestIntersection = null;
        float minT = Float.MAX_VALUE;
        float near = scene.getCamera().near;
        float far = scene.getCamera().far;
        for (Object3D object : scene.getObjects()) {
            Intersection intersection = object.intersect(ray);
            if (intersection.hit && intersection.t < minT && intersection.t >= near && intersection.t <= far){
                minT = intersection.t;
                closestIntersection = intersection;
            }
        }
        if (closestIntersection == null){
            return scene.getBg();
        }
        return Color.GRAY;
    }
    public BufferedImage getImage() {
        return image;
    }
}
