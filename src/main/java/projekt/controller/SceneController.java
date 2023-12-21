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
    protected SceneController(Builder<Region> viewBuilder) {
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

    public static Runnable mainMenuSceneLoader(Stage stage) {
        return () -> SceneSwitcher.loadScene(SceneSwitcher.SceneType.MAIN_MENU, stage);
    }

    public static Runnable settingsSceneLoader(Stage stage) {
        return () -> System.out.println("Loading settings");
    }

    public static Runnable highscoreSceneLoader(Stage stage) {
        return () -> System.out.println("Loading highscores");

    }

    public static Runnable gameSceneLoader(Stage stage) {
        return () -> SceneSwitcher.loadScene(SceneSwitcher.SceneType.GAME_BOARD, stage);
    }
}