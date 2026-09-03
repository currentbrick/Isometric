package io.github.currentbrick.player;

public class ItemStack {

    private ItemType type;
    private int amount;

    public ItemStack(ItemType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public ItemType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public void add(int amount) {
        this.amount += amount;
    }

    public void remove(int amount) {
        this.amount -= amount;

        if (this.amount < 0) {
            this.amount = 0;
        }
    }

    public boolean isEmpty() {
        return amount <= 0;
    }
}
