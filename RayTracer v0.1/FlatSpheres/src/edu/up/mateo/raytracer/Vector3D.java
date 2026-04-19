package edu.up.mateo.raytracer;

public class Vector3D {

    public float X;
    public float Y;
    public float Z;

    public Vector3D(float x, float y, float z){
        this.X = x;
        this.Y = y;
        this.Z = z;
    }

    public Vector3D VectorSubstract(Vector3D vec2){
        float newX = this.X - vec2.X;
        float newY = this.Y - vec2.Y;
        float newZ = this.Z - vec2.Z;

        return new Vector3D(newX, newY, newZ);
    }

    public float DotProduct(Vector3D vec2){
        float newX = this.X * vec2.X;
        float newY = this.Y * vec2.Y;
        float newZ = this.Z * vec2.Z;
        float sum = newX + newY + newZ;

        return sum;
    }

    public float Magnitude(){
        float mag = (float)Math.sqrt(DotProduct(this));

        return mag;
    }

    public Vector3D Normalize(){
        float mag = Magnitude();
        float newX = this.X / mag;
        float newY = this.Y / mag;
        float newZ = this.Z / mag;

        return new Vector3D(newX, newY, newZ);
    }

}
