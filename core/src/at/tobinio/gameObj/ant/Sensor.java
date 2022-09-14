package at.tobinio.gameObj.ant;

import at.tobinio.Variables;
import at.tobinio.gameObj.GameObj;
import at.tobinio.gameObj.ant.pheromon.PheromonType;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created: 19.06.2022
 *
 * @author Tobias Frischmann
 */

public class Sensor extends GameObj {

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

        location.x = MathUtils.cosDeg(realAngle) * Variables.Ant.Sensor.DISTANCE_TO_ANT + ant.getLocation().x;
        location.y = MathUtils.sinDeg(realAngle) * Variables.Ant.Sensor.DISTANCE_TO_ANT + ant.getLocation().y;
    }

    public void reSumOfPheromons(PheromonType pheromonType) {
        rePosition();
        sum = ant.getColony().sumOfPheromons(pheromonType, location.x, location.y, Variables.Ant.Sensor.RANGE);
    }

    public float getSum() {
        return sum;
    }
}
