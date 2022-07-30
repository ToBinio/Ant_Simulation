package at.tobinio.gameObj;

import at.tobinio.map.Map;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public class Food extends GameObj {

    private float volume;

    public Food(float x, float y) {
        super(x, y);

        volume = 1 + (float) Math.random();
    }

    public void renderer(ShapeRenderer renderer) {
        renderer.circle(location.x, location.y, volume, 8);
    }

    public float pickUp(float volume) {
        this.volume -= volume;

        if (this.volume <= 0) {

            Map.GameObjs.removeFood(this);

            return volume + this.volume;
        }

        return volume;
    }
}
