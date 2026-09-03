package io.github.currentbrick.save;

import com.badlogic.gdx.utils.Json;

import java.io.File;
import java.nio.file.Files;

public class Save {

    private static final Json json = new Json();

    public static void saveWorld(
        String worldName,
        int seed,
        float playerX,
        float playerY,
        float worldTime
    ) {

        File worldDirectory =
            FileUtil.getWorldDirectory(worldName);

        SaveData data = new SaveData();

        data.name = worldName;
        data.seed = seed;
        data.playerX = playerX;
        data.playerY = playerY;
        data.worldTime = worldTime;

        File worldFile =
            new File(worldDirectory, "world.json");

        try {
            Files.writeString(
                worldFile.toPath(),
                json.prettyPrint(data)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SaveData loadWorld(String worldName) {

        File worldDirectory =
            FileUtil.getWorldDirectory(worldName);

        File worldFile =
            new File(worldDirectory, "world.json");

        try {

            String contents =
                Files.readString(worldFile.toPath());

            return json.fromJson(
                SaveData.class,
                contents
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
