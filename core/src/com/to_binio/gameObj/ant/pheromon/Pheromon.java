package com.to_binio.gameObj.ant.pheromon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.to_binio.Map;
import com.to_binio.gameObj.GameObj;

/**
 * Created: 19.06.2022
 *
 * @author Tobias Frischmann
 */

public class Pheromon extends GameObj {

    public final PheromonType type;

    private float strength;
    private final float beginningStrength;
    private long spawnTime = -1;

    public Pheromon(float x, float y, PheromonType type, float strength) {
        super(x, y);

        this.type = type;
        this.beginningStrength = strength;
    }

    public void update() {
        if (spawnTime == -1) spawnTime = System.nanoTime();

        float timeSinceSpawn = (System.nanoTime() - spawnTime) / 1_000_000_000f;

        strength = beginningStrength * (0.5f / (timeSinceSpawn / 5 + 0.5f));

        if (strength <= 0.05) Map.removePheromon(this);
    }

    public float getStrength() {
        return strength;
    }

    public void render(ShapeRenderer renderer) {
        renderer.circle(location.x, location.y, strength / 2, 8);
    }

    public void addPheromon(Pheromon pheromon) {
        strength += pheromon.getStrength();
    }
}
