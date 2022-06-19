package com.to_binio.utils;

import com.badlogic.gdx.math.Vector2;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public class Maths {

    public static float isPointInCircleSection(Vector2 point, Vector2 circle, float radius, float deg, float degOff) {
        Vector2 difVector = new Vector2(circle).sub(point);

        float len = difVector.len();

        if (len > radius) return -1;

        float normalizedDifAngle = (difVector.angleDeg() - deg + 360 + 180) % 360 - 180;

        if (normalizedDifAngle >= -degOff && normalizedDifAngle <= degOff) {
            return len;
        }
        return -1;
    }
}
