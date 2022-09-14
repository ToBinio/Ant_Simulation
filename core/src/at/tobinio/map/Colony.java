package at.tobinio.map;

import at.tobinio.NeighborFinder;
import at.tobinio.Variables;
import at.tobinio.gameObj.Nest;
import at.tobinio.gameObj.ant.Ant;
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

    public final PheromonMap pheromonMap;

    public final Nest nest;
    public final Color color;

    private float neededFood;
    private float currentFood;

    public Colony() {

        pheromonMap = new PheromonMap(Variables.Game.WIDTH / Variables.Map.PHEROMON_CELL_SIZE, Variables.Game.HEIGHT / Variables.Map.PHEROMON_CELL_SIZE);

        antsNeighborFinder = new SpacialHashmap<>(-Variables.Game.WIDTH / 2.0, Variables.Game.WIDTH / 2.0, -Variables.Game.HEIGHT / 2.0, Variables.Game.HEIGHT / 2.0, Variables.Game.HEIGHT / 4, Variables.Game.WIDTH / 4);

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

        //[>-------------------
        // Pheromons
        //[>-------------------

        pheromonMap.update();

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

    public Ant getNearestAntInFOV(Ant ant) {
        return Map.GameObjs.getNearestGameObjInFOV(ant.getLocation().x, ant.getLocation().y, Variables.Ant.VIEWING_DISTANCE, antsNeighborFinder, ant.getDir(), Variables.Ant.VIEWiNG_ANGLE / 2f);
    }

    public int getAntCount() {
        return ants.size();
    }
}
