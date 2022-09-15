package at.tobinio.map;

import at.tobinio.Variables;
import at.tobinio.gameObj.ant.pheromon.PheromonType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created: 14.09.2022
 *
 * @author Tobias Frischmann
 */

public class PheromonMap {

    private final float[][] pheromons;

    public PheromonMap(int xCount, int yCount) {
        pheromons = new float[xCount * yCount][2];
    }

    private int worldToGrid(float x, float y) {
        return ((int) (x + Variables.Game.WIDTH / 2.0) / Variables.Map.PHEROMON_CELL_SIZE) * (Variables.Game.WIDTH / Variables.Map.PHEROMON_CELL_SIZE) + (int) (y + Variables.Game.HEIGHT / 2.0) / Variables.Map.PHEROMON_CELL_SIZE;
    }

    private float gridToWorldX(int i) {
        return (float) ((i / (Variables.Game.WIDTH / Variables.Map.PHEROMON_CELL_SIZE)) * Variables.Map.PHEROMON_CELL_SIZE - Variables.Game.WIDTH / 2.0);
    }

    private float gridToWorldY(int i) {
        return (float) ((i % (Variables.Game.WIDTH / Variables.Map.PHEROMON_CELL_SIZE)) * Variables.Map.PHEROMON_CELL_SIZE - Variables.Game.WIDTH / 2.0);
    }


    public float sumOfPheromons(PheromonType pheromonType, float x, float y, float range) {

        float sum = 0;

        int size = (int) (range / Variables.Map.PHEROMON_CELL_SIZE);

        int pheromonCount = Variables.Game.WIDTH / Variables.Map.PHEROMON_CELL_SIZE;

        for (int xOffset = -size; xOffset <= size; xOffset++) {
            for (int yOffset = -size; yOffset <= size; yOffset++) {
                switch (pheromonType) {
                    case HOME_PATH -> sum += pheromons[worldToGrid(x, y) + xOffset * pheromonCount + yOffset][0];

                    case FOOD_PATH -> sum += pheromons[worldToGrid(x, y) + xOffset * pheromonCount + yOffset][1];
                }
            }
        }

        return sum;
    }

    public void setPheromon(float worldX, float worldY, PheromonType pheromonType, float strength) {
        float pheromonStrength = pheromons[worldToGrid(worldX, worldY)][pheromonType == PheromonType.HOME_PATH ? 0 : 1];

        pheromons[worldToGrid(worldX, worldY)][pheromonType == PheromonType.HOME_PATH ? 0 : 1] = Math.max(pheromonStrength, strength);
    }

    public void update() {


        for (float[] pheromon : pheromons) {
            for (int i = 0; i < pheromon.length; i++) {
                pheromon[i] -= 0.00005;
                pheromon[i] = Math.max(0, pheromon[i]);
            }
        }
    }

    public void render(ShapeRenderer renderer) {

        for (int i = 0; i < pheromons.length; i++) {
            if (pheromons[i][0] == 0 && pheromons[i][1] == 0) {
                continue;
            }

            renderer.setColor(new Color(pheromons[i][0], pheromons[i][1], 0, 1));

            renderer.rect(gridToWorldX(i), gridToWorldY(i), Variables.Map.PHEROMON_CELL_SIZE, Variables.Map.PHEROMON_CELL_SIZE);
        }
    }
}

