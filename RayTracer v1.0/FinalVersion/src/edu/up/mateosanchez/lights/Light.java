package edu.up.mateosanchez.lights;

import edu.up.mateosanchez.math.Vector3d;

public interface Light {
    void getDirection(Vector3d point, Vector3d resultDirection);
    void getColor(Vector3d point, Vector3d resultColor);
    double getDistance(Vector3d point);
}
