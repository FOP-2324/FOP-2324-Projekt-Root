package projekt.controller.gui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A SceneController is responsible for dynamically managing a {@link Scene} and
 * its {@link Stage}.
 */
public interface SceneController extends Controller {
    /**
     * Specifies the title of the {@link Stage}.
     * This is used to set the title of the
     * {@link Stage}.
     *
     * @return The title of the {@link Stage}.
     */
    String getTitle();

    // --Setup Methods-- //

    public static void quit() {
        Platform.exit();
    }

    public static void mainMenuSceneLoader() {
        SceneSwitcher.getInstance().loadScene(SceneSwitcher.SceneType.MAIN_MENU);
    }

    public static void settingsSceneLoader() {
        System.out.println("Loading settings");
    }

    public static void highscoreSceneLoader() {
        System.out.println("Loading highscores");
    }

    public static void gameSceneLoader() {
        SceneSwitcher.getInstance().loadScene(SceneSwitcher.SceneType.GAME_BOARD);
    }
}
