package at.tobinio.utils;

import com.badlogic.gdx.math.Vector2;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public class Maths {

    public static boolean isPointInFOV(Vector2 point, Vector2 circle, float deg, float degOff) {
        Vector2 difVector = new Vector2(point).sub(circle);

        float normalizedDifAngle = (difVector.angleDeg() - deg + 360 + 180) % 360 - 180;

        return normalizedDifAngle >= -degOff && normalizedDifAngle <= degOff;
    }
}
