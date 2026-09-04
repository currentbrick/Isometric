package io.github.currentbrick.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.currentbrick.generation.World;

public class Player {

    private float x;
    private float y;

    private Inventory inventory;

    private float speed = 5f;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
        inventory = new Inventory();
        inventory.addItem(ItemType.TORCH, 64);
    }

    public void update(float delta, World world) {

        float moveX = 0;
        float moveY = 0;

        // SCREEN UP
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveX += 1;
            moveY += 1;
        }

        // SCREEN DOWN
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveX -= 1;
            moveY -= 1;
        }

        // SCREEN LEFT
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX -= 1;
            moveY += 1;
        }

        // SCREEN RIGHT
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX += 1;
            moveY -= 1;
        }

        // Normalize
        float length =
            (float)Math.sqrt(
                moveX * moveX +
                    moveY * moveY
            );

        if (length > 0) {

            moveX /= length;
            moveY /= length;

            float movementX =
                moveX * speed * delta;

            float movementY =
                moveY * speed * delta;

            // X collision
            float newX =
                x + movementX;

            if (!world.isTreeColliding(newX, y)) {
                x = newX;
            }

            // Y collision
            float newY =
                y + movementY;

            if (!world.isTreeColliding(x, newY)) {
                y = newY;
            }
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
