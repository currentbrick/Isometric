package io.github.currentbrick.rendering;

import com.badlogic.gdx.math.Vector2;

public class IsoRenderer {

    private static final float TILE_WIDTH = 64;
    private static final float TILE_HEIGHT = 32;

    public Vector2 worldToScreen(float x, float y) {
        float screenX = (x - y) * TILE_WIDTH / 2;
        float screenY = (x + y) * TILE_HEIGHT / 2;

        return new Vector2(screenX, screenY);
    }
}
