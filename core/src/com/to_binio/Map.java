package com.to_binio;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.to_binio.gameObj.Food;
import com.to_binio.gameObj.GameObj;
import com.to_binio.gameObj.Nest;
import com.to_binio.gameObj.ant.Ant;
import com.to_binio.gameObj.ant.pheromon.Pheromon;
import com.to_binio.gameObj.ant.pheromon.PheromonType;
import com.to_binio.utils.Maths;

import java.util.ArrayList;
import java.util.List;

/**
 * Created: 18.06.2022
 *
 * @author Tobias Frischmann
 */

public class Map {

    private final static List<Ant> ants = new ArrayList<>();
    private final static List<Ant> antsToAdd = new ArrayList<>();

    private final static List<Food> foods = new ArrayList<>();
    private final static List<Food> foodsToRemove = new ArrayList<>();

    private final static List<Pheromon> homePheromons = new ArrayList<>();
    private final static List<Pheromon> foodPheromons = new ArrayList<>();


    private final static List<Pheromon> pheromonsToAdd = new ArrayList<>();
    private final static List<Pheromon> pheromonsToRemove = new ArrayList<>();

    public final static Nest nest = new Nest(0, 0);

    public static void addFood(Food food) {
        foods.add(food);
    }

    public static void removeFood(Food food) {
        foodsToRemove.add(food);
    }

    public static void addAnt(Ant ant) {
        antsToAdd.add(ant);
    }

    public static void addPheromon(Pheromon pheromon) {
        pheromonsToAdd.add(pheromon);
    }

    public static void removePheromon(Pheromon pheromon) {
        pheromonsToRemove.add(pheromon);
    }

    public static void update() {
        //[>-------------------
        // adding / removing to lists
        //[>-------------------

        ants.addAll(antsToAdd);
        antsToAdd.clear();

        for (Food food : foodsToRemove) {
            foods.remove(food);
        }
        foodsToRemove.clear();

        for (Pheromon pheromon : pheromonsToRemove) {
            if (pheromon.type == PheromonType.FOOD_PATH) {
                foodPheromons.remove(pheromon);
            } else {
                homePheromons.remove(pheromon);
            }
        }
        pheromonsToRemove.clear();

        for (Pheromon pheromon : pheromonsToAdd) {
            if (pheromon.type == PheromonType.FOOD_PATH) {
                Pheromon nearestPheromon = getNearestPheromonInDistance(pheromon.getLocation().x, pheromon.getLocation().y, 0.2f, PheromonType.FOOD_PATH);
                if (nearestPheromon != null) {
                    nearestPheromon.addPheromon(pheromon);
                } else {
                    foodPheromons.add(pheromon);
                }
            } else {
                Pheromon nearestPheromon = getNearestPheromonInDistance(pheromon.getLocation().x, pheromon.getLocation().y, 0.7f, PheromonType.HOME_PATH);
                if (nearestPheromon != null) {
                    nearestPheromon.addPheromon(pheromon);
                } else {
                    homePheromons.add(pheromon);
                }
            }
        }
        pheromonsToAdd.clear();

        //[>-------------------
        // updating
        //[>-------------------

        homePheromons.forEach(Pheromon::update);
        foodPheromons.forEach(Pheromon::update);

        ants.forEach(Ant::update);
    }

    public static void render(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer, Sprite antSprite) {

        //[>-------------------
        // Food
        //[>-------------------
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(0, 0.7f, 0, 1));
        foods.forEach(food -> food.renderer(shapeRenderer));

        shapeRenderer.end();

        //[>-------------------
        // Pheromons
        //[>-------------------
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.BLUE);
        homePheromons.forEach(pheromon -> pheromon.render(shapeRenderer));

        shapeRenderer.setColor(Color.RED);
        foodPheromons.forEach(pheromon -> pheromon.render(shapeRenderer));

        shapeRenderer.end();

        //[>-------------------
        // Nest
        //[>-------------------
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(139 / 255f, 69 / 255f, 32 / 255f, 1));
        nest.render(shapeRenderer);

        shapeRenderer.end();

        //[>-------------------
        //  Ants
        //[>-------------------
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        spriteBatch.begin();

        ants.forEach(ant -> {
            ant.render(antSprite, shapeRenderer);
            antSprite.draw(spriteBatch);
        });

        spriteBatch.end();
        shapeRenderer.end();
    }

    public static <T extends GameObj> T getNearestGameObjInDistance(float x, float y, float range, List<T> gameObjs) {
        float nearestLength = -1;
        T nearestGameObj = null;

        for (T gameObj : gameObjs) {
            float distanceToFood = Vector2.dst(gameObj.getLocation().x, gameObj.getLocation().y, x, y);

            if (distanceToFood < range && (nearestLength == -1 || distanceToFood < nearestLength)) {
                nearestLength = distanceToFood;
                nearestGameObj = gameObj;
            }
        }

        return nearestGameObj;
    }

    public static Food getNearestFoodInDistance(float x, float y, float range) {
        return getNearestGameObjInDistance(x, y, range, foods);
    }

    public static Food getNearestFoodInFOV(Ant ant) {

        Food nearestFood = getNearestFoodInDistance(ant.getLocation().x, ant.getLocation().y, Ant.VIEWING_DISTANCE);

        if (nearestFood != null && Maths.isPointInFOV(ant.getLocation(), nearestFood.getLocation(), ant.getDir(), Ant.VIEWiNG_ANGLE / 2f)) {
            return nearestFood;
        } else {
            return null;
        }
    }

    public static Pheromon getNearestPheromonInDistance(float x, float y, float range, PheromonType type) {
        return getNearestGameObjInDistance(x, y, range, type == PheromonType.FOOD_PATH ? foodPheromons : homePheromons);
    }

    public static float sumOfPheromons(PheromonType pheromonType, float x, float y, float range) {
        float sum = 0;

        List<Pheromon> activeList = (pheromonType == PheromonType.FOOD_PATH ? foodPheromons : homePheromons);

        for (Pheromon pheromon : activeList) {
            if (Vector2.dst(x, y, pheromon.getLocation().x, pheromon.getLocation().y) < range)
                sum += pheromon.getStrength();
        }

        return sum;
    }

    public static void spawnRandomFoodCluster() {
        float x = (float) (Math.random() * Main.GAME_WIDTH - Main.GAME_WIDTH / 2f);
        float y = (float) (Math.random() * Main.GAME_HEIGHT - Main.GAME_HEIGHT / 2f);

        for (int i = 0; i < 500; i++) {
            Map.addFood(new Food((float) (Math.random() * 20) + x - 10, (float) (Math.random() * 20) + y - 10));
        }
    }
}
