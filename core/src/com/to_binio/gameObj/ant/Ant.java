package com.to_binio.gameObj.ant;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.to_binio.Main;
import com.to_binio.Map;
import com.to_binio.gameObj.Food;
import com.to_binio.gameObj.GameObj;
import com.to_binio.gameObj.Nest;
import com.to_binio.gameObj.ant.pheromon.Pheromon;
import com.to_binio.gameObj.ant.pheromon.PheromonType;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public class Ant extends GameObj {

    public static final float MAX_SPEED = 0.3f;
    public static final float WANDERING_STRENGTH = 5;
    public static final float VIEWING_DISTANCE = 15;
    public static final float VIEWiNG_ANGLE = 120;
    public static final float PICK_UP_RADIUS = 2;
    public static final float PHEROMON_SPAWN_FRENZY = 0.2f;

    private float dir;
    private float goalDir;

    private boolean hasFood;

    private float speed;
    private float goalSpeed;

    private float lastPheromonSpawn = 0;

    private float pheromonSwitchTime = -1;

    private final Sensor sensorUp;
    private final Sensor sensorLeft;
    private final Sensor sensorRight;

    public Ant(float x, float y, float dir) {
        super(x, y);

        this.dir = dir;
        goalDir = dir;

        speed = MAX_SPEED;

        sensorUp = new Sensor(this, 0);
        sensorLeft = new Sensor(this, -45);
        sensorRight = new Sensor(this, 45);
    }

    public void update() {

        //[>-------------------
        // spawn Pheromon
        //[>-------------------

        if (pheromonSwitchTime == -1) pheromonSwitchTime = System.nanoTime();

        lastPheromonSpawn += Gdx.graphics.getDeltaTime();

        if (lastPheromonSpawn >= PHEROMON_SPAWN_FRENZY) {
            lastPheromonSpawn -= PHEROMON_SPAWN_FRENZY;

            float timeSinceLastPheromonSwitch = (System.nanoTime() - pheromonSwitchTime) / 1_000_000_000f;

            float strength = 1 / ((timeSinceLastPheromonSwitch / 5) + 1);

            if (strength > 0.4)
                Map.addPheromon(new Pheromon(location.x, location.y, hasFood ? PheromonType.FOOD_PATH : PheromonType.HOME_PATH, strength));
        }

        //[>-------------------
        // Compute Pheromons
        //[>-------------------

        sensorUp.rePosition();
        sensorLeft.rePosition();
        sensorRight.rePosition();

        sensorUp.reSumOfPheromons(hasFood ? PheromonType.HOME_PATH : PheromonType.FOOD_PATH);
        sensorLeft.reSumOfPheromons(hasFood ? PheromonType.HOME_PATH : PheromonType.FOOD_PATH);
        sensorRight.reSumOfPheromons(hasFood ? PheromonType.HOME_PATH : PheromonType.FOOD_PATH);

        if (sensorLeft.getSum() > sensorUp.getSum() && sensorLeft.getSum() > sensorRight.getSum()) {
            goalDir += -10;
        } else if (sensorRight.getSum() > sensorUp.getSum()) {
            goalDir += 10;
        }

        //[>-------------------
        // Handle Food
        //[>-------------------

        if (hasFood) {
            handleHasFood();
        } else {
            handleHasNoFood();
        }

        //[>-------------------
        // Movement
        //[>-------------------

        location.add((MathUtils.cosDeg(dir)) * speed, (MathUtils.sinDeg(dir) * speed));


        if (location.x > Main.GAME_WIDTH / 2f || location.x < -Main.GAME_WIDTH / 2f) {
            Vector2 vector2 = new Vector2(-MathUtils.cosDeg(dir), MathUtils.sinDeg(dir));
            goalDir = vector2.angleDeg();
            dir = goalDir;
        }

        if (location.y > Main.GAME_HEIGHT / 2f || location.y < -Main.GAME_HEIGHT / 2f) {
            Vector2 vector2 = new Vector2(MathUtils.cosDeg(dir), -MathUtils.sinDeg(dir));
            goalDir = vector2.angleDeg();
            dir = goalDir;

        }


        goalDir += (Math.random() > 0.5 ? 1 : -1) * Math.random() * WANDERING_STRENGTH;

        dir = (dir + 360) % 360;
        goalDir = (goalDir + 360) % 360;

        float dirDifference = goalDir - dir;

        goalSpeed = (MAX_SPEED * (((-(dirDifference * dirDifference)) / 20_000) + 1));
        goalSpeed = Math.max(0, goalSpeed);

        if (goalSpeed > speed) {
            speed += (goalSpeed - speed) * 0.1;
        } else {
            speed -= (speed - goalSpeed) * 0.2;
        }

        dir += ((dirDifference + 360 + 180) % 360 - 180) / 20;
    }

    private void handleHasFood() {
        float dst = Vector2.dst(Map.nest.getLocation().x, Map.nest.getLocation().y, location.x, location.y);

        if (dst < VIEWING_DISTANCE + Nest.SIZE) {
            lookAt(Map.nest.getLocation());

            if (dst < PICK_UP_RADIUS + Nest.SIZE) {
                pheromonSwitchTime = System.nanoTime();

                hasFood = false;

                goalDir += 180;
            }
        }
    }

    private void handleHasNoFood() {
        Food nearestFood = null;

        nearestFood = Map.getNearestFoodInFOV(this);
        float dstToNest = Vector2.dst(Map.nest.getLocation().x, Map.nest.getLocation().y, location.x, location.y);

        if (dstToNest < VIEWING_DISTANCE + Nest.SIZE) pheromonSwitchTime = System.nanoTime();

        if (nearestFood != null) {

            lookAt(nearestFood.getLocation());

            if (Vector2.dst(nearestFood.getLocation().x, nearestFood.getLocation().y, location.x, location.y) < PICK_UP_RADIUS) {

                pheromonSwitchTime = System.nanoTime();

                hasFood = true;
                Map.removeFood(nearestFood);

                goalDir += 180;
            }
        }
    }

    private void lookAt(Vector2 target) {
        goalDir = (float) Math.toDegrees(Math.atan2(target.y - location.y, target.x - location.x));
    }

    public void render(Sprite sprite, ShapeRenderer shapeRenderer) {
        sprite.setCenterX(location.x);
        sprite.setCenterY(location.y);

        sprite.setRotation(dir - 180);

        if (hasFood) {
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.circle(location.x, location.y, 0.5f, 8);
        }

//        shapeRenderer.arc(location.x, location.y, VIEWING_DISTANCE, dir - VIEWiNG_ANGLE / 2, VIEWiNG_ANGLE, 16);
    }

    public float getDir() {
        return dir;
    }
}
