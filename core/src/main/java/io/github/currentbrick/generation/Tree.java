package io.github.currentbrick.generation;

public class Tree {

    public int x;
    public int y;

    public float size;
    public int health;

    private float shakeTime = 0f;
    private float shakeDuration = 0.2f;
    private float shakeAmount = 4f;

    public Tree(int x, int y, float size) {
        this.x = x;
        this.y = y;
        this.size = size;

        this.health = 3;
    }

    public void update(float delta) {
        if (shakeTime > 0f) {
            shakeTime -= delta;

            if (shakeTime < 0f) {
                shakeTime = 0f;
            }
        }
    }

    public void shake() {
        shakeTime = shakeDuration;
    }

    public float getShakeOffset() {
        if (shakeTime <= 0f) {
            return 0f;
        }

        float progress =
            shakeTime / shakeDuration;

        return (float)Math.sin(progress * Math.PI * 6f)
            * shakeAmount
            * progress;
    }

    public void damage(int amount) {
        health -= amount;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }


}
