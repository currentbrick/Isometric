package io.github.currentbrick.generation;

import io.github.currentbrick.blocks.Torch;

import java.util.*;

public class World {

    private final Map<String, Chunk> chunks = new HashMap<>();

    private static final int MAX_CHUNK_KEEP_RADIUS = 5;

    private int seed = 0;

    public World() {
        seed = generateSeed();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                loadChunk(x, y);
            }
        }
    }

    public World(int seed) {
        this.seed = seed;
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                loadChunk(x, y);
            }
        }
    }

    public void loadChunk(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        if (!chunks.containsKey(key)) {
            // Updated to pass coordinates down into Chunk for proper noise mapping
            chunks.put(key, new Chunk(chunkX, chunkY, seed, this));

        }
    }

    public Chunk getChunk(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        return chunks.get(key);
    }

    public Tile getTileAtWorldCoords(int globalX, int globalY) {
        int chunkX = Math.floorDiv(globalX, Chunk.SIZE);
        int chunkY = Math.floorDiv(globalY, Chunk.SIZE);

        // DYNAMIC TRIGGER: If a chunk isn't loaded yet, create it on-the-fly!
        loadChunk(chunkX, chunkY);

        Chunk chunk = getChunk(chunkX, chunkY);
        int localX = Math.floorMod(globalX, Chunk.SIZE);
        int localY = Math.floorMod(globalY, Chunk.SIZE);

        return chunk.getTile(localX, localY);
    }

    public void unloadDistantChunks(int camGlobalX, int camGlobalY) {
        // 1. Calculate which chunk coordinates the camera is currently inside
        int camChunkX = Math.floorDiv(camGlobalX, Chunk.SIZE);
        int camChunkY = Math.floorDiv(camGlobalY, Chunk.SIZE);

        // 2. Safely iterate through the map to avoid ConcurrentModificationExceptions
        Iterator<Map.Entry<String, Chunk>> iterator = chunks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Chunk> entry = iterator.next();
            Chunk chunk = entry.getValue();

            // 3. Compute distance in chunks (using Manhattan distance for efficiency)
            int distanceX = Math.abs(chunk.getChunkX() - camChunkX);
            int distanceY = Math.abs(chunk.getChunkY() - camChunkY);

            // 4. Remove if it exceeds our threshold
            if (distanceX > MAX_CHUNK_KEEP_RADIUS || distanceY > MAX_CHUNK_KEEP_RADIUS) {
                iterator.remove(); // Cleanly unloads from memory
            }
        }
    }

    /**
     * Helper to see how many chunks are live in memory.
     */
    public int getLoadedChunkCount() {
        return chunks.size();
    }

    public int generateSeed() {
        Random random = new Random();
        return random.nextInt(1000000);
    }

    public ArrayList<Chunk> getLoadedChunks() {
        ArrayList<Chunk> loadedChunks = new ArrayList<>();
        Iterator<Map.Entry<String, Chunk>> iterator = chunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Chunk> entry = iterator.next();
            Chunk chunk = entry.getValue();
            loadedChunks.add(chunk);
        }
        return loadedChunks;
    }

    public Tree getTreeAtWorldCoords(int globalX, int globalY) {
        int chunkX = Math.floorDiv(globalX, Chunk.SIZE);
        int chunkY = Math.floorDiv(globalY, Chunk.SIZE);

        int localX = Math.floorMod(globalX, Chunk.SIZE);
        int localY = Math.floorMod(globalY, Chunk.SIZE);

        Chunk chunk = getChunk(chunkX, chunkY);
        if (chunk == null) {
            return null;
        }
        for (Tree tree : chunk.getTrees()) {
            if (tree.x == globalX && tree.y == globalY) {
                return tree;
            }
        }
        return null;
    }

    public int getSeed() {
        return seed;
    }

    public void removeTreeAtWorldCoords(
        int globalX,
        int globalY
    ) {

        int chunkX =
            Math.floorDiv(
                globalX,
                Chunk.SIZE
            );

        int chunkY =
            Math.floorDiv(
                globalY,
                Chunk.SIZE
            );

        Chunk chunk =
            getChunk(
                chunkX,
                chunkY
            );

        if (chunk == null) {
            return;
        }

        Tree tree =
            getTreeAtWorldCoords(
                globalX,
                globalY
            );

        if (tree != null) {
            chunk.removeTree(tree);
        }
    }
    public boolean isTreeColliding(float x, float y) {

        int centerX = Math.round(x);
        int centerY = Math.round(y);

        for (int tileX = centerX - 1; tileX <= centerX + 1; tileX++) {
            for (int tileY = centerY - 1; tileY <= centerY + 1; tileY++) {

                Tree tree =
                    getTreeAtWorldCoords(tileX, tileY);

                if (tree == null) {
                    continue;
                }

                float dx = x - tree.x;
                float dy = y - tree.y;

                float distance =
                    (float)Math.sqrt(
                        dx * dx + dy * dy
                    );

                float collisionRadius =
                    0.35f * tree.size;

                if (distance < collisionRadius) {
                    return true;
                }
            }
        }

        return false;
    }

    public Torch getTorchAtWorldCoords(int globalX, int globalY) {
        int chunkX = Math.floorDiv(globalX, Chunk.SIZE);
        int chunkY = Math.floorDiv(globalY, Chunk.SIZE);

        int localX = Math.floorMod(globalX, Chunk.SIZE);
        int localY = Math.floorMod(globalY, Chunk.SIZE);

        Chunk chunk = getChunk(chunkX, chunkY);
        if (chunk == null) {
            return null;
        }
        for (Torch torch : chunk.getTorches()) {
            if (torch.x == globalX && torch.y == globalY) {
                return torch;
            }
        }
        return null;
    }

    public void removeTorchAtWorldCoords(int globalX, int globalY) {
        int chunkX = Math.floorDiv(globalX, Chunk.SIZE);
        int chunkY = Math.floorDiv(globalY, Chunk.SIZE);
        Chunk chunk = getChunk(chunkX, chunkY);

        if (chunk == null) {
            return;
        }

        Torch torch = getTorchAtWorldCoords(globalX, globalY);

        if (torch != null) {
            chunk.removeTorch(torch);
            bakeTorchLighting();
        }
    }

    public void bakeTorchLighting() {

        // Clear all existing baked light
        for (Chunk chunk : chunks.values()) {

            for (Tile[] row : chunk.getTiles()) {

                for (Tile tile : row) {
                    tile.lightLevel = 0f;
                }
            }
        }

        // Bake every torch
        for (Chunk chunk : chunks.values()) {

            for (Torch torch : chunk.getTorches()) {

                int torchX = torch.x;
                int torchY = torch.y;

                int radius = torch.TORCH_RADIUS;

                for (int x = torchX - radius;
                     x <= torchX + radius;
                     x++) {

                    for (int y = torchY - radius;
                         y <= torchY + radius;
                         y++) {

                        Tile tile =
                            getLoadedTileAtWorldCoords(x, y);

                        if (tile == null) {
                            continue;
                        }

                        float dx = x - torchX;
                        float dy = y - torchY;

                        float distance =
                            (float)Math.sqrt(
                                dx * dx +
                                    dy * dy
                            );

                        if (distance > radius) {
                            continue;
                        }

                        float light =
                            1f - distance / radius;

                        tile.lightLevel =
                            Math.max(
                                tile.lightLevel,
                                light
                            );
                    }
                }
            }
        }
    }

    public void addTorch(int x, int y) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE);
        int chunkY = Math.floorDiv(y, Chunk.SIZE);

        loadChunk(chunkX, chunkY);

        Chunk chunk = getChunk(chunkX, chunkY);

        Torch torch = new Torch(x, y);

        chunk.addTorch(torch);

        bakeTorchArea(x, y, torch.TORCH_RADIUS);
    }

    private void bakeTorchArea(
        int torchX,
        int torchY,
        int radius
    ) {

        for (int x = torchX - radius;
             x <= torchX + radius;
             x++) {

            for (int y = torchY - radius;
                 y <= torchY + radius;
                 y++) {

                Tile tile =
                    getLoadedTileAtWorldCoords(x, y);

                if (tile == null) {
                    continue;
                }

                float dx = x - torchX;
                float dy = y - torchY;

                float distance =
                    (float)Math.sqrt(
                        dx * dx +
                            dy * dy
                    );

                if (distance > radius) {
                    continue;
                }

                float light =
                    1f - distance / radius;

                tile.lightLevel =
                    Math.max(
                        tile.lightLevel,
                        light
                    );
            }
        }
    }

    public Tile getLoadedTileAtWorldCoords(int globalX, int globalY) {

        int chunkX =
            Math.floorDiv(globalX, Chunk.SIZE);

        int chunkY =
            Math.floorDiv(globalY, Chunk.SIZE);

        Chunk chunk =
            getChunk(chunkX, chunkY);

        if (chunk == null) {
            return null;
        }

        int localX =
            Math.floorMod(globalX, Chunk.SIZE);

        int localY =
            Math.floorMod(globalY, Chunk.SIZE);

        return chunk.getTile(localX, localY);
    }
}

