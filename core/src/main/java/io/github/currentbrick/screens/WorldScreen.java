package io.github.currentbrick.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.badlogic.gdx.math.Vector3;
import io.github.currentbrick.blocks.Torch;
import io.github.currentbrick.player.ItemStack;
import io.github.currentbrick.player.ItemType;
import io.github.currentbrick.player.Player;
import io.github.currentbrick.generation.*;
import io.github.currentbrick.save.Save;
import io.github.currentbrick.save.SaveData;

public class WorldScreen implements Screen {

    private OrthographicCamera camera;

    private ShapeRenderer shapeRenderer;
    private PolygonSpriteBatch polygonSpriteBatch;

    private World world;

    private static final float TILE_WIDTH = 64f;
    private static final float TILE_HEIGHT = 32f;

    private float unloadTimer = 0f;
    private final float UNLOAD_INTERVAL = 3.0f;

    private static final float WATER_HEIGHT = 1.6f;
    private static final float HEIGHT_SCALE = 16f;
    private static final float WATER_ALPHA = 0.5f;

    private static final float SNOW_THICKNESS = 0.25f;
    private static final float SNOW_HEIGHT = 13f;

    private float worldTime = 12f;
    private float dayLength = 120f;

    private String worldName;

    Player player;
    private HUD hud;

    private int seed = -1;
    private int selectedTileX = Integer.MIN_VALUE;
    private int selectedTileY = Integer.MIN_VALUE;


    private Texture grassTexture;
    private TextureRegion grassRegion;

    private final float[] grassVertices = new float[20];

    private final short[] grassTriangles = {
        0, 1, 2,
        2, 3, 0
    };

    private static final float INTERACTION_RANGE = 3.0f;


    public WorldScreen(SaveData data) {

        worldTime = data.worldTime;
        seed = data.seed;
        worldName = data.name;

        player = new Player(
            data.playerX,
            data.playerY
        );
    }

    public WorldScreen(String worldName) {

        this.worldName = worldName;

        player = new Player(
            0,
            0
        );
    }

    @Override
    public void show() {

        shapeRenderer = new ShapeRenderer();

        polygonSpriteBatch =
            new PolygonSpriteBatch();

        // Create world
        if (seed != -1) {
            world = new World(seed);
        } else {
            world = new World();
        }

        // Camera
        camera = new OrthographicCamera();

        camera.setToOrtho(
            false,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        camera.position.set(
            0,
            0,
            0
        );

        camera.update();

        hud = new HUD(player.getInventory());

        grassTexture = new Texture(
            Gdx.files.internal(
                "textures/grass.png"
            )
        );

        grassTexture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        grassRegion =
            new TextureRegion(grassTexture);
    }

    private void updateTileSelection() {
        Vector3 mouse = new Vector3(
            Gdx.input.getX(),
            Gdx.input.getY(),
            0
        );

        camera.unproject(mouse);

        float mouseWorldX = mouse.x;
        float mouseWorldY = mouse.y;

        selectedTileX = Integer.MIN_VALUE;
        selectedTileY = Integer.MIN_VALUE;

        float closestHeight = Float.NEGATIVE_INFINITY;

        /*
         * Only check tiles around the player.
         * This is much cheaper than checking the entire world.
         */
        int playerTileX = Math.round(player.getX());
        int playerTileY = Math.round(player.getY());

        int searchRadius = 12;

        for (int x = playerTileX - searchRadius; x <= playerTileX + searchRadius; x++) {
            for (int y = playerTileY - searchRadius; y <= playerTileY + searchRadius; y++) {
                Tile tile = world.getTileAtWorldCoords(x, y);
                if (tile == null) continue;

                float screenX = (x - y) * TILE_WIDTH / 2f;
                float screenY = (x + y) * TILE_HEIGHT / 2f;
                float heightOffset = tile.height * HEIGHT_SCALE;

                screenY += heightOffset;

                float dx = Math.abs(mouseWorldX - screenX);
                float dy = Math.abs(mouseWorldY - screenY);
                float halfWidth = TILE_WIDTH / 2f;
                float halfHeight = TILE_HEIGHT / 2f;
                float normalizedX = dx / halfWidth;
                float normalizedY = dy / halfHeight;

                if (normalizedX + normalizedY <= 1f) {
                    if (heightOffset > closestHeight) {

                        closestHeight = heightOffset;

                        selectedTileX = x;
                        selectedTileY = y;
                    }
                }
            }
        }
    }

    private void drawTileSelection() {

        if (selectedTileX == Integer.MIN_VALUE) {
            return;
        }

        Tile tile = world.getTileAtWorldCoords(selectedTileX, selectedTileY);

        if (tile == null) {
            return;
        }

        float screenX = (selectedTileX - selectedTileY) * TILE_WIDTH / 2f;
        float screenY = (selectedTileX + selectedTileY) * TILE_HEIGHT / 2f;
        float heightOffset = tile.height * HEIGHT_SCALE;

        screenY += heightOffset;

        float topX = screenX;
        float topY = screenY + TILE_HEIGHT / 2f;
        float rightX = screenX + TILE_WIDTH / 2f;
        float rightY = screenY;
        float bottomX = screenX;
        float bottomY = screenY - TILE_HEIGHT / 2f;
        float leftX = screenX - TILE_WIDTH / 2f;
        float leftY = screenY;

        float dx =
            player.getX() - selectedTileX;

        float dy =
            player.getY() - selectedTileY;

        float distance =
            (float)Math.sqrt(
                dx * dx +
                    dy * dy
            );

        if (distance <= INTERACTION_RANGE) {

            // Close enough
            shapeRenderer.setColor(
                0.2f,
                1f,
                0.2f,
                0.9f
            );

        } else {

            // Too far away
            shapeRenderer.setColor(
                1f,
                0.2f,
                0.2f,
                0.9f
            );
        }
        shapeRenderer.line(topX, topY, rightX, rightY);
        shapeRenderer.line(rightX, rightY, bottomX, bottomY);
        shapeRenderer.line(bottomX, bottomY, leftX, leftY);
        shapeRenderer.line(leftX, leftY, topX, topY);
    }



    @Override
    public void render(float delta) {
        if (Gdx.input.justTouched()) {

            if (selectedTileX != Integer.MIN_VALUE) {

                interactWithTile();
            }
        }
        worldTime +=
            (24f / dayLength) * delta;

        if (worldTime >= 24f) {
            worldTime -= 24f;
        }

        player.update(
            delta,
            world
        );

        float playerScreenX =
            (player.getX() - player.getY())
                * TILE_WIDTH / 2f;

        float playerScreenY =
            (player.getX() + player.getY())
                * TILE_HEIGHT / 2f;

        int tileX =
            Math.round(player.getX());

        int tileY =
            Math.round(player.getY());

        Tile playerTile =
            world.getTileAtWorldCoords(
                tileX,
                tileY
            );

        float playerHeightOffset = 0f;

        if (playerTile != null) {

            playerHeightOffset =
                playerTile.height
                    * HEIGHT_SCALE;
        }

        camera.position.set(
            playerScreenX,
            playerScreenY
                + playerHeightOffset,
            0
        );

        camera.update();
        updateTileSelection();

        float night =
            getNightAmount();

        float skyR = 0.6f;
        float skyG = 0.82f;
        float skyB = 0.93f;

        float nightR = 0.04f;
        float nightG = 0.06f;
        float nightB = 0.12f;

        float r =
            skyR
                + (nightR - skyR)
                * night;

        float g =
            skyG
                + (nightG - skyG)
                * night;

        float b =
            skyB
                + (nightB - skyB)
                * night;

        Gdx.gl.glClearColor(
            r,
            g,
            b,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );

        Gdx.gl.glEnable(
            GL20.GL_BLEND
        );

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        float camX =
            camera.position.x;

        float camY =
            camera.position.y;

        int centerGlobalX =
            Math.round(
                (camX / TILE_WIDTH)
                    + (camY / TILE_HEIGHT)
            );

        int centerGlobalY =
            Math.round(
                (camY / TILE_HEIGHT)
                    - (camX / TILE_WIDTH)
            );

        unloadTimer += delta;

        if (unloadTimer >= UNLOAD_INTERVAL) {

            world.unloadDistantChunks(
                centerGlobalX,
                centerGlobalY
            );

            unloadTimer = 0f;
        }

        int tileRadius = 32;

        int minGlobalX =
            centerGlobalX - tileRadius;

        int maxGlobalX =
            centerGlobalX + tileRadius;

        int minGlobalY =
            centerGlobalY - tileRadius;

        int maxGlobalY =
            centerGlobalY + tileRadius;

        int minSum =
            minGlobalX + minGlobalY;

        int maxSum =
            maxGlobalX + maxGlobalY;

        int playerSum =
            Math.round(
                player.getX()
                    + player.getY()
            );

        for (int sum = maxSum; sum >= minSum; sum--) {
            for (int globalX = minGlobalX; globalX <= maxGlobalX; globalX++) {
                int globalY = sum - globalX;
                if (globalY < minGlobalY || globalY > maxGlobalY) continue;
                Tile tile = world.getTileAtWorldCoords(globalX, globalY);
                if (tile != null) {
                    float screenX = (globalX - globalY) * TILE_WIDTH / 2f;
                    float screenY = (globalX + globalY) * TILE_HEIGHT / 2f;
                    float heightOffset = tile.height * HEIGHT_SCALE;

                    shapeRenderer.setProjectionMatrix(camera.combined);

                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                    drawTile(globalX, globalY, tile);

                    if (tile.type == TileType.SAND && tile.height <= 1) {
                        drawWater(globalX, globalY, tile);}

                    shapeRenderer.end();

                    if (tile.type == TileType.GRASS) {
                        polygonSpriteBatch.setProjectionMatrix(camera.combined);

                        polygonSpriteBatch.begin();

                        drawGrassTexture(screenX, screenY, heightOffset, tile);

                        polygonSpriteBatch.end();
                    }

                    if (tile.biome == Biome.SNOWY_MOUNTAIN || tile.biome == Biome.TUNDRA) {
                        shapeRenderer.setProjectionMatrix(camera.combined);
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        drawSnow(globalX, globalY, tile);

                        shapeRenderer.end();
                    }

                    Tree tree = world.getTreeAtWorldCoords(globalX, globalY);

                    if (tree != null) {
                        tree.update(delta);
                        shapeRenderer.setProjectionMatrix(camera.combined);
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                        drawTree(tree);

                        shapeRenderer.end();
                    }

                    Torch torch =
                        world.getTorchAtWorldCoords(
                            globalX,
                            globalY
                        );

                    if (torch != null) {

                        shapeRenderer.setProjectionMatrix(
                            camera.combined
                        );

                        shapeRenderer.begin(
                            ShapeRenderer.ShapeType.Filled
                        );

                        drawTorch(torch);

                        shapeRenderer.end();
                    }
                }

                if (sum == playerSum) {
                    shapeRenderer.setProjectionMatrix(
                        camera.combined
                    );

                    shapeRenderer.begin(
                        ShapeRenderer.ShapeType.Filled
                    );

                    drawPlayer(
                        player.getX(),
                        player.getY()
                    );

                    shapeRenderer.end();
                }
            }
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawTileSelection();
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        hud.render(worldTime);
    }

    private void drawGrassTexture(
        float screenX,
        float screenY,
        float heightOffset,
        Tile tile
    ) {

        float topX =
            screenX;

        float topY =
            screenY
                + TILE_HEIGHT / 2f
                + heightOffset;

        float rightX =
            screenX
                + TILE_WIDTH / 2f;

        float rightY =
            screenY
                + heightOffset;

        float bottomX =
            screenX;

        float bottomY =
            screenY
                - TILE_HEIGHT / 2f
                + heightOffset;

        float leftX =
            screenX
                - TILE_WIDTH / 2f;

        float leftY =
            screenY
                + heightOffset;


        float color = applyLighting(getGrassColor(tile), tile).toFloatBits();

        grassVertices[0] = topX;
        grassVertices[1] = topY;
        grassVertices[2] = color;
        grassVertices[3] = grassRegion.getU();
        grassVertices[4] = grassRegion.getV();

        // RIGHT
        grassVertices[5] = rightX;
        grassVertices[6] = rightY;
        grassVertices[7] = color;
        grassVertices[8] = grassRegion.getU2();
        grassVertices[9] = grassRegion.getV();

        // BOTTOM
        grassVertices[10] = bottomX;
        grassVertices[11] = bottomY;
        grassVertices[12] = color;
        grassVertices[13] = grassRegion.getU2();
        grassVertices[14] = grassRegion.getV2();

        // LEFT
        grassVertices[15] = leftX;
        grassVertices[16] = leftY;
        grassVertices[17] = color;
        grassVertices[18] = grassRegion.getU();
        grassVertices[19] = grassRegion.getV2();

        polygonSpriteBatch.draw(
            grassTexture,
            grassVertices,
            0,
            grassVertices.length,
            grassTriangles,
            0,
            grassTriangles.length
        );
    }


    // TREE


    private void drawTree(Tree tree) {

        float screenX =
            (tree.x - tree.y)
                * TILE_WIDTH / 2f;

        float screenY =
            (tree.x + tree.y)
                * TILE_HEIGHT / 2f;

        float shake =
            tree.getShakeOffset();

        screenX += shake;

        Tile tile =
            world.getTileAtWorldCoords(
                tree.x,
                tree.y
            );

        if (tile == null) {
            return;
        }

        float groundY =
            screenY
                + tile.height
                * HEIGHT_SCALE;

        float trunkWidth =
            8f * tree.size;

        float trunkHeight =
            28f * tree.size;


        // TRUNK


        shapeRenderer.setColor(
            applyLighting(
                new Color(0.55f, 0.34f, 0.20f, 1f),
                tile)
        );

        shapeRenderer.rect(
            screenX - trunkWidth / 2f,
            groundY,
            trunkWidth,
            trunkHeight
        );


        // LEAVES

        shapeRenderer.setColor(applyLighting(getGrassColor(tile), tile));

        float leafSize = 30f * tree.size;

        shapeRenderer.triangle(
            screenX,
            groundY
                + trunkHeight
                + leafSize,

            screenX - leafSize,
            groundY + trunkHeight,

            screenX + leafSize,
            groundY + trunkHeight
        );
    }

    private void drawTorch(Torch torch) {

        float screenX =
            (torch.x - torch.y)
                * TILE_WIDTH / 2f;

        float screenY =
            (torch.x + torch.y)
                * TILE_HEIGHT / 2f;

        Tile tile =
            world.getTileAtWorldCoords(
                torch.x,
                torch.y
            );

        if (tile == null) {
            return;
        }

        float groundY =
            screenY +
                tile.height * HEIGHT_SCALE;

        // Stick
        shapeRenderer.setColor(
            applyLighting(
                new Color(
                    0.35f,
                    0.18f,
                    0.08f,
                    1f
                ),
                tile
            )
        );

        shapeRenderer.rect(
            screenX - 3,
            groundY,
            6,
            20
        );

        // Flame
        shapeRenderer.setColor(
            new Color(
                1f,
                0.55f,
                0.1f,
                1f
            )
        );

        shapeRenderer.circle(
            screenX,
            groundY + 25,
            6
        );
    }


    // TILE


    private void drawTile(
        int globalX,
        int globalY,
        Tile tile
    ) {

        Color tileColor =
            switch (tile.type) {

                case GRASS ->
                    getGrassColor(tile);

                case SAND ->
                    new Color(
                        0.9f,
                        0.8f,
                        0.4f,
                        1f
                    );

                case STONE, MOUNTAIN ->
                    new Color(
                        0.5f,
                        0.5f,
                        0.5f,
                        1f
                    );

                default ->
                    Color.PURPLE;
            };

        float screenX =
            (globalX - globalY)
                * TILE_WIDTH / 2f;

        float screenY =
            (globalX + globalY)
                * TILE_HEIGHT / 2f;

        float heightOffset =
            tile.height
                * HEIGHT_SCALE;


        // NEIGHBOURS


        Tile leftNeighbour =
            world.getTileAtWorldCoords(
                globalX - 1,
                globalY
            );

        int leftNeighbourHeight =
            leftNeighbour != null
                ? leftNeighbour.height
                : 0;

        int leftDifference =
            tile.height
                - leftNeighbourHeight;

        Tile rightNeighbour =
            world.getTileAtWorldCoords(
                globalX,
                globalY - 1
            );

        int rightNeighbourHeight =
            rightNeighbour != null
                ? rightNeighbour.height
                : 0;

        int rightDifference =
            tile.height
                - rightNeighbourHeight;


        // DIAMOND


        float topX =
            screenX;

        float topY =
            screenY
                + TILE_HEIGHT / 2f
                + heightOffset;

        float rightX =
            screenX
                + TILE_WIDTH / 2f;

        float rightY =
            screenY
                + heightOffset;

        float bottomX =
            screenX;

        float bottomY =
            screenY
                - TILE_HEIGHT / 2f
                + heightOffset;

        float leftX =
            screenX
                - TILE_WIDTH / 2f;

        float leftY =
            screenY
                + heightOffset;


        // LEFT WALL


        if (leftDifference > 0) {

            float leftWallDepth =
                leftDifference
                    * HEIGHT_SCALE;

            float wallBottomLeftY =
                leftY
                    - leftWallDepth;

            float wallBottomBottomY =
                bottomY
                    - leftWallDepth;

            shapeRenderer.setColor(
                applyLighting(
                    new Color(
                        tileColor.r - 0.05f,
                        tileColor.g - 0.05f,
                        tileColor.b - 0.05f,
                        1f
                    ), tile
                )
            );

            shapeRenderer.triangle(
                leftX,
                leftY,

                bottomX,
                bottomY,

                bottomX,
                wallBottomBottomY
            );

            shapeRenderer.triangle(
                leftX,
                leftY,

                bottomX,
                wallBottomBottomY,

                leftX,
                wallBottomLeftY
            );
        }


        // RIGHT WALL


        if (rightDifference > 0) {

            float rightWallDepth =
                rightDifference
                    * HEIGHT_SCALE;

            float wallBottomRightY =
                rightY
                    - rightWallDepth;

            float wallBottomBottomY =
                bottomY
                    - rightWallDepth;

            shapeRenderer.setColor(
                applyLighting(
                    new Color(
                        tileColor.r - 0.1f,
                        tileColor.g - 0.1f,
                        tileColor.b - 0.1f,
                        1f
                    ), tile
                )
            );

            shapeRenderer.triangle(
                rightX,
                rightY,

                bottomX,
                bottomY,

                bottomX,
                wallBottomBottomY
            );

            shapeRenderer.triangle(
                rightX,
                rightY,

                bottomX,
                wallBottomBottomY,

                rightX,
                wallBottomRightY
            );
        }


        // TOP


        /*
         * IMPORTANT:
         *
         * Grass top is NOT drawn here.
         *
         * It is drawn using the grass texture in
         * drawGrassTexture().
         */

        if (tile.type != TileType.GRASS) {

            shapeRenderer.setColor(
                applyLighting(tileColor, tile)
            );

            shapeRenderer.triangle(
                topX,
                topY,

                rightX,
                rightY,

                bottomX,
                bottomY
            );

            shapeRenderer.triangle(
                topX,
                topY,

                bottomX,
                bottomY,

                leftX,
                leftY
            );
        }
    }


    // WATER


    private void drawWater(float globalX, float globalY, Tile tile) {
        Gdx.gl.glEnable(GL20.GL_BLEND);

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );
        float screenX =
            (globalX - globalY)
                * TILE_WIDTH / 2f;

        float screenY =
            (globalX + globalY)
                * TILE_HEIGHT / 2f;

        float waterOffset =
            WATER_HEIGHT
                * HEIGHT_SCALE;

        float topX =
            screenX;

        float topY =
            screenY
                + TILE_HEIGHT / 2f
                + waterOffset;

        float rightX =
            screenX
                + TILE_WIDTH / 2f;

        float rightY =
            screenY
                + waterOffset;

        float bottomX =
            screenX;

        float bottomY =
            screenY
                - TILE_HEIGHT / 2f
                + waterOffset;

        float leftX =
            screenX
                - TILE_WIDTH / 2f;

        float leftY =
            screenY
                + waterOffset;

        shapeRenderer.setColor(
            applyLighting(
                new Color(
                    0f,
                    0.43f,
                    1f,
                    WATER_ALPHA
                ), tile
            )
        );

        shapeRenderer.triangle(
            topX,
            topY,
            rightX,
            rightY,
            bottomX,
            bottomY
        );

        shapeRenderer.triangle(
            topX,
            topY,
            bottomX,
            bottomY,
            leftX,
            leftY
        );
    }


    // SNOW


    private void drawSnow(
        int globalX,
        int globalY,
        Tile tile
    ) {

        float screenX =
            (globalX - globalY)
                * TILE_WIDTH / 2f;

        float screenY =
            (globalX + globalY)
                * TILE_HEIGHT / 2f;

        float baseHeight =
            tile.height
                * HEIGHT_SCALE;

        float snowHeight =
            SNOW_THICKNESS
                * HEIGHT_SCALE;

        float bottomOffset =
            baseHeight;

        float topOffset =
            baseHeight
                + snowHeight;


        // BOTTOM


        float bottomTopX =
            screenX;

        float bottomTopY =
            screenY
                + TILE_HEIGHT / 2f
                + bottomOffset;

        float bottomRightX =
            screenX
                + TILE_WIDTH / 2f;

        float bottomRightY =
            screenY
                + bottomOffset;

        float bottomBottomX =
            screenX;

        float bottomBottomY =
            screenY
                - TILE_HEIGHT / 2f
                + bottomOffset;

        float bottomLeftX =
            screenX
                - TILE_WIDTH / 2f;

        float bottomLeftY =
            screenY
                + bottomOffset;


        // TOP


        float topTopX =
            screenX;

        float topTopY =
            screenY
                + TILE_HEIGHT / 2f
                + topOffset;

        float topRightX =
            screenX
                + TILE_WIDTH / 2f;

        float topRightY =
            screenY
                + topOffset;

        float topBottomX =
            screenX;

        float topBottomY =
            screenY
                - TILE_HEIGHT / 2f
                + topOffset;

        float topLeftX =
            screenX
                - TILE_WIDTH / 2f;

        float topLeftY =
            screenY
                + topOffset;


        // LEFT SNOW WALL


        shapeRenderer.setColor(
            applyLighting(
                new Color(
                    0.82f,
                    0.82f,
                    0.82f,
                    1f
                ), tile
            )
        );

        shapeRenderer.triangle(
            bottomLeftX,
            bottomLeftY,

            bottomBottomX,
            bottomBottomY,

            topBottomX,
            topBottomY
        );

        shapeRenderer.triangle(
            bottomLeftX,
            bottomLeftY,

            topBottomX,
            topBottomY,

            topLeftX,
            topLeftY
        );


        // RIGHT SNOW WALL


        shapeRenderer.setColor(
            applyLighting(
                new Color(
                    0.75f,
                    0.75f,
                    0.75f,
                    1f
                ), tile
            )
        );

        shapeRenderer.triangle(
            bottomRightX,
            bottomRightY,

            bottomBottomX,
            bottomBottomY,

            topBottomX,
            topBottomY
        );

        shapeRenderer.triangle(
            bottomRightX,
            bottomRightY,

            topBottomX,
            topBottomY,

            topRightX,
            topRightY
        );


        // SNOW TOP


        shapeRenderer.setColor(
            applyLighting(
                new Color(
                    0.95f,
                    0.95f,
                    0.95f,
                    1f
                ), tile
            )
        );

        shapeRenderer.triangle(
            topTopX,
            topTopY,

            topRightX,
            topRightY,

            topBottomX,
            topBottomY
        );

        shapeRenderer.triangle(
            topTopX,
            topTopY,

            topBottomX,
            topBottomY,

            topLeftX,
            topLeftY
        );
    }


    // GRASS COLOUR


    private Color getGrassColor(Tile tile) {

        float temperature =
            tile.temperature;

        float moisture =
            tile.moisture;

        float red = 0.25f;
        float green = 0.7f;
        float blue = 0.2f;


        // COLD


        if (temperature < 0.5f) {

            float cold =
                (0.5f - temperature)
                    * 2f;

            red += cold * 0.15f;
            green -= cold * 0.15f;
            blue += cold * 0.15f;
        }


        // HOT


        if (temperature > 0.5f) {

            float hot =
                (temperature - 0.5f)
                    * 2f;

            red += hot * 0.15f;
            green += hot * 0.05f;
            blue -= hot * 0.12f;
        }


        if (moisture > 0.5f) {

            float wet =
                (moisture - 0.5f)
                    * 2f;

            green += wet * 0.10f;
            red -= wet * 0.05f;
        }


        // DRY


        if (moisture < 0.5f) {

            float dry =
                (0.5f - moisture)
                    * 2f;

            red += dry * 0.18f;
            green -= dry * 0.10f;
            blue -= dry * 0.10f;
        }

        return new Color(
            red,
            green,
            blue,
            1f
        );
    }


    // PLAYER


    private void drawPlayer(
        float worldX,
        float worldY
    ) {

        int tileX =
            Math.round(worldX);

        int tileY =
            Math.round(worldY);

        Tile tile =
            world.getTileAtWorldCoords(
                tileX,
                tileY
            );

        if (tile == null) {
            return;
        }

        float screenX =
            (worldX - worldY)
                * TILE_WIDTH / 2f;

        float screenY =
            (worldX + worldY)
                * TILE_HEIGHT / 2f;

        float groundY =
            screenY
                + tile.height
                * HEIGHT_SCALE;

        shapeRenderer.setColor(
            applyLighting(Color.WHITE, tile)
        );

        // Body
        shapeRenderer.rect(
            screenX - 8,
            groundY,
            16,
            25
        );

        // Head
        shapeRenderer.circle(
            screenX,
            groundY + 32,
            8
        );
    }


    // NIGHT


    private float getNightAmount() {

        float sunrise = 6f;
        float sunset = 18f;
        float transition = 2f;

        if (
            worldTime >= sunrise + transition
                && worldTime <= sunset - transition
        ) {
            return 0f;
        }

        if (
            worldTime < sunrise - transition
                || worldTime > sunset + transition
        ) {
            return 1f;
        }

        if (worldTime < sunrise + transition) {

            float t =
                (worldTime
                    - (sunrise - transition))
                    / (transition * 2f);

            return 1f - smoothstep(t);
        }

        float t =
            (worldTime
                - (sunset - transition))
                / (transition * 2f);

        return smoothstep(t);
    }

    private Color applyLighting(Color color, Tile tile) {

        float night =
            getNightAmount();

        float brightness =
            1f - night * 0.8f;
        brightness += tile.lightLevel * night * 0.8f;

        brightness = Math.min(brightness, 1f);

        float torch = tile.lightLevel * night;

        float red =
            brightness + torch * 0.5f;

        float green =
            brightness + torch * 0.25f;

        float blue =
            brightness - torch * 0.1f;

        return new Color(
            color.r * red,
            color.g * green,
            color.b * blue,
            color.a
        );
    }

    private float smoothstep(
        float x
    ) {

        x =
            Math.max(
                0f,
                Math.min(
                    1f,
                    x
                )
            );

        return x * x * (3f - 2f * x);
    }


    // TIME


    private int getGameHour() {

        return (int) worldTime;
    }

    private int getGameMinute() {

        return (int)
            ((worldTime
                - getGameHour())
                * 60f);
    }

    private void interactWithTile() {
        if (selectedTileX == Integer.MIN_VALUE || selectedTileY == Integer.MIN_VALUE) {
            return;
        }

        // Check distance to selected tile
        float dx = player.getX() - selectedTileX;
        float dy = player.getY() - selectedTileY;

        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        if (distance > INTERACTION_RANGE) {
            System.out.println("Too far away!");
            return;
        }
        ItemStack stack = hud.hotbar.getSelectedItem();
        if (stack != null) {
            if (player.getInventory().hasItem(ItemType.TORCH, 1) && stack.getType() == ItemType.TORCH) {

                // Don't place another torch on an existing torch
                Torch existingTorch =
                    world.getTorchAtWorldCoords(
                        selectedTileX,
                        selectedTileY
                    );

                if (existingTorch != null) {
                    return;
                }

                // Place torch
                world.addTorch(
                    selectedTileX,
                    selectedTileY
                );

                // Remove one torch from inventory
                player.getInventory().removeItem(
                    ItemType.TORCH,
                    1
                );

                System.out.println(
                    "Placed torch!"
                );

                return;
            }
        }

        // ==========================================
        // TREE INTERACTION
        // ==========================================

        Tree tree =
            world.getTreeAtWorldCoords(
                selectedTileX,
                selectedTileY
            );

        if (tree == null) {
            return;
        }

        // Tree can be damaged
        if (tree.health > 0) {

            tree.damage(1);
            tree.shake();

            System.out.println(
                "Tree damaged! Health: "
                    + tree.health
            );

            return;
        }

        // Tree is dead → collect wood
        boolean added =
            player.getInventory().addItem(
                ItemType.WOOD,
                3
            );

        if (added) {

            world.removeTreeAtWorldCoords(
                selectedTileX,
                selectedTileY
            );

            System.out.println(
                "Chopped tree! +3 Wood"
            );

        } else {

            System.out.println(
                "Inventory full!"
            );
        }
    }


    // RESIZE


    @Override
    public void resize(
        int width,
        int height
    ) {

        camera.viewportWidth =
            width;

        camera.viewportHeight =
            height;

        camera.update();
    }


    // PAUSE / RESUME


    @Override
    public void pause() {

        saveWorld();
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {

        saveWorld();
    }


    // DISPOSE


    @Override
    public void dispose() {

        saveWorld();

        shapeRenderer.dispose();

        polygonSpriteBatch.dispose();

        grassTexture.dispose();

        hud.dispose();
    }


    // SAVE


    public void saveWorld() {

        Save.saveWorld(
            worldName,
            world.getSeed(),
            player.getX(),
            player.getY(),
            worldTime
        );

        System.out.println(
            "World saved: "
                + worldName
        );
    }


}
