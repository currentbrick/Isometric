package io.github.currentbrick.generation;

import java.util.ArrayList;

public class Chunk {

    public static final int SIZE = 16;

    private Tile[][] tiles;

    private int chunkX, chunkY;
    private int seed;

    private ArrayList<Tree> trees;

    public Chunk(int chunkX, int chunkY, int seed) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.seed = seed;
        tiles = new Tile[SIZE][SIZE];
        trees = new ArrayList<>();
        generateTerrain();
        generateTrees();
    }

    private void generateTerrain() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                // Convert local chunk array coordinates to global world grid coordinates
                int globalX = chunkX * SIZE + x;
                int globalY = chunkY * SIZE + y;

                float noiseValue = DummyNoise.generate(globalX * 0.05f, globalY * 0.05f, seed);

                int height = (int) ((noiseValue + 1f) * 2.5f);
                height = Math.max(0, height);

                float temperatureNoise = DummyNoise.generate(
                    globalX * 0.02f,
                    globalY * 0.02f,
                    seed + 10000
                );

                float moistureNoise = DummyNoise.generate(
                    globalX * 0.025f,
                    globalY * 0.025f,
                    seed + 20000
                );

                float temperature =
                    (temperatureNoise + 1f) / 2f;

                float moisture =
                    (moistureNoise + 1f) / 2f;

                Biome biome = determineBiome(
                    height,
                    temperature,
                    moisture
                );

                // Determine Tile Type based on height
                TileType type;

                type = switch (biome) {
                    case OCEAN, BEACH, DESERT -> TileType.SAND;
                    case MOUNTAIN, SNOWY_MOUNTAIN -> TileType.MOUNTAIN;
                    default -> TileType.GRASS;
                };

                tiles[x][y] = new Tile(
                    type,
                    height,
                    biome,
                    temperature,
                    moisture
                );
            }
        }
    }

    private void generateTrees() {

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {

                Tile tile = tiles[x][y];

                // Trees only grow on grass
                if (tile.type != TileType.GRASS) {
                    continue;
                }

                // Trees prefer warm, wet areas
                float treeChance =
                    tile.moisture * 0.7f +
                        tile.temperature * 0.3f;

                // Forests get lots of trees
                if (tile.biome == Biome.FOREST) {
                    treeChance += 0.35f;
                }

                // Random value based on world position
                float random = DummyNoise.generate(
                    (chunkX * SIZE + x) * 0.8f,
                    (chunkY * SIZE + y) * 0.8f,
                    seed + 50000
                );

                random = (random + 1f) / 2f;

                if (random < treeChance * 0.35f) {

                    float size = 0.8f + random * 0.5f;

                    trees.add(new Tree(
                        chunkX * SIZE + x,
                        chunkY * SIZE + y,
                        size
                    ));
                }
            }
        }
    }

    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkY() {
        return chunkY;
    }

    private Biome determineBiome(
        int height,
        float temperature,
        float moisture
    ) {

        // Water overrides everything
        if (height <= 0) {
            return Biome.OCEAN;
        }

        // Low coastal areas
        if (height <= 2) {
            return Biome.BEACH;
        }

        // High elevations
        if (height >= 10) {

            if (temperature < 0.35f) {
                return Biome.SNOWY_MOUNTAIN;
            }

            return Biome.MOUNTAIN;
        }

        // Cold regions
        if (temperature < 0.25f) {
            return Biome.TUNDRA;
        }

        // Hot + dry
        if (temperature > 0.65f && moisture < 0.35f) {
            return Biome.DESERT;
        }

        // Wet
        if (moisture > 0.6f) {
            return Biome.FOREST;
        }

        // Default
        return Biome.PLAINS;
    }

    public ArrayList<Tree> getTrees() {
        return trees;
    }

    public void removeTree(Tree tree) {
        trees.remove(tree);
    }
}
