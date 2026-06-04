package edu.up.mateosanchez.core;

import edu.up.mateosanchez.camera.Camera;
import edu.up.mateosanchez.geometry.Intersectable;
import edu.up.mateosanchez.lights.Light;
import edu.up.mateosanchez.utils.ObjParser;

import java.util.ArrayList;

public class Scene {
    public ArrayList<Intersectable> objects;
    public ArrayList<Light> lights;
    public Camera camera;
    public double ambientIntensity;

    public Scene() {
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();
        this.ambientIntensity = 0.01;
    }

    public Scene(Camera camera, double ambientIntensity) {
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();
        this.camera = camera;
        this.ambientIntensity = ambientIntensity;
    }

    public void addObject(Intersectable object) {
        this.objects.add(object);
    }

    public void addObjects(ArrayList<Intersectable> newObjects) {
        this.objects.addAll(newObjects);
    }

    public void addLight(Light light) {
        this.lights.add(light);
    }

    // Import OBJ mesh with its materials into the scene
    public void importObj(String objFilePath) {
        ArrayList<Intersectable> imported = ObjParser.parse(objFilePath);
        addObjects(imported);
    }
}