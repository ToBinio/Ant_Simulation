package at.tobinio.map;

import at.tobinio.NeighborFinder;
import at.tobinio.Variables;
import at.tobinio.gameObj.Nest;
import at.tobinio.gameObj.ant.Ant;
import at.tobinio.gameObj.ant.pheromon.Pheromon;
import at.tobinio.gameObj.ant.pheromon.PheromonType;
import at.tobinio.spacialHashmap.SpacialHashmap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created: 28.06.2022
 *
 * @author Tobias Frischmann
 */

public class Colony {
    private final List<Ant> ants = new ArrayList<>();
    private final NeighborFinder<Ant> antsNeighborFinder;
    private final List<Ant> antsToAdd = new ArrayList<>();
    private final List<Ant> antsToRemove = new ArrayList<>();

    private final List<Pheromon> homePheromons = new ArrayList<>();
    private final NeighborFinder<Pheromon> homePheromonsNeighborFinder;
    private final List<Pheromon> foodPheromons = new ArrayList<>();
    private final NeighborFinder<Pheromon> foodPheromonsNeighborFinder;

    private final List<Pheromon> pheromonsToAdd = new ArrayList<>();
    private final List<Pheromon> pheromonsToRemove = new ArrayList<>();

    public final Nest nest;
    public final Color color;

    private float neededFood;
    private float currentFood;

    public Colony() {
        homePheromonsNeighborFinder = new SpacialHashmap<>(-Variables.Game.WIDTH / 2.0, Variables.Game.WIDTH / 2.0, -Variables.Game.HEIGHT / 2.0, Variables.Game.HEIGHT / 2.0, Variables.Game.HEIGHT / 4, Variables.Game.WIDTH / 4);
        foodPheromonsNeighborFinder = new SpacialHashmap<>(-Variables.Game.WIDTH / 2.0, Variables.Game.WIDTH / 2.0, -Variables.Game.HEIGHT / 2.0, Variables.Game.HEIGHT / 2.0, Variables.Game.HEIGHT / 4, Variables.Game.WIDTH / 4);

        antsNeighborFinder = new SpacialHashmap<>(-Variables.Game.WIDTH / 2.0, Variables.Game.WIDTH / 2.0, -Variables.Game.HEIGHT / 2.0, Variables.Game.HEIGHT / 2.0, Variables.Game.HEIGHT / 4, Variables.Game.WIDTH / 4);

//        homePheromonsNeighborFinder = new QuadTree<>(-Main.GAME_WIDTH / 2.0, Main.GAME_WIDTH / 2.0, -Main.GAME_HEIGHT / 2.0, Main.GAME_HEIGHT / 2.0, 10);
//        foodPheromonsNeighborFinder = new QuadTree<>(-Main.GAME_WIDTH / 2.0, Main.GAME_WIDTH / 2.0, -Main.GAME_HEIGHT / 2.0, Main.GAME_HEIGHT / 2.0, 10);

        Vector2 nestLocation = Map.Structure.getRandomValidLocation();
        nest = new Nest(nestLocation.x, nestLocation.y);

        color = new Color((float) Math.random() / 2, (float) Math.random() / 2, (float) Math.random() / 2, 1f);
    }

    public void addFood(float value) {
        currentFood += value;

        if (neededFood <= currentFood) {
            spawnAnt();
            currentFood -= neededFood;
        }
    }

    public void spawnAnt() {
        Ant ant = new Ant((float) nest.getX(), (float) nest.getY(), (float) (Math.random() * 360 - 180), 1 - (float) Math.random() * 0.6f);

        antsToAdd.add(ant);
        ant.setColony(this);
    }

    public void removeAnt(Ant ant) {
        antsToRemove.add(ant);
    }

    public void addPheromon(Pheromon pheromon) {
        pheromonsToAdd.add(pheromon);
        pheromon.setColony(this);
    }

    public void removePheromon(Pheromon pheromon) {
        pheromonsToRemove.add(pheromon);
    }

    public void update() {
        //[>-------------------
        // adding / removing to lists
        //[>-------------------

        for (Ant ant : antsToRemove) {
            ants.remove(ant);
        }
        antsToRemove.clear();

        ants.addAll(antsToAdd);
        antsToAdd.clear();


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
                Pheromon nearestPheromon = getNearestPheromonInDistance(pheromon.getLocation().x, pheromon.getLocation().y, 1f, PheromonType.FOOD_PATH);
                if (nearestPheromon != null) {
                    nearestPheromon.addPheromon(pheromon);
                } else {
                    foodPheromons.add(pheromon);
                }
            } else {
                Pheromon nearestPheromon = getNearestPheromonInDistance(pheromon.getLocation().x, pheromon.getLocation().y, 1f, PheromonType.HOME_PATH);
                if (nearestPheromon != null) {
                    nearestPheromon.addPheromon(pheromon);
                } else {
                    homePheromons.add(pheromon);
                }
            }
        }
        pheromonsToAdd.clear();

        //[>-------------------
        // Pheromons
        //[>-------------------

        homePheromonsNeighborFinder.clear();
        for (Pheromon homePheromon : homePheromons) {
            homePheromon.update();
            homePheromonsNeighborFinder.add(homePheromon);
        }

        foodPheromonsNeighborFinder.clear();
        for (Pheromon homePheromon : foodPheromons) {
            homePheromon.update();
            foodPheromonsNeighborFinder.add(homePheromon);
        }

        //[>-------------------
        // updating
        //[>-------------------

        antsNeighborFinder.clear();
        for (Ant ant : ants) {
            antsNeighborFinder.add(ant);
        }

        for (Ant ant : ants) {
            ant.update();
        }

        neededFood = ants.size() / 10f;
    }

    public void renderPheromons(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.BLUE);
        for (Pheromon homePheromon : homePheromons) {
            homePheromon.render(shapeRenderer);
        }

        shapeRenderer.setColor(Color.RED);
        for (Pheromon pheromon : foodPheromons) {
            pheromon.render(shapeRenderer);
        }
    }

    public void renderAntBody(SpriteBatch spriteBatch, Sprite antSprite) {

        antSprite.setColor(color);

        for (Ant ant : ants) {
            ant.renderSprite(antSprite);
            antSprite.draw(spriteBatch);
        }
    }

    public void renderAntFood(ShapeRenderer shapeRenderer) {
        for (Ant ant : ants) {
            ant.renderFood(shapeRenderer);
        }
    }

    public void renderAntFOV(ShapeRenderer shapeRenderer) {
        for (Ant ant : ants) {
            ant.renderFOV(shapeRenderer);
        }
    }

    public void renderNest(ShapeRenderer shapeRenderer) {
        nest.render(shapeRenderer, color, currentFood / neededFood);
    }

    public Pheromon getNearestPheromonInDistance(float x, float y, float range, PheromonType type) {
        return Map.GameObjs.getNearestGameObjInDistance(x, y, range, type == PheromonType.FOOD_PATH ? foodPheromonsNeighborFinder : homePheromonsNeighborFinder);
    }

    public Ant getNearestAntInFOV(Ant ant) {
        return Map.GameObjs.getNearestGameObjInFOV(ant.getLocation().x, ant.getLocation().y, Variables.Ant.VIEWING_DISTANCE, antsNeighborFinder, ant.getDir(), Variables.Ant.VIEWiNG_ANGLE / 2f);
    }

    public float sumOfPheromons(PheromonType pheromonType, float x, float y, float range) {
        float sum = 0;

        List<Pheromon> possiblePheromons;

        if (pheromonType == PheromonType.FOOD_PATH) {
            possiblePheromons = foodPheromonsNeighborFinder.getInCircle(x, y, range);
        } else {
            possiblePheromons = homePheromonsNeighborFinder.getInCircle(x, y, range);
        }

        // TODO: 28.06.2022 wtf i lose 14% cpu or so.... wtf :( 
        for (Pheromon pheromon : possiblePheromons) {
            sum += pheromon.getStrength();
        }

        return sum;
    }

    public int getAntCount() {
        return ants.size();
    }
}
