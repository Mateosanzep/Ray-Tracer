package edu.up.mateosanchez.lights;

import edu.up.mateosanchez.math.Vector3d;

public class DirectionalLight implements Light{
    public Vector3d direction, color;

    public DirectionalLight(Vector3d direction, Vector3d color) {
        this.direction = direction;
        this.color = color;
    }

    @Override
    public void getColor(Vector3d resultColor) {
        resultColor.set(this.color.x, this.color.y, this.color.z);
    }

    @Override
    public void getDirection(Vector3d point, Vector3d resultDirection) {
        double dX = (this.direction.x * -1.0);
        double dY = (this.direction.y * -1.0);
        double dZ = (this.direction.z * -1.0);
        resultDirection.set(dX, dY, dZ);
    }

    @Override
    public double getDistance(Vector3d point) {
        return Double.MAX_VALUE;
    }

    
}
