package com.to_binio.gameObj;

import com.badlogic.gdx.math.Vector2;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public abstract class GameObj {
    protected final Vector2 location;

    public GameObj(float x, float y) {
        this.location = new Vector2(x, y);
    }

    public Vector2 getLocation() {
        return new Vector2(location);
    }
}
