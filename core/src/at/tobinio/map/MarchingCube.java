package at.tobinio.map;

import at.tobinio.Variables;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created: 27.06.2022
 *
 * @author Tobias Frischmann
 */

public class MarchingCube {

    public final float[] vertices;
    public final Vector2[] borders;
    public final ShortArray shortArray;

    public MarchingCube(int id, int x, int y) {
        Points[][] points = getInstance(id);
        Points[] verticesPoints = points[0];
        Points[] borderPoints = points[1];

        vertices = new float[verticesPoints.length * 2];

        for (int i = 0; i < verticesPoints.length; i++) {
            vertices[i * 2] = verticesPoints[i].x * Variables.Map.CELL_SIZE + x * Variables.Map.CELL_SIZE - Variables.Game.WIDTH / 2f;
            vertices[i * 2 + 1] = verticesPoints[i].y * Variables.Map.CELL_SIZE + y * Variables.Map.CELL_SIZE - Variables.Game.HEIGHT / 2f;
        }

        borders = new Vector2[borderPoints.length];

        for (int i = 0; i < borderPoints.length; i++) {
            borders[i] = new Vector2(borderPoints[i].x * Variables.Map.CELL_SIZE + x * Variables.Map.CELL_SIZE - Variables.Game.WIDTH / 2f, borderPoints[i].y * Variables.Map.CELL_SIZE + y * Variables.Map.CELL_SIZE - Variables.Game.HEIGHT / 2f);
        }

        shortArray = new EarClippingTriangulator().computeTriangles(vertices);
    }

    public enum Points {
        topLeft(0, 1), centreTop(0.5f, 1), topRight(1, 1), centreLeft(0, 0.5f), centreRight(1, 0.5f), bottomLeft(0, 0), centreBottom(0.5f, 0), bottomRight(1, 0);

        public final float x;
        public final float y;

        Points(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static Points[][] getInstance(int id) {
        return switch (id) {
            // 1 points:
            case 1 ->
                    new Points[][]{{Points.centreBottom, Points.bottomLeft, Points.centreLeft}, {Points.centreBottom, Points.centreLeft}};
            case 2 ->
                    new Points[][]{{Points.centreRight, Points.bottomRight, Points.centreBottom}, {Points.centreRight, Points.centreBottom}};
            case 4 ->
                    new Points[][]{{Points.centreTop, Points.topRight, Points.centreRight}, {Points.centreTop, Points.centreRight}};
            case 8 ->
                    new Points[][]{{Points.topLeft, Points.centreTop, Points.centreLeft}, {Points.centreTop, Points.centreLeft}};

            // 2 points:
            case 3 ->
                    new Points[][]{{Points.centreRight, Points.bottomRight, Points.bottomLeft, Points.centreLeft}, {Points.centreRight, Points.centreLeft}};
            case 6 ->
                    new Points[][]{{Points.centreTop, Points.topRight, Points.bottomRight, Points.centreBottom}, {Points.centreTop, Points.centreBottom}};
            case 9 ->
                    new Points[][]{{Points.topLeft, Points.centreTop, Points.centreBottom, Points.bottomLeft}, {Points.centreTop, Points.centreBottom}};
            case 12 ->
                    new Points[][]{{Points.topLeft, Points.topRight, Points.centreRight, Points.centreLeft}, {Points.centreRight, Points.centreLeft}};
            case 5 ->
                    new Points[][]{{Points.centreTop, Points.topRight, Points.centreRight, Points.centreBottom, Points.bottomLeft, Points.centreLeft}, {Points.centreTop, Points.centreLeft, Points.centreRight, Points.centreBottom}};
            case 10 ->
                    new Points[][]{{Points.topLeft, Points.centreTop, Points.centreRight, Points.bottomRight, Points.centreBottom, Points.centreLeft}, {Points.centreTop, Points.centreRight, Points.centreLeft, Points.centreBottom}};

            // 3 point:
            case 7 ->
                    new Points[][]{{Points.centreTop, Points.topRight, Points.bottomRight, Points.bottomLeft, Points.centreLeft}, {Points.centreTop, Points.centreLeft}};
            case 11 ->
                    new Points[][]{{Points.topLeft, Points.centreTop, Points.centreRight, Points.bottomRight, Points.bottomLeft}, {Points.centreTop, Points.centreRight}};
            case 13 ->
                    new Points[][]{{Points.topLeft, Points.topRight, Points.centreRight, Points.centreBottom, Points.bottomLeft}, {Points.centreRight, Points.centreBottom}};
            case 14 ->
                    new Points[][]{{Points.topLeft, Points.topRight, Points.bottomRight, Points.centreBottom, Points.centreLeft}, {Points.centreBottom, Points.centreLeft}};

            // 4 point:
            case 15 -> new Points[][]{{Points.topLeft, Points.topRight, Points.bottomRight, Points.bottomLeft}, {}};

            default -> new Points[][]{{}, {}};
        };
    }
}
