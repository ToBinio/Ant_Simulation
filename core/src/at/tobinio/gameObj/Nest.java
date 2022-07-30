package at.tobinio.gameObj;

import at.tobinio.Variables;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created: 19.06.2022
 *
 * @author Tobias Frischmann
 */

public class Nest extends GameObj {

    public Nest(float x, float y) {
        super(x, y);
    }

    public void render(ShapeRenderer renderer, Color color, float foodLevel) {
        renderer.setColor(new Color(139 / 255f, 69 / 255f, 32 / 255f, 1));
        renderer.circle(location.x, location.y, Variables.Nest.SIZE, 32);

        renderer.setColor(color);
        renderer.arc(location.x, location.y, Variables.Nest.SIZE, 0, foodLevel * 360, 32);
    }
}
