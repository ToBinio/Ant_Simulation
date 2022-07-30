package at.tobinio.utils;

import at.tobinio.Main;
import at.tobinio.Variables;
import at.tobinio.map.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created: 26.06.2022
 *
 * @author Tobias Frischmann
 */

public class MyInput implements InputProcessor {

    private final Main main;
    private final Vector2 mousePos = new Vector2();

    public MyInput(Main main) {
        this.main = main;
    }

    @Override
    public boolean keyDown(int keycode) {

        switch (keycode) {
            case Input.Keys.NUM_1 -> Variables.Game.TOGGLE_RENDER_PHEROMONS = !Variables.Game.TOGGLE_RENDER_PHEROMONS;
            case Input.Keys.NUM_2 -> Variables.Game.TOGGLE_RENDER_ANT_FOV = !Variables.Game.TOGGLE_RENDER_ANT_FOV;
            case Input.Keys.SPACE -> Variables.Game.TOGGLE_PAUSE_SIMULATION = !Variables.Game.TOGGLE_PAUSE_SIMULATION;
        }

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE) {
            mousePos.x = screenX;
            mousePos.y = screenY;
        } else if (button == Input.Buttons.LEFT) {

            Vector3 pos = main.gameCamera.unproject(new Vector3(screenX, screenY, 0));

            if (Map.Structure.isValidSpawnLocation(pos.x, pos.y))
                Map.GameObjs.spawnRandomFoodCluster(pos.x, pos.y);
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            main.gameCamera.translate(main.gameCamera.unproject(new Vector3(mousePos.x, mousePos.y, 0)).sub(main.gameCamera.unproject(new Vector3(screenX, screenY, 0))));
            main.gameCamera.update();

            mousePos.x = screenX;
            mousePos.y = screenY;
        }

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Vector3 before = main.gameCamera.unproject(new Vector3(mouse.x, mouse.y, mouse.z));

        main.gameCamera.zoom *= 1 + (amountY / 20);
        main.gameCamera.update();

        main.gameCamera.translate(before.sub(main.gameCamera.unproject(mouse)));
        main.gameCamera.update();

        return true;
    }
}
