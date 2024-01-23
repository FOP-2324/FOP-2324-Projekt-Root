package projekt.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Builder;

/**
 * A SceneController is responsible for dynamically managing a {@link Scene} and
 * its {@link Stage}.
 */
public abstract class SceneController extends Controller {
    protected SceneController(final Builder<Region> viewBuilder) {
        super(viewBuilder);
    }

    /**
     * Specifies the title of the {@link Stage}.
     * This is used to set the title of the
     * {@link Stage}.
     *
     * @return The title of the {@link Stage}.
     */
    public abstract String getTitle();

    // --Setup Methods-- //

    public static void quit() {
        Platform.exit();
    }

    public static Runnable mainMenuSceneLoader(final Stage stage) {
        return () -> SceneSwitcher.loadScene(SceneSwitcher.SceneType.MAIN_MENU, stage);
    }

    public static Runnable settingsSceneLoader(final Stage stage) {
        return () -> System.out.println("Loading settings");
    }

    public static Runnable highscoreSceneLoader(final Stage stage) {
        return () -> System.out.println("Loading highscores");

    }

    public static Runnable gameSceneLoader(final Stage stage) {
        return () -> {
        }; // () -> SceneSwitcher.loadScene(SceneSwitcher.SceneType.GAME_BOARD, stage);
    }
}
