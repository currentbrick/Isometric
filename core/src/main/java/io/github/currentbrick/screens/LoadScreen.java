package io.github.currentbrick.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.currentbrick.Main;
import io.github.currentbrick.save.SaveData;
import io.github.currentbrick.save.Save;
import io.github.currentbrick.save.FileUtil;

import java.io.File;

public class LoadScreen implements Screen {

    private final Main game;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private File[] worlds;

    private final float buttonWidth = 400;
    private final float buttonHeight = 70;

    public LoadScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(2f);

        loadWorldList();
    }

    private void loadWorldList() {

        File saveDirectory = FileUtil.getSaveDirectory();

        worlds = saveDirectory.listFiles(File::isDirectory);

        if (worlds == null) {
            worlds = new File[0];
        }
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(
            0.08f,
            0.08f,
            0.12f,
            1
        );

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float centerX = Gdx.graphics.getWidth() / 2f;

        // -------------------------
        // BUTTONS
        // -------------------------

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        for (int i = 0; i < worlds.length; i++) {

            float x = centerX - buttonWidth / 2f;

            float y =
                Gdx.graphics.getHeight()
                    - 180
                    - i * 90;

            if (isMouseOver(
                x,
                y,
                buttonWidth,
                buttonHeight
            )) {

                shapeRenderer.setColor(
                    0.35f,
                    0.35f,
                    0.45f,
                    1
                );

            } else {

                shapeRenderer.setColor(
                    0.2f,
                    0.2f,
                    0.25f,
                    1
                );
            }

            shapeRenderer.rect(
                x,
                y,
                buttonWidth,
                buttonHeight
            );
        }

        // Back button

        float backY = 60;

        if (isMouseOver(
            centerX - buttonWidth / 2f,
            backY,
            buttonWidth,
            buttonHeight
        )) {

            shapeRenderer.setColor(
                0.35f,
                0.35f,
                0.45f,
                1
            );

        } else {

            shapeRenderer.setColor(
                0.2f,
                0.2f,
                0.25f,
                1
            );
        }

        shapeRenderer.rect(
            centerX - buttonWidth / 2f,
            backY,
            buttonWidth,
            buttonHeight
        );

        shapeRenderer.end();

        // -------------------------
        // TEXT
        // -------------------------

        batch.begin();

        font.getData().setScale(3f);

        font.draw(
            batch,
            "LOAD WORLD",
            centerX - 120,
            Gdx.graphics.getHeight() - 70
        );

        font.getData().setScale(2f);

        for (int i = 0; i < worlds.length; i++) {

            float x = centerX - buttonWidth / 2f;

            float y =
                Gdx.graphics.getHeight()
                    - 180
                    - i * 90;

            font.draw(
                batch,
                worlds[i].getName(),
                x + 20,
                y + 45
            );
        }

        font.draw(
            batch,
            "BACK",
            centerX - 45,
            backY + 45
        );

        batch.end();

        // -------------------------
        // INPUT
        // -------------------------

        if (Gdx.input.justTouched()) {

            for (int i = 0; i < worlds.length; i++) {

                float x = centerX - buttonWidth / 2f;

                float y =
                    Gdx.graphics.getHeight()
                        - 180
                        - i * 90;

                if (isMouseOver(
                    x,
                    y,
                    buttonWidth,
                    buttonHeight
                )) {

                    loadWorld(worlds[i]);
                    return;
                }
            }

            if (isMouseOver(
                centerX - buttonWidth / 2f,
                backY,
                buttonWidth,
                buttonHeight
            )) {

                game.setScreen(
                    new MenuScreen(game)
                );
            }
        }
    }

    private void loadWorld(File worldDirectory) {

        String worldName = worldDirectory.getName();

        SaveData data =
            Save.loadWorld(worldName);

        if (data == null) {
            System.out.println(
                "Failed to load world: " + worldName
            );
            return;
        }

        System.out.println(
            "Loading world: " + data.name
        );

        game.setScreen(
            new WorldScreen(
                data
            )
        );
    }

    private boolean isMouseOver(
        float x,
        float y,
        float width,
        float height
    ) {

        float mouseX = Gdx.input.getX();

        float mouseY =
            Gdx.graphics.getHeight()
                - Gdx.input.getY();

        return mouseX >= x
            && mouseX <= x + width
            && mouseY >= y
            && mouseY <= y + height;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {

        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
