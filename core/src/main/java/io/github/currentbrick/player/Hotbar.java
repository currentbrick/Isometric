package io.github.currentbrick.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Hotbar {

    private static final int SLOT_COUNT = 8;

    private static final float SLOT_SIZE = 50f;
    private static final float SLOT_SPACING = 4f;

    private Inventory inventory;

    private int selectedSlot = 0;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;

    public Hotbar(Inventory inventory) {

        this.inventory = inventory;

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
    }

    public void update() {

        // Number keys 1-8
        for (int i = 0; i < SLOT_COUNT; i++) {

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1 + i
            )) {

                selectedSlot = i;
            }
        }

        // Mouse wheel
        /*float scroll =
            Gdx.input.getDeltaY();

        if (scroll > 0) {
            selectedSlot--;

            if (selectedSlot < 0) {
                selectedSlot = SLOT_COUNT - 1;
            }
        }

        if (scroll < 0) {
            selectedSlot++;

            if (selectedSlot >= SLOT_COUNT) {
                selectedSlot = 0;
            }
        }*/
    }

    public void render() {

        update();

        float totalWidth =
            SLOT_COUNT * SLOT_SIZE
                + (SLOT_COUNT - 1) * SLOT_SPACING;

        float startX =
            (Gdx.graphics.getWidth() - totalWidth) / 2f;

        float y = 25f;

        // Draw slot backgrounds
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        for (int i = 0; i < SLOT_COUNT; i++) {

            float x =
                startX
                    + i * (SLOT_SIZE + SLOT_SPACING);

            if (i == selectedSlot) {

                shapeRenderer.setColor(
                    Color.WHITE
                );

            } else {

                shapeRenderer.setColor(
                    new Color(
                        0.15f,
                        0.15f,
                        0.15f,
                        0.9f
                    )
                );
            }

            shapeRenderer.rect(
                x,
                y,
                SLOT_SIZE,
                SLOT_SIZE
            );
        }

        shapeRenderer.end();


        // Draw item names / amounts
        spriteBatch.begin();

        for (int i = 0; i < SLOT_COUNT; i++) {

            ItemStack stack =
                inventory.getSlot(i);

            if (stack == null) {
                continue;
            }

            float x =
                startX
                    + i * (SLOT_SIZE + SLOT_SPACING);

            String name =
                stack.getType()
                    .getDisplayName();

            String amount =
                String.valueOf(
                    stack.getAmount()
                );

            font.draw(
                spriteBatch,
                name.substring(0, 1),
                x + 19,
                y + 31
            );

            font.draw(
                spriteBatch,
                amount,
                x + 35,
                y + 14
            );
        }

        spriteBatch.end();
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public ItemStack getSelectedItem() {

        return inventory.getSlot(
            selectedSlot
        );
    }

    public void dispose() {

        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
