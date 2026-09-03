package io.github.currentbrick.save;

import java.io.File;
import java.nio.file.Path;

public class FileUtil {

    private static final String GAME_NAME = "Currentbrick/Isometric";

    public static File getSaveDirectory() {

        String osName = java.lang.System.getProperty("os.name").toLowerCase();

        File directory;

        if (osName.contains("mac")) {

            Path path = Path.of(
                java.lang.System.getProperty("user.home"),
                "Library",
                "Application Support",
                GAME_NAME,
                "saves"
            );

            directory = path.toFile();

        } else if (osName.contains("win")) {

            Path path = Path.of(
                java.lang.System.getProperty("user.home"),
                "AppData",
                "Local",
                GAME_NAME,
                "saves"
            );

            directory = path.toFile();

        } else {

            // Linux
            Path path = Path.of(
                java.lang.System.getProperty("user.home"),
                ".local",
                "share",
                GAME_NAME,
                "saves"
            );

            directory = path.toFile();
        }

        directory.mkdirs();

        return directory;
    }

    public static File getWorldDirectory(String worldName) {

        File worldDirectory = new File(
            getSaveDirectory(),
            worldName
        );

        worldDirectory.mkdirs();

        return worldDirectory;
    }
}
