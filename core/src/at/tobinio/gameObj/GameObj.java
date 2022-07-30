package at.tobinio.gameObj;

import at.tobinio.Position;
import com.badlogic.gdx.math.Vector2;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public abstract class GameObj implements Position {
    protected final Vector2 location;

    public GameObj(float x, float y) {
        this.location = new Vector2(x, y);
    }

    public Vector2 getLocation() {
        return location;
    }

    @Override
    public double getX() {
        return location.x;
    }

    @Override
    public double getY() {
        return location.y;
    }
}
