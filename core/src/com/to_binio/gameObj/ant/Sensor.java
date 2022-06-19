package com.to_binio.gameObj.ant;

import com.badlogic.gdx.math.MathUtils;
import com.to_binio.Map;
import com.to_binio.gameObj.GameObj;
import com.to_binio.gameObj.ant.pheromon.PheromonType;

/**
 * Created: 19.06.2022
 *
 * @author Tobias Frischmann
 */

public class Sensor extends GameObj {

    private static final float DISTANCE_TO_ANT = Ant.VIEWING_DISTANCE / 2f;
    private static final float RANGE = Ant.VIEWING_DISTANCE / 2f;

    public final Ant ant;
    public final float angle;

    private float sum;

    public Sensor(Ant ant, float angle) {
        super(0, 0);

        this.ant = ant;
        this.angle = angle;
    }

    public void rePosition() {
        float realAngle = ant.getDir() + angle;

        location.x = MathUtils.cosDeg(realAngle) * DISTANCE_TO_ANT + ant.getLocation().x;
        location.y = MathUtils.sinDeg(realAngle) * DISTANCE_TO_ANT + ant.getLocation().y;
    }

    public void reSumOfPheromons(PheromonType pheromonType) {
        sum = Map.sumOfPheromons(pheromonType, location.x, location.y, RANGE);
    }

    public float getSum() {
        return sum;
    }
}
