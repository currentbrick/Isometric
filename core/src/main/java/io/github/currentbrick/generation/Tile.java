package io.github.currentbrick.generation;

public class Tile {

    public TileType type;

    public int height;
    public Biome biome;

    public float temperature;
    public float moisture;

    public Tile(
        TileType type,
        int height,
        Biome biome,
        float temperature,
        float moisture
    ) {
        this.type = type;
        this.height = height;
        this.biome = biome;
        this.temperature = temperature;
        this.moisture = moisture;
    }
}
