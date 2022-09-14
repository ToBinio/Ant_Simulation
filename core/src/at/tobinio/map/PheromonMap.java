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

    private final float[][][] pheromons;

    public PheromonMap(int xCount, int yCount) {
        pheromons = new float[xCount][yCount][2];
    }

    private int worldToGridX(float x) {
        return (int) (x + Variables.Game.WIDTH / 2.0) / Variables.Map.PHEROMON_CELL_SIZE;
    }

    private int worldToGridY(float y) {
        return (int) (y + Variables.Game.HEIGHT / 2.0) / Variables.Map.PHEROMON_CELL_SIZE;
    }

    private float gridToWorldX(int x) {
        return (float) (x * Variables.Map.PHEROMON_CELL_SIZE - Variables.Game.WIDTH / 2.0);
    }

    private float gridToWorldY(int y) {
        return (float) (y * Variables.Map.PHEROMON_CELL_SIZE - Variables.Game.WIDTH / 2.0);
    }


    public float sumOfPheromons(PheromonType pheromonType, float x, float y, float range) {

        float sum = 0;

        int size = (int) (range / Variables.Map.PHEROMON_CELL_SIZE);

        for (int xOffset = -size; xOffset <= size; xOffset++) {
            for (int yOffset = -size; yOffset <= size; yOffset++) {
                switch (pheromonType) {
                    case HOME_PATH -> sum += pheromons[worldToGridX(x) + xOffset][worldToGridY(y) + yOffset][0];

                    case FOOD_PATH -> sum += pheromons[worldToGridX(x) + xOffset][worldToGridY(y) + yOffset][1];

                }
            }
        }

        return sum;
    }

    public void setPheromon(float worldX, float worldY, PheromonType pheromonType, float strength) {
        float pheromonStrength = pheromons[worldToGridX(worldX)][worldToGridY(worldY)][pheromonType == PheromonType.HOME_PATH ? 0 : 1];

        pheromons[worldToGridX(worldX)][worldToGridY(worldY)][pheromonType == PheromonType.HOME_PATH ? 0 : 1] = Math.max(pheromonStrength, strength);
    }

    public void update() {
        for (float[][] pheromon : pheromons) {
            for (float[] values : pheromon) {
                for (int i = 0; i < values.length; i++) {
                    values[i] -= 0.00005;
                    values[i] = Math.max(0, values[i]);
                }
            }
        }
    }

    public void render(ShapeRenderer renderer) {
        for (int x = 0; x < pheromons.length; x++) {
            for (int y = 0; y < pheromons[x].length; y++) {

                if (pheromons[x][y][0] == 0 && pheromons[x][y][1] == 0) {
                    continue;
                }

                renderer.setColor(new Color(pheromons[x][y][0], pheromons[x][y][1], 0, 1));

                renderer.rect(gridToWorldX(x), gridToWorldY(y), Variables.Map.PHEROMON_CELL_SIZE, Variables.Map.PHEROMON_CELL_SIZE);
            }
        }
    }
}
