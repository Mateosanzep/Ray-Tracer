package edu.up.mateosanchez.lights;

import edu.up.mateosanchez.math.Vector3d;

public class AreaLight implements Light {
    public Vector3d corner;
    public Vector3d u;
    public Vector3d v;
    public Vector3d color;
    public Vector3d normal;
    
    private final ThreadLocal<Vector3d> currentSamplePoint = ThreadLocal.withInitial(Vector3d::new);
    
    public double constant = 1.0;
    public double linear = 0.09;
    public double quadratic = 0.032;

    public AreaLight(Vector3d corner, Vector3d u, Vector3d v, Vector3d color) {
        this.corner = corner;
        this.u = u;
        this.v = v;
        this.color = color;
        
        this.normal = new Vector3d();
        u.crossProduct(v, this.normal);
        this.normal.normalize();
    }

    public AreaLight(Vector3d corner, Vector3d u, Vector3d v, Vector3d color, double constant, double linear, double quadratic) {
        this(corner, u, v, color);
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    @Override
    public void getDirection(Vector3d point, Vector3d resultDirection) {
        // CORRECCIÓN: Muestreo fijo en el centro (0.5) en lugar de Math.random()
        // Esto elimina el ruido de Monte Carlo si no utilizas multisampling.
        double r1 = 0.5; 
        double r2 = 0.5;
        
        Vector3d sample = this.currentSamplePoint.get();
        sample.set(
            this.corner.x + r1 * this.u.x + r2 * this.v.x,
            this.corner.y + r1 * this.u.y + r2 * this.v.y,
            this.corner.z + r1 * this.u.z + r2 * this.v.z
        );
        
        double dX = sample.x - point.x;
        double dY = sample.y - point.y;
        double dZ = sample.z - point.z;
        double distance = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        
        if (distance > 0.0) {
            resultDirection.set(dX / distance, dY / distance, dZ / distance);
        } else {
            resultDirection.set(0.0, 1.0, 0.0);
        }
    }

    @Override
    public void getColor(Vector3d point, Vector3d resultColor) {
        Vector3d sample = this.currentSamplePoint.get();
        
        double dX = point.x - sample.x;
        double dY = point.y - sample.y;
        double dZ = point.z - sample.z;
        double distance = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        
        if (distance == 0.0) {
            resultColor.set(0.0, 0.0, 0.0);
            return;
        }
        
        double toPointX = dX / distance;
        double toPointY = dY / distance;
        double toPointZ = dZ / distance;
        
        double cosAlpha = toPointX * this.normal.x + toPointY * this.normal.y + toPointZ * this.normal.z;
        
        if (cosAlpha <= 0.0) {
            resultColor.set(0.0, 0.0, 0.0);
            return;
        }
        
        double atten = 1.0 / (this.constant + this.linear * distance + this.quadratic * distance * distance);
        double intensity = cosAlpha * atten;
        
        resultColor.set(this.color.x * intensity, this.color.y * intensity, this.color.z * intensity);
    }

    @Override
    public double getDistance(Vector3d point) {
        Vector3d sample = this.currentSamplePoint.get();
        double dX = sample.x - point.x;
        double dY = sample.y - point.y;
        double dZ = sample.z - point.z;
        return Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }
}