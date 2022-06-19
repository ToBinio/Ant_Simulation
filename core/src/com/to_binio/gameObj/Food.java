package com.to_binio.gameObj;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public class Food extends GameObj {

    public Food(float x, float y) {
        super(x, y);
    }

    public void renderer(ShapeRenderer renderer) {
        renderer.circle(location.x, location.y, 0.5f, 8);
    }
}
