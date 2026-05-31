package edu.up.mateosanchez.lights;

import edu.up.mateosanchez.math.Vector3d;

public class PointLight implements Light{
    public Vector3d position, color;

    public PointLight(Vector3d position, Vector3d color) {
        this.position = position;
        this.color = color;
    }

    @Override
    public void getColor(Vector3d resultColor) {
        resultColor.set(this.color.x, this.color.y, this.color.z);
    }

    @Override
    public void getDirection(Vector3d point, Vector3d resultDirection) {
        double distance = this.getDistance(point);
        double dX = (this.position.x - point.x) / distance;
        double dY = (this.position.y - point.y) / distance;
        double dZ = (this.position.z - point.z) / distance;
        resultDirection.set(dX, dY, dZ);
    }

    @Override
    public double getDistance(Vector3d point) {
        double dX = this.position.x - point.x;
        double dY = this.position.y - point.y;
        double dZ = this.position.z - point.z;
        return Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }
    
    
}
