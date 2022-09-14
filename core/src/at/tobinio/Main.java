package at.tobinio;

import at.tobinio.map.Colony;
import at.tobinio.map.Map;
import at.tobinio.utils.MyInput;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private PolygonSpriteBatch polygonSpriteBatch;

    private Texture img;
    private Sprite sprite;

    private BitmapFont font;

    private Viewport viewport;
    public OrthographicCamera gameCamera;
    public OrthographicCamera uiCamera;

    @Override
    public void create() {

        Gdx.input.setInputProcessor(new MyInput(this));

        gameCamera = new OrthographicCamera(160, 90);
        uiCamera = new OrthographicCamera(1600, 900);
        viewport = new ExtendViewport(340, 180, gameCamera);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        polygonSpriteBatch = new PolygonSpriteBatch();

        font = new BitmapFont();
        font.setColor(Color.BLACK);
        font.getData().setScale(1.5f);

        img = new Texture("ant.png");

        sprite = new Sprite(img);

        Map.Structure.compute();
        Map.Structure.bakeMarchingCubeMap();
        Map.Structure.optimizeBorder();
        Map.Structure.bakeBorderRayCaster();

        for (int i = 0; i < 3; i++) {
            Colony colony = new Colony();
            Map.GameObjs.addColony(colony);

            for (int j = 0; j < 100; j++) {
                colony.spawnAnt();
            }
        }

        for (int i = 0; i < 5; i++) {
            Map.GameObjs.spawnRandomFoodCluster();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(92 / 255f, 64 / 255f, 51 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!Variables.Game.TOGGLE_PAUSE_SIMULATION)
            Map.update();

        spriteBatch.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        polygonSpriteBatch.setProjectionMatrix(gameCamera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(222 / 255f, 184 / 255f, 135 / 255f, 1);
        shapeRenderer.rect(-Variables.Game.WIDTH / 2f, -Variables.Game.HEIGHT / 2f, Variables.Game.WIDTH, Variables.Game.HEIGHT);

        shapeRenderer.end();

        Map.render(spriteBatch, shapeRenderer, sprite, polygonSpriteBatch);

        spriteBatch.setProjectionMatrix(uiCamera.combined);

        spriteBatch.begin();
        font.draw(spriteBatch, "Fps: " + Gdx.graphics.getFramesPerSecond(), -800, 450);
        font.draw(spriteBatch, "Ants: " + Map.GameObjs.getAntCount(), -800, 430);
        font.draw(spriteBatch, "Food Pheromons: " + Map.GameObjs.getPheromonCount(), -800, 410);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
        polygonSpriteBatch.dispose();

        img.dispose();
        font.dispose();
    }
}
