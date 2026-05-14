package edu.up.mateo.raytracer;

import java.awt.Color;
import java.util.ArrayList;

public class Scene {
    private ArrayList<Object3D> objects;
    private Camera camera;
    private Color bg = Color.BLACK;
    private ArrayList<Light> lights;
    
    public Scene(ArrayList<Object3D> objects, Camera camera, Color bg, ArrayList<Light> lights) {
        this.objects = objects;
        this.camera = camera;
        this.bg = bg;
        this.lights = lights;
    }

    public Scene(Camera camera) {
        this.camera = camera;
        this.objects = new ArrayList<>();
        this.bg = Color.BLACK;
        this.lights = new ArrayList<>();
    }

    public ArrayList<Object3D> getObjects() {
        return objects;
    }

    public void setObjects(ArrayList<Object3D> objects) {
        this.objects = objects;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Color getBg() {
        return bg;
    }

    public void setBg(Color bg) {
        this.bg = bg;
    }
    
    public void addObject(Object3D object){
        this.objects.add(object);
    }

    public ArrayList<Light> getLights() {
        return lights;
    }

    public void setLights(ArrayList<Light> lights) {
        this.lights = lights;
    }

    public void addLight(Light light){
        this.lights.add(light);
    }

}
