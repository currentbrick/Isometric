package io.github.currentbrick.player;

public enum ItemType {

    WOOD("Wood"),

    STONE("Stone"),

    SAND("Sand"),

    GRASS("Grass");

    private final String displayName;

    ItemType(String displayName) {

        this.displayName = displayName;

    }

    public String getDisplayName() {

        return displayName;

    }

}
