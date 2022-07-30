package at.tobinio.gameObj.ant;

import at.tobinio.Variables;
import at.tobinio.gameObj.Food;
import at.tobinio.gameObj.GameObj;
import at.tobinio.gameObj.ant.pheromon.Pheromon;
import at.tobinio.gameObj.ant.pheromon.PheromonType;
import at.tobinio.map.Colony;
import at.tobinio.map.Map;
import at.tobinio.utils.Maths;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public class Ant extends GameObj {

    // TODO: 01.07.2022 handle Viewing distance change based on size

    private float dir;
    private float goalDir;

    private float pickedUpFood;

    private float speed;

    private int lastPheromonSpawn = 0;
    private int pheromonsToSpawnCount = Variables.Ant.PHEROMON_MAX_TO_SPAWN;

    private final Sensor sensorUp;
    private final Sensor sensorLeft;
    private final Sensor sensorRight;

    private Colony colony;

    private final float size;
    private final float maxFoodCapacity;

    private GameObj target;

    public Ant(float x, float y, float dir, float size) {
        super(x, y);

        this.dir = dir;
        goalDir = dir;

        speed = Variables.Ant.MAX_SPEED;

        sensorUp = new Sensor(this, 0);
        sensorLeft = new Sensor(this, -30);
        sensorRight = new Sensor(this, 30);

        this.size = size;
        this.maxFoodCapacity = Variables.Ant.MAX_FOOD_CAPACITY * size;
    }

    public void update() {

        //[>-------------------
        // Handle Food
        //[>-------------------

        if (pickedUpFood >= maxFoodCapacity) {
            handleHasFood();
        } else {
            handleHasNoFood();
        }

        //[>-------------------
        // Compute Pheromons
        //[>-------------------

        if (target == null) {
            sensorUp.rePosition();
            sensorLeft.rePosition();
            sensorRight.rePosition();

            sensorUp.reSumOfPheromons(pickedUpFood >= maxFoodCapacity ? PheromonType.HOME_PATH : PheromonType.FOOD_PATH);
            sensorLeft.reSumOfPheromons(pickedUpFood >= maxFoodCapacity ? PheromonType.HOME_PATH : PheromonType.FOOD_PATH);
            sensorRight.reSumOfPheromons(pickedUpFood >= maxFoodCapacity ? PheromonType.HOME_PATH : PheromonType.FOOD_PATH);

            if (sensorLeft.getSum() > sensorUp.getSum() && sensorLeft.getSum() > sensorRight.getSum()) {
                goalDir += -Variables.Ant.PHEROMON_TURNING_SPEED * (-Math.pow(sensorUp.getSum() / sensorLeft.getSum(), 2) + 1);
            } else if (sensorRight.getSum() > sensorUp.getSum()) {
                goalDir += Variables.Ant.PHEROMON_TURNING_SPEED * (-Math.pow(sensorUp.getSum() / sensorRight.getSum(), 2) + 1);
            }
        }

        //[>-------------------
        // Movement
        //[>-------------------

        location.add((MathUtils.cosDeg(dir)) * speed, (MathUtils.sinDeg(dir) * speed));

        handleBorders();

        goalDir += (Math.random() > 0.5 ? 1 : -1) * Math.random() * Variables.Ant.WANDERING_STRENGTH;

        dir = (dir + 360) % 360;
        goalDir = (goalDir + 360) % 360;

        float dirDifference = (goalDir - dir + 360) % 360;

        float goalSpeed = (Variables.Ant.MAX_SPEED * (((-(dirDifference * dirDifference)) / 25_000) + 1));
        goalSpeed = Math.max(0.1f, goalSpeed);

        if (goalSpeed > speed) {
            speed += (goalSpeed - speed) * 0.2;
        } else {
            speed -= (speed - goalSpeed) * 0.08;
        }

        if (target != null) lookAt(target.getLocation());

        dir += ((dirDifference + 360 + 180) % 360 - 180) / (speed * 50);

        //[>-------------------
        // spawn Pheromon
        //[>-------------------

        lastPheromonSpawn++;

        if (lastPheromonSpawn >= 60 * Variables.Ant.PHEROMON_SPAWN_FRENZY) {
            lastPheromonSpawn -= 60 * Variables.Ant.PHEROMON_SPAWN_FRENZY;

            pheromonsToSpawnCount--;

            float strength = (float) pheromonsToSpawnCount / Variables.Ant.PHEROMON_MAX_TO_SPAWN;

            if (strength > 0.1)
                colony.addPheromon(new Pheromon(location.x, location.y, pickedUpFood >= maxFoodCapacity ? PheromonType.FOOD_PATH : PheromonType.HOME_PATH, strength));
        }
    }

    private void handleBorders() {
        for (int i = 0; i < Map.Structure.borders.length; i += 2) {

            Vector2 point = new Vector2();
            Intersector.nearestSegmentPoint(Map.Structure.borders[i], Map.Structure.borders[i + 1], location, point);

            float distance = point.dst(location);

            if (distance > 5f) continue;

            Vector2 normalBorderVector = new Vector2(Map.Structure.borders[i]).sub(Map.Structure.borders[i + 1]).nor();
            float borderAngle = normalBorderVector.angleDeg();
            float away = (180 + (borderAngle - dir + 180) + borderAngle) % 360;

            if (Maths.isPointInFOV(point, location, dir, Variables.Ant.VIEWiNG_ANGLE / 2f) && Math.abs(normalBorderVector.dot(MathUtils.cosDeg(away), MathUtils.sinDeg(away))) < Math.abs(normalBorderVector.dot(MathUtils.cosDeg(goalDir), MathUtils.sinDeg(goalDir)))) {
                goalDir = away;
            }

            if (distance < 1f) {
                goalDir = away;
                dir = away;
                location.add((MathUtils.cosDeg(dir)) * speed, (MathUtils.sinDeg(dir) * speed));
            }

            if (distance < 0.2) {
                //fixme ....
                colony.removeAnt(this);
                System.out.println("rip Ant");
            }
        }
    }

    private void handleHasFood() {
        float dstToNest = Vector2.dst(colony.nest.getLocation().x, colony.nest.getLocation().y, location.x, location.y);

        if (dstToNest < Variables.Ant.VIEWING_DISTANCE + Variables.Nest.SIZE) {
            target = colony.nest;

            if (dstToNest < Variables.Ant.PICK_UP_RADIUS * size + Variables.Nest.SIZE) {
                pheromonsToSpawnCount = Variables.Ant.PHEROMON_MAX_TO_SPAWN;

                colony.addFood(pickedUpFood);
                pickedUpFood = 0;

                target = null;
                goalDir += 180;
            }
        } else {
            target = null;
        }
    }

    private void handleHasNoFood() {

        //repower pheromons if near Nest
        float dstToNest = Vector2.dst(colony.nest.getLocation().x, colony.nest.getLocation().y, location.x, location.y);

        if (dstToNest < Variables.Ant.VIEWING_DISTANCE + Variables.Nest.SIZE)
            pheromonsToSpawnCount = Variables.Ant.PHEROMON_MAX_TO_SPAWN;

        Ant nearestAnt = Map.GameObjs.getNearestEnemyAntInFOV(this);
        target = nearestAnt;

        if (nearestAnt != null) {

            if (nearestAnt.getLocation().dst(location) < Variables.Ant.PICK_UP_RADIUS * size) {
                // TODO: 02.07.2022 fighting system
                nearestAnt.colony.removeAnt(nearestAnt);
            }

            return;
        }

        //look at nearest food
        Food nearestFood = Map.GameObjs.getNearestFoodInFOV(this);
        target = nearestFood;

        if (nearestFood != null && Vector2.dst(nearestFood.getLocation().x, nearestFood.getLocation().y, location.x, location.y) < Variables.Ant.PICK_UP_RADIUS * size) {

            pheromonsToSpawnCount = Variables.Ant.PHEROMON_MAX_TO_SPAWN;

            pickedUpFood += nearestFood.pickUp(maxFoodCapacity - pickedUpFood);

            if (pickedUpFood >= maxFoodCapacity) {
                goalDir += 180;
                target = null;
            }

        }

    }

    private void lookAt(Vector2 target) {
        goalDir = (float) Math.toDegrees(Math.atan2(target.y - location.y, target.x - location.x));
    }

    public void renderSprite(Sprite sprite) {

        sprite.setScale(Variables.Ant.SPRITE_SIZE * size);

        sprite.setCenterX(location.x);
        sprite.setCenterY(location.y);

        sprite.setRotation(dir - 180);
    }

    public void renderFood(ShapeRenderer shapeRenderer) {
        if (pickedUpFood >= maxFoodCapacity) {
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.circle(location.x + MathUtils.cosDeg(dir) * 2.35f * size, location.y + MathUtils.sinDeg(dir) * 2.35f * size, maxFoodCapacity, 8);
        }

    }

    public void renderFOV(ShapeRenderer shapeRenderer) {
        shapeRenderer.arc(location.x, location.y, Variables.Ant.VIEWING_DISTANCE, dir - Variables.Ant.VIEWiNG_ANGLE / 2, Variables.Ant.VIEWiNG_ANGLE, 16);
    }

    public float getDir() {
        return dir;
    }

    public void setColony(Colony colony) {
        if (this.colony == null) this.colony = colony;
    }

    public Colony getColony() {
        return colony;
    }
}
