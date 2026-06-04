package edu.up.mateosanchez.lights;

import edu.up.mateosanchez.math.Vector3d;

public class PointLight implements Light {
    public Vector3d position, color;
    
    // Coeficientes de atenuación añadidos
    public double constant = 1.0;
    public double linear = 0.09;
    public double quadratic = 0.032;

    public PointLight(Vector3d position, Vector3d color) {
        this.position = position;
        this.color = color;
    }

    // Nuevo constructor opcional por si deseas personalizar el alcance
    public PointLight(Vector3d position, Vector3d color, double constant, double linear, double quadratic) {
        this.position = position;
        this.color = color;
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    @Override
    public void getColor(Vector3d point, Vector3d resultColor) {
        double distance = this.getDistance(point);
        
        // Evitar división por cero
        if (distance == 0.0) {
            resultColor.set(this.color.x, this.color.y, this.color.z);
            return;
        }

        // Aplicamos la atenuación física real
        double atten = 1.0 / (this.constant + this.linear * distance + this.quadratic * distance * distance);
        
        resultColor.set(this.color.x * atten, this.color.y * atten, this.color.z * atten);
    }

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

    @Override
    public double getDistance(Vector3d point) {
        double dX = this.position.x - point.x;
        double dY = this.position.y - point.y;
        double dZ = this.position.z - point.z;
        return Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }
}