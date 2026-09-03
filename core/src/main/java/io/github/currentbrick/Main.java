package io.github.currentbrick;

import com.badlogic.gdx.Game;
import io.github.currentbrick.screens.MenuScreen;
import io.github.currentbrick.screens.WorldScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
