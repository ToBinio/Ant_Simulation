package com.to_binio;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private Texture img;
    private Sprite sprite;

    private Viewport viewport;
    private OrthographicCamera camera;

    public static final int GAME_WIDTH = 160 * 2;
    public static final int GAME_HEIGHT = 90 * 2;

    @Override
    public void create() {

        camera = new OrthographicCamera(GAME_WIDTH, GAME_HEIGHT);
        viewport = new FitViewport(GAME_WIDTH, GAME_HEIGHT, camera);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        img = new Texture("ant.png");

        sprite = new Sprite(img);

        for (int i = 0; i < 50; i++) {
            Map.nest.spawnAnt();
        }

        for (int i = 0; i < 5; i++) {
            Map.spawnRandomFoodCluster();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.15f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Map.update();

        spriteBatch.setProjectionMatrix(camera.projection);
        shapeRenderer.setProjectionMatrix(camera.projection);

        sprite.setScale(0.01f);

        Map.render(spriteBatch, shapeRenderer, sprite);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        img.dispose();
    }
}
