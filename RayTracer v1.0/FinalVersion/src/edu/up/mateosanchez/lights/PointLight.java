package edu.up.mateosanchez.lights;

import edu.up.mateosanchez.math.Vector3d;

public class PointLight implements Light {
    public Vector3d position, color;
    
    // Attenuation coefficients
    public double constant = 1.0;
    public double linear = 0.09;
    public double quadratic = 0.032;

    public PointLight(Vector3d position, Vector3d color) {
        this.position = position;
        this.color = color;
    }

    public PointLight(Vector3d position, Vector3d color, double constant, double linear, double quadratic) {
        this.position = position;
        this.color = color;
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    // Calculate light color and intensity reaching a point using distance attenuation
    @Override
    public void getColor(Vector3d point, Vector3d resultColor) {
        double distance = this.getDistance(point);
        
        // Prevent division by zero
        if (distance == 0.0) {
            resultColor.set(this.color.x, this.color.y, this.color.z);
            return;
        }

        // Apply distance falloff attenuation formula
        double atten = 1.0 / (this.constant + this.linear * distance + this.quadratic * distance * distance);
        
        resultColor.set(this.color.x * atten, this.color.y * atten, this.color.z * atten);
    }

    // Get normalized direction vector pointing from the hit point to the light source position
    @Override
    public void getDirection(Vector3d point, Vector3d resultDirection) {
        double distance = this.getDistance(point);
        if (distance > 0.0) {
            double dX = (this.position.x - point.x) / distance;
            double dY = (this.position.y - point.y) / distance;
            double dZ = (this.position.z - point.z) / distance;
            resultDirection.set(dX, dY, dZ);
        } else {
            resultDirection.set(0.0, 1.0, 0.0);
        }
    }

    // Calculate distance between the light source position and the specified point
    @Override
    public double getDistance(Vector3d point) {
        double dX = this.position.x - point.x;
        double dY = this.position.y - point.y;
        double dZ = this.position.z - point.z;
        return Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }
}