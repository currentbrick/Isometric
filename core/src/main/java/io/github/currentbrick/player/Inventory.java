package io.github.currentbrick.player;

import io.github.currentbrick.player.ItemStack;
import io.github.currentbrick.player.ItemType;

public class Inventory {

    private static final int SLOT_COUNT = 20;
    private static final int MAX_STACK_SIZE = 64;

    private ItemStack[] slots;

    public Inventory() {
        slots = new ItemStack[SLOT_COUNT];
    }

    public boolean addItem(
        ItemType type,
        int amount
    ) {

        // First try to add to existing stacks
        for (int i = 0; i < slots.length; i++) {

            ItemStack stack = slots[i];

            if (
                stack != null
                    && stack.getType() == type
                    && stack.getAmount() < MAX_STACK_SIZE
            ) {

                int space =
                    MAX_STACK_SIZE
                        - stack.getAmount();

                int amountToAdd =
                    Math.min(space, amount);

                stack.add(amountToAdd);

                amount -= amountToAdd;

                if (amount <= 0) {
                    return true;
                }
            }
        }

        // Then find empty slots
        for (int i = 0; i < slots.length; i++) {

            if (slots[i] == null) {

                int amountToAdd =
                    Math.min(
                        MAX_STACK_SIZE,
                        amount
                    );

                slots[i] =
                    new ItemStack(
                        type,
                        amountToAdd
                    );

                amount -= amountToAdd;

                if (amount <= 0) {
                    return true;
                }
            }
        }

        // Inventory was full
        return false;
    }

    public ItemStack getSlot(int slot) {

        if (
            slot < 0
                || slot >= slots.length
        ) {
            return null;
        }

        return slots[slot];
    }

    public int getSlotCount() {
        return slots.length;
    }

    public boolean removeItem(
        ItemType type,
        int amount
    ) {

        for (int i = 0; i < slots.length; i++) {

            ItemStack stack = slots[i];

            if (
                stack != null
                    && stack.getType() == type
            ) {

                int amountToRemove =
                    Math.min(
                        amount,
                        stack.getAmount()
                    );

                stack.remove(
                    amountToRemove
                );

                amount -= amountToRemove;

                if (stack.isEmpty()) {
                    slots[i] = null;
                }

                if (amount <= 0) {
                    return true;
                }
            }
        }

        return false;
    }
}
