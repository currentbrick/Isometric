package io.github.currentbrick.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.currentbrick.Main;

import java.util.Random;

public class MenuScreen implements Screen {

    private int selectedButton = -1;
    private final float buttonWidth = 300;
    private final float buttonHeight = 60;

    private Main game;

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        font.getData().setScale(3f);
    }

    private boolean isMouseOver(
        float x,
        float y,
        float width,
        float height
    ) {

        float mouseX = Gdx.input.getX();

        // LibGDX mouse Y starts at the top,
        // whereas our UI coordinates start at the bottom.
        float mouseY =
            Gdx.graphics.getHeight() - Gdx.input.getY();

        return mouseX >= x &&
            mouseX <= x + width &&
            mouseY >= y &&
            mouseY <= y + height;
    }

    @Override
    public void render(float delta) {
        float centerX = Gdx.graphics.getWidth() / 2f;

        float buttonX = centerX - 150;

        float newWorldY = Gdx.graphics.getHeight() / 2f + 50;
        float loadWorldY = Gdx.graphics.getHeight() / 2f - 20;
        float quitY = Gdx.graphics.getHeight() / 2f - 90;
        Gdx.gl.glClearColor(
            0.1f,
            0.1f,
            0.15f,
            1
        );

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1);
        shapeRenderer.rect(buttonX, newWorldY-40, 300, 60);
        shapeRenderer.rect(buttonX, loadWorldY-40, 300, 60);
        shapeRenderer.rect(buttonX, quitY-40, 300, 60);

        shapeRenderer.end();
        batch.begin();
        font.draw(
            batch,
            "I S O M E T R I C",
            centerX - 165,
            Gdx.graphics.getHeight() / 2f + 150
        );

        font.draw(
            batch,
            "NEW WORLD",
            centerX - 140,
            newWorldY
        );

        font.draw(
            batch,
            "LOAD WORLD",
            centerX - 150,
            loadWorldY
        );

        font.draw(
            batch,
            "QUIT",
            centerX - 50,
            quitY
        );

        if (isMouseOver(buttonX, newWorldY-40, 300, 60)) {

            if (Gdx.input.justTouched()) {
                Random random = new Random();
                game.setScreen(new WorldScreen("WORLD"+random.nextInt(100)));
            }
        }

        if (isMouseOver(buttonX, loadWorldY-40, 300, 60)) {

            if (Gdx.input.justTouched()) {
                game.setScreen(new LoadScreen(game));
            }
        }

        if (isMouseOver(buttonX, quitY-40, 300, 60)) {

            if (Gdx.input.justTouched()) {
                Gdx.app.exit();
            }
        }

        batch.end();
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
        font.dispose();
        shapeRenderer.dispose();
    }
}
