package edu.up.mateo.raytracer;

public class Camera {
    public Vector3D origin;
    public int width, height;
    public float fieldfOfView;

    public Camera(Vector3D origin, int width, int height, float fieldfOfView) {
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.fieldfOfView = fieldfOfView;
    }

    public Ray createRay(int x, int y){
        float normX = (2f * ((x + 0.5f) / width) - 1f) * ((float) width / height) * (float) Math.tan(fieldfOfView / 2f);
        float normY = (1f - 2f * ((y + 0.5f) / height)) * (float) Math.tan(fieldfOfView / 2f);
        Vector3D P = new Vector3D(normX, normY, -1);
        Vector3D D = P.VectorSubstract(this.origin);
        Vector3D normD = D.Normalize();

        return new Ray(origin, normD);
    }
}