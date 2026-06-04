package edu.up.mateosanchez.lights;

import edu.up.mateosanchez.math.Vector3d;

public class SpotLight implements Light {
    public Vector3d position;
    public Vector3d direction; // normalized
    public Vector3d color;
    public double innerCutoff; // in degrees
    public double outerCutoff; // in degrees
    
    private double cosInnerCutoff;
    private double cosOuterCutoff;
    
    // Attenuation coefficients
    public double constant = 1.0;
    public double linear = 0.09;
    public double quadratic = 0.032;

    public SpotLight(Vector3d position, Vector3d direction, Vector3d color, double innerCutoff, double outerCutoff) {
        this.position = position;
        this.color = color;
        this.innerCutoff = innerCutoff;
        this.outerCutoff = outerCutoff;
        
        // Normalize direction
        double mag = Math.sqrt(direction.x * direction.x + direction.y * direction.y + direction.z * direction.z);
        if (mag > 0.0) {
            this.direction = new Vector3d(direction.x / mag, direction.y / mag, direction.z / mag);
        } else {
            this.direction = new Vector3d(0.0, -1.0, 0.0);
        }
        
        this.cosInnerCutoff = Math.cos(Math.toRadians(innerCutoff));
        this.cosOuterCutoff = Math.cos(Math.toRadians(outerCutoff));
    }

    public SpotLight(Vector3d position, Vector3d direction, Vector3d color, double innerCutoff, double outerCutoff, double constant, double linear, double quadratic) {
        this(position, direction, color, innerCutoff, outerCutoff);
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    @Override
    public void getDirection(Vector3d point, Vector3d resultDirection) {
        // Direction from point to light source (standard for shading)
        double distance = this.getDistance(point);
        if (distance > 0.0) {
            resultDirection.set(
                (this.position.x - point.x) / distance,
                (this.position.y - point.y) / distance,
                (this.position.z - point.z) / distance
            );
        } else {
            resultDirection.set(0.0, 1.0, 0.0);
        }
    }

    @Override
    public void getColor(Vector3d point, Vector3d resultColor) {
        // Vector from light to point
        double dX = point.x - this.position.x;
        double dY = point.y - this.position.y;
        double dZ = point.z - this.position.z;
        double distance = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        
        if (distance == 0.0) {
            resultColor.set(0.0, 0.0, 0.0);
            return;
        }
        
        // Normalize vector from light to point
        double dirToPointX = dX / distance;
        double dirToPointY = dY / distance;
        double dirToPointZ = dZ / distance;
        
        // Dot product with spotlight direction
        double cosTheta = dirToPointX * this.direction.x + dirToPointY * this.direction.y + dirToPointZ * this.direction.z;
        
        double intensity = 0.0;
        if (cosTheta > this.cosInnerCutoff) {
            intensity = 1.0;
        } else if (cosTheta > this.cosOuterCutoff) {
            // Smooth interpolation (smoothstep)
            double t = (cosTheta - this.cosOuterCutoff) / (this.cosInnerCutoff - this.cosOuterCutoff);
            intensity = t * t * (3.0 - 2.0 * t);
        }
        
        // Attenuation
        double atten = 1.0 / (this.constant + this.linear * distance + this.quadratic * distance * distance);
        double finalIntensity = intensity * atten;
        
        resultColor.set(this.color.x * finalIntensity, this.color.y * finalIntensity, this.color.z * finalIntensity);
    }

    @Override
    public double getDistance(Vector3d point) {
        double dX = this.position.x - point.x;
        double dY = this.position.y - point.y;
        double dZ = this.position.z - point.z;
        return Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }
}
