package io.github.currentbrick.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.currentbrick.player.Hotbar;
import io.github.currentbrick.player.Inventory;

public class HUD {

    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private Hotbar hotbar;

    public HUD(Inventory inventory) {
        spriteBatch = new SpriteBatch();
        hotbar = new Hotbar(inventory);
        font = new BitmapFont();
        font.getData().setScale(2f);
    }

    public void render(float worldTime) {

        int hour = (int) worldTime;
        int minute = (int) ((worldTime - hour) * 60f);

        String time = String.format("%02d:%02d", hour, minute);

        // Use screen coordinates rather than the world camera
        spriteBatch.getProjectionMatrix().setToOrtho2D(
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        spriteBatch.begin();

        font.draw(
            spriteBatch,
            time,
            20,
            Gdx.graphics.getHeight() - 20
        );

        spriteBatch.end();
        hotbar.render();
    }

    public void dispose() {
        spriteBatch.dispose();
        font.dispose();
        hotbar.dispose();
    }
}
