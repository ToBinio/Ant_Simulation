package at.tobinio;

/**
 * Created: 29.06.2022
 *
 * @author Tobias Frischmann
 */

public class Variables {
    public static class Game {

        public static final int WIDTH = 1200;
        public static final int HEIGHT = 1200;

        public static boolean TOGGLE_RENDER_PHEROMONS = false;
        public static boolean TOGGLE_RENDER_ANT_FOV = false;
        public static boolean TOGGLE_PAUSE_SIMULATION = false;
    }

    public static class Map {

        public static final int CELL_SIZE = 12;
    }

    public static class Nest {

        public static final float SIZE = 10;
    }

    public static class Ant {

        public static final float MAX_SPEED = 0.3f;
        public static final float WANDERING_STRENGTH = 5;

        public static final float VIEWING_DISTANCE = 20;
        public static final float VIEWiNG_ANGLE = 120;

        public static final float PICK_UP_RADIUS = 2;

        public static final float MAX_FOOD_CAPACITY = 0.4f;

        public static final float PHEROMON_TURNING_SPEED = 10;
        public static final float PHEROMON_SPAWN_FRENZY = 0.5f;
        public static final float PHEROMON_TIME_TO_SPAWN = 40;
        public static final int PHEROMON_MAX_TO_SPAWN = (int) (PHEROMON_TIME_TO_SPAWN * (1 / PHEROMON_SPAWN_FRENZY));

        public static final float SPRITE_SIZE = 0.01f;

        public static class Sensor {

            public static final float DISTANCE_TO_ANT = VIEWING_DISTANCE / 2f;
            public static final float RANGE = VIEWING_DISTANCE / 2f;
        }
    }
}
