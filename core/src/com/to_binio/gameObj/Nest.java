package com.to_binio.gameObj;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.to_binio.Map;
import com.to_binio.gameObj.ant.Ant;

/**
 * Created: 19.06.2022
 *
 * @author Tobias Frischmann
 */

public class Nest extends GameObj {

    public static final float SIZE = 9;

    public Nest(float x, float y) {
        super(x, y);
    }

    public void render(ShapeRenderer renderer) {
        renderer.circle(location.x, location.y, SIZE, 16);
    }

    public void spawnAnt() {
        Map.addAnt(new Ant(location.x, location.y, (float) (Math.random() * 360)));
    }
}
