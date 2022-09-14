package at.tobinio.map;

import at.tobinio.NeighborFinder;
import at.tobinio.Variables;
import at.tobinio.gameObj.Food;
import at.tobinio.gameObj.GameObj;
import at.tobinio.gameObj.ant.Ant;
import at.tobinio.ray.rayCaster.RayCaster;
import at.tobinio.ray.rayCatcher.RayCatcherLine;
import at.tobinio.spacialHashmap.SpacialHashmap;
import at.tobinio.utils.Maths;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public class Map {

    public static class GameObjs extends Thread {
        private final static List<Food> foods = new ArrayList<>();
        private final static List<Food> foodsToAdd = new ArrayList<>();
        private final static List<Food> foodsToRemove = new ArrayList<>();
        private final static NeighborFinder<Food> foodsNeighborFinder;

        private final static List<Colony> colonies = new ArrayList<>();

        static {
            foodsNeighborFinder = new SpacialHashmap<>(-Variables.Game.WIDTH / 2.0, Variables.Game.WIDTH / 2.0, -Variables.Game.HEIGHT / 2.0, Variables.Game.HEIGHT / 2.0, 100, 100);
        }

        public static void addColony(Colony colony) {
            colonies.add(colony);
        }

        public static void addFood(Food food) {
            foodsToAdd.add(food);
        }

        public static void removeFood(Food food) {
            foodsToRemove.add(food);
        }

        public static void update() {

            //[>-------------------
            // Colonies
            //[>-------------------

            for (Colony colony : colonies) {
                colony.update();
            }

            //[>-------------------
            // food
            //[>-------------------

            for (Food food : foodsToRemove) {
                foods.remove(food);
            }
            foodsToRemove.clear();

            foods.addAll(foodsToAdd);
            foodsToAdd.clear();

            foodsNeighborFinder.clear();
            for (Food food : foods) {
                foodsNeighborFinder.add(food);
            }
        }

        public static void render(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer, Sprite antSprite) {

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            shapeRenderer.setColor(new Color(0, 0.7f, 0, 1));
            for (Food food : foods) {
                food.renderer(shapeRenderer);
            }

            if (Variables.Game.TOGGLE_RENDER_PHEROMONS) for (Colony colony : colonies) {
                colony.renderPheromons(shapeRenderer);
            }

            shapeRenderer.end();

            spriteBatch.begin();

            for (Colony colony : colonies) {
                colony.renderAntBody(spriteBatch, antSprite);
            }

            spriteBatch.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            shapeRenderer.setColor(new Color(0, 0.7f, 0, 1));
            for (Colony colony : colonies) {
                colony.renderAntFood(shapeRenderer);
            }

            shapeRenderer.setColor(new Color(139 / 255f, 69 / 255f, 32 / 255f, 1));
            for (Colony colony : colonies) {
                colony.renderNest(shapeRenderer);
            }

            if (Variables.Game.TOGGLE_RENDER_ANT_FOV) {

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shapeRenderer.setColor(new Color(1, 1, 0, 0.2f));

                for (Colony colony : colonies) {
                    colony.renderAntFOV(shapeRenderer);
                }
            }

            shapeRenderer.end();
        }

        public static <T extends GameObj> T getNearestGameObjInDistance(float x, float y, float range,
                NeighborFinder<T> finder) {

            float nearestLength = -1;
            T nearestGameObj = null;

            List<T> possibleObj;

            possibleObj = finder.getInCircle(x, y, range);

            for (T gameObj : possibleObj) {
                float distanceToFood = Vector2.dst(gameObj.getLocation().x, gameObj.getLocation().y, x, y);

                if (distanceToFood < range && (nearestLength == -1 || distanceToFood < nearestLength)) {
                    nearestLength = distanceToFood;
                    nearestGameObj = gameObj;
                }
            }

            return nearestGameObj;
        }

        public static <T extends GameObj> T getNearestGameObjInFOV(float x, float y, float range,
                NeighborFinder<T> finder, float deg, float degOff) {

            float nearestLength = -1;
            T nearestGameObj = null;

            List<T> possibleObj;

            possibleObj = finder.getInCircle(x, y, range);

            for (T gameObj : possibleObj) {
                float distanceToFood = Vector2.dst(gameObj.getLocation().x, gameObj.getLocation().y, x, y);

                if (distanceToFood < range && Maths.isPointInFOV(gameObj.getLocation(), new Vector2(x, y), deg, degOff) && (nearestLength == -1 || distanceToFood < nearestLength)) {
                    nearestLength = distanceToFood;
                    nearestGameObj = gameObj;
                }
            }

            return nearestGameObj;
        }

        public static Food getNearestFoodInFOV(Ant ant) {
            return getNearestGameObjInFOV(ant.getLocation().x, ant.getLocation().y, Variables.Ant.VIEWING_DISTANCE, foodsNeighborFinder, ant.getDir(), Variables.Ant.VIEWiNG_ANGLE / 2f);
        }

        public static Ant getNearestEnemyAntInFOV(Ant ant) {
            Ant nearestAnt = null;

            for (Colony colony : colonies) {
                if (colony == ant.getColony()) continue;

                Ant nearestAntInFOV = colony.getNearestAntInFOV(ant);
                if (nearestAnt == null || (nearestAntInFOV != null && nearestAntInFOV.getLocation().dst(ant.getLocation()) < nearestAnt.getLocation().dst(ant.getLocation()))) {
                    nearestAnt = nearestAntInFOV;
                }
            }

            return nearestAnt;
        }


        public static void spawnRandomFoodCluster() {
            Vector2 location = Structure.getRandomValidLocation();

            spawnRandomFoodCluster(location.x, location.y);
        }

        public static void spawnRandomFoodCluster(float x, float y) {
            for (int i = 0; i < 100; i++) {

                float angle = (float) (Math.random() * Math.PI * 2);
                float r = (float) Math.random() * Variables.Map.CELL_SIZE;

                Map.GameObjs.addFood(new Food(MathUtils.cos(angle) * r + x, MathUtils.sin(angle) * r + y));
            }
        }

        public static int getAntCount() {

            int sum = 0;

            for (Colony colony : colonies) {
                sum += colony.getAntCount();
            }

            return sum;
        }

        public static int getPheromonCount() {

            int sum = 0;

            for (Colony colony : colonies) {
                sum += colony.getPheromonCount();
            }

            return sum;
        }
    }

    public static class Structure {

        private final static Cell[][] cellMap;
        public static final MarchingCube[][] marchingCubeMap;

        private static final TextureRegion textureRegion;

        public static Line[] borders;
        public static RayCaster borderRayCaster;

        private static class Line {
            public Vector2 pointA;
            public Vector2 pointB;

            public Line(Vector2 pointA, Vector2 pointB) {
                this.pointA = pointA;
                this.pointB = pointB;
            }
        }

        private static class Cell {
            public enum StructureType {
                AIR, STONE
            }

            public StructureType type;
            public boolean hasRoom;

            public Cell copy() {
                Cell cell = new Cell();

                cell.type = this.type;

                return cell;
            }

            public void addCellsToRoom(int x, int y, Room room) {
                if (hasRoom || type == StructureType.STONE) return;

                room.addCell(this);
                hasRoom = true;

                cellMap[x + 1][y].addCellsToRoom(x + 1, y, room);
                cellMap[x - 1][y].addCellsToRoom(x - 1, y, room);
                cellMap[x][y + 1].addCellsToRoom(x, y + 1, room);
                cellMap[x][y - 1].addCellsToRoom(x, y - 1, room);
            }
        }

        private static class Room {
            private final List<Cell> cellList;

            public Room() {
                cellList = new ArrayList<>();
            }

            public void addCell(Cell cell) {
                cellList.add(cell);
            }

            public void fill() {
                for (Cell cell : cellList) {
                    cell.type = Cell.StructureType.STONE;
                }
            }
        }

        static {
            //[>-------------------
            // rendering
            //[>-------------------

            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(92 / 255f, 64 / 255f, 51 / 255f, 1);
            pix.fill();

            Texture texture = new Texture(pix);
            textureRegion = new TextureRegion(texture);

            //[>-------------------
            // data
            //[>-------------------

            cellMap = new Cell[Variables.Game.WIDTH / Variables.Map.CELL_SIZE + 1][];
            marchingCubeMap = new MarchingCube[cellMap.length - 1][];

            for (int i = 0; i < cellMap.length; i++) {
                cellMap[i] = new Cell[Variables.Game.HEIGHT / Variables.Map.CELL_SIZE + 1];
                for (int j = 0; j < cellMap[i].length; j++) {
                    cellMap[i][j] = new Cell();
                }
            }

            for (int i = 0; i < marchingCubeMap.length; i++) {
                marchingCubeMap[i] = new MarchingCube[cellMap[0].length - 1];
            }

            //init cellMap
            for (int x = 0; x < cellMap.length; x++) {
                for (int y = 0; y < cellMap[x].length; y++) {

                    if (x < 5 || y < 5 || x >= cellMap.length - 5 || y >= cellMap[0].length - 5) {
                        cellMap[x][y].type = Cell.StructureType.STONE;
                    } else {
                        cellMap[x][y].type = Math.random() > 0.46 ? Cell.StructureType.AIR : Cell.StructureType.STONE;
                    }
                }
            }
        }

        public static void render(PolygonSpriteBatch polygonSpriteBatch) {
            polygonSpriteBatch.begin();

            for (MarchingCube[] marchingCubes : marchingCubeMap) {
                for (MarchingCube marchingCube : marchingCubes) {

                    if (marchingCube == null) continue;

                    new PolygonSprite(new PolygonRegion(textureRegion, marchingCube.vertices, marchingCube.shortArray.toArray())).draw(polygonSpriteBatch);
                }
            }

            polygonSpriteBatch.end();
        }

        private static Cell[][] getMapCopy() {
            Cell[][] copyMap = new Cell[cellMap.length][];

            for (int x = 0; x < copyMap.length; x++) {
                copyMap[x] = new Cell[cellMap[x].length];
                for (int i = 0; i < copyMap[x].length; i++) {
                    copyMap[x][i] = cellMap[x][i].copy();
                }
            }

            return copyMap;
        }

        private static int neighborCount(int x, int y, Cell[][] map) {

            int count = 0;

            for (int offX = -1; offX <= 1; offX++) {
                for (int offY = -1; offY <= 1; offY++) {

                    if (offX == offY && offX == 0) continue;
                    if (x + offX < 0 || x + offX >= map.length || y + offY < 0 || y + offY >= map[0].length) continue;

                    if (map[x + offX][y + offY].type == Cell.StructureType.AIR) count++;
                }
            }

            return count;
        }

        public static void compute() {

            List<Room> rooms = new ArrayList<>();

            for (int i = 0; i < 25; i++) {
                smoothStep();
            }

            //[>-------------------
            // find Rooms
            //[>-------------------

            Room currentRoom;

            for (int x = 0; x < cellMap.length; x++) {
                for (int y = 0; y < cellMap.length; y++) {
                    Cell cell = cellMap[x][y];
                    if (cell.hasRoom || cell.type == Cell.StructureType.STONE) continue;

                    currentRoom = new Room();
                    rooms.add(currentRoom);

                    cell.addCellsToRoom(x, y, currentRoom);
                }
            }

            //[>-------------------
            // Fill small Rooms
            //[>-------------------

            System.out.println("room filler:");

            for (Room room : rooms) {
                System.out.println(room.cellList.size());

                if (room.cellList.size() > 50) continue;
                room.fill();
            }

            // TODO: 01.07.2022 connect rooms
        }

        private static void smoothStep() {
            Cell[][] lookUpMap = getMapCopy();

            for (int x = 0; x < lookUpMap.length; x++) {
                for (int y = 0; y < lookUpMap[x].length; y++) {
                    int neighborCount = neighborCount(x, y, lookUpMap);

                    if (neighborCount < 4) {
                        cellMap[x][y].type = Cell.StructureType.STONE;
                    } else if (neighborCount > 4) {
                        cellMap[x][y].type = Cell.StructureType.AIR;
                    }
                }
            }
        }

        public static void bakeMarchingCubeMap() {

            ArrayList<Line> borders = new ArrayList<>();

            for (int x = 0; x < marchingCubeMap.length; x++) {
                for (int y = 0; y < marchingCubeMap[x].length; y++) {
                    MarchingCube marchingCube = new MarchingCube(Integer.parseInt("" + cellMap[x][y + 1].type.ordinal() + cellMap[x + 1][y + 1].type.ordinal() + cellMap[x + 1][y].type.ordinal() + cellMap[x][y].type.ordinal(), 2), x, y);

                    marchingCubeMap[x][y] = marchingCube;

                    for (int i = 0; i < marchingCube.borders.length; i += 2) {
                        borders.add(new Line(marchingCube.borders[i], marchingCube.borders[i + 1]));
                    }
                }
            }

            Map.Structure.borders = borders.toArray(new Line[0]);
        }

        public static boolean isValidSpawnLocation(float x, float y) {
            return neighborCount(Math.round((x + Variables.Game.WIDTH / 2f) / Variables.Game.WIDTH * ((float) Variables.Game.WIDTH / Variables.Map.CELL_SIZE)), Math.round((y + Variables.Game.HEIGHT / 2f) / Variables.Game.HEIGHT * ((float) Variables.Game.HEIGHT / Variables.Map.CELL_SIZE)), cellMap) == 8;
        }

        public static Vector2 getRandomValidLocation() {
            float x;
            float y;

            do {
                x = (float) Math.random() * Variables.Game.WIDTH - Variables.Game.WIDTH / 2f;
                y = (float) Math.random() * Variables.Game.WIDTH - Variables.Game.WIDTH / 2f;
            } while (!isValidSpawnLocation(x, y));

            return new Vector2(x, y);
        }

        public static void optimizeBorder() {

            System.out.println("border optimizer:");

            int before;
            do {
                before = borders.length;

                List<Line> newBorders = new ArrayList<>();
                List<Line> usedLines = new ArrayList<>();

                for (Line border : borders) {
                    optimizeBorderLine(border, newBorders, usedLines);
                }

                borders = newBorders.toArray(new Line[]{});
                System.out.println(before + " ->" + borders.length);

            } while (before != borders.length);
        }

        private static void optimizeBorderLine(Line border, List<Line> newBorders, List<Line> usedLines) {
            if (usedLines.contains(border)) return;

            for (Line newBorder : newBorders) {
                if (border.pointB.equals(newBorder.pointA)) {
                    Vector2 dir1 = new Vector2(border.pointA).sub(border.pointB).nor();
                    Vector2 dir2 = new Vector2(newBorder.pointA).sub(newBorder.pointB).nor();

                    if (Math.abs(dir1.x - dir2.x) < 0.1 && Math.abs(dir1.y - dir2.y) < 0.1) {
                        newBorders.remove(newBorder);
                        newBorders.add(new Line(border.pointA, newBorder.pointB));
                        usedLines.add(border);

                        return;
                    }
                }
            }

            for (Line currentBorder : borders) {
                if (border.pointB.equals(currentBorder.pointA)) {

                    Vector2 dir1 = new Vector2(border.pointA).sub(border.pointB).nor();
                    Vector2 dir2 = new Vector2(currentBorder.pointA).sub(currentBorder.pointB).nor();

                    if (Math.abs(dir1.x - dir2.x) < 0.1 && Math.abs(dir1.y - dir2.y) < 0.1) {

                        Line line = new Line(border.pointA, currentBorder.pointB);

                        newBorders.add(line);

                        usedLines.add(border);
                        usedLines.add(currentBorder);

                        return;
                    }
                }
            }

            newBorders.add(border);
            usedLines.add(border);
        }

        public static void bakeBorderRayCaster() {
            borderRayCaster = new RayCaster();

            for (Line border : borders) {
                borderRayCaster.addRayCaster(new RayCatcherLine(border.pointA.x, border.pointA.y, border.pointB.x, border.pointB.y));
            }

            borderRayCaster.bake();
        }

        public static void renderBorder(ShapeRenderer shapeRenderer) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (Line border : borders) {
                Random random = new Random((long) (border.pointA.y * border.pointA.x));

                shapeRenderer.setColor(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1));
                shapeRenderer.line(border.pointA, border.pointB);
                shapeRenderer.circle(border.pointA.x, border.pointA.y, 1);
            }
            shapeRenderer.end();
        }
    }

    public static void update() {
        GameObjs.update();
    }

    public static void render(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer, Sprite antSprite,
            PolygonSpriteBatch polygonSpriteBatch) {
        GameObjs.render(spriteBatch, shapeRenderer, antSprite);
        Structure.render(polygonSpriteBatch);
//        Structure.renderBorder(shapeRenderer);
    }
}
