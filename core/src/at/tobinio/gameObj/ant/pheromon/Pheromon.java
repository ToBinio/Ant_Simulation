package at.tobinio.gameObj.ant.pheromon;

import at.tobinio.gameObj.GameObj;
import at.tobinio.map.Colony;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created: 19.06.2022
 *
 * @author Tobias Frischmann
 */

public class Pheromon extends GameObj {
    public final PheromonType type;


    private float strength;
    private long lastUpdateTime = -1;

    private Colony colony;


    public Pheromon(float x, float y, PheromonType type, float strength) {
        super(x, y);

        this.type = type;
        this.strength = strength;
    }

    public void update() {
        if (lastUpdateTime == -1) lastUpdateTime = System.nanoTime();

        float timeSinceLastUpdate = (System.nanoTime() - lastUpdateTime) / 1_000_000_000f;

        strength -= 0.00018 * ((timeSinceLastUpdate * timeSinceLastUpdate) / 1000 + 1);

        if (strength <= 0.01) colony.removePheromon(this);
    }

    public float getStrength() {
        return strength;
    }

    public void render(ShapeRenderer renderer) {
        renderer.circle(location.x, location.y, (float) Math.pow(strength, 1 / 2f) / 3, 8);
    }

    public void addPheromon(Pheromon pheromon) {
        strength += pheromon.getStrength() * Math.pow(strength + 0.5f, -0.9f);
        lastUpdateTime = System.nanoTime();
    }

    public void setColony(Colony colony) {
        if (this.colony == null) this.colony = colony;
    }
}
