package edu.up.mateosanchez.math;

public class Vector3d {
    public double x, y, z;

    public Vector3d() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setVector(Vector3d v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    // In-place vector addition
    public Vector3d add(Vector3d v){
        this.x = this.x + v.x;
        this.y = this.y + v.y;
        this.z = this.z + v.z;

        return this;
    }

    // In-place vector subtraction
    public Vector3d substract(Vector3d v){
        this.x = this.x - v.x;
        this.y = this.y - v.y;
        this.z = this.z - v.z;

        return this;
    }

    // In-place scalar multiplication
    public Vector3d scalarMultiply(double a){
        this.x = this.x * a;
        this.y = this.y * a;
        this.z = this.z * a;

        return this;
    }

    // Calculate the dot product between this vector and vector v
    public double dot(Vector3d v){
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    // Compute the geometric length (magnitude) of the vector
    public double magnitude(){
        double mag = Math.sqrt(dot(this));
        return mag;
    }

    // Convert the vector into a unit vector with a magnitude of 1.0 safely
    public void normalize(){
        double mag = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        if (mag == 0.0) {
            this.x = 0.0;
            this.y = 0.0;
            this.z = 0.0;
            return;
        }
        double inv = 1.0 / mag;
        this.x = this.x * inv;
        this.y = this.y * inv;
        this.z = this.z * inv;
    }

    // Compute the cross product with vector v and store it into the result vector
    public void crossProduct(Vector3d v, Vector3d result){
        result.x = (this.y * v.z) - (this.z * v.y);
        result.y = (this.z * v.x) - (this.x * v.z);
        result.z = (this.x * v.y) - (this.y * v.x);
    }
}