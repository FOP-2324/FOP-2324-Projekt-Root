package projekt.controller.gui;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;

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

    @DoNotTouch
    public static void quit() {
        Platform.exit();
    }

    public static void loadMainMenuScene() {
        SceneSwitcher.getInstance().loadScene(SceneSwitcher.SceneType.MAIN_MENU);
    }

    public static void loadCreateGameScene() {
        SceneSwitcher.getInstance().loadScene(SceneSwitcher.SceneType.CREATE_GAME);
    }

    public static void loadSettingsScene() {
        System.out.println("Loading settings");
    }

    public static void loadHighscoreScene() {
        System.out.println("Loading highscores");
    }

    public static void loadGameScene() {
        SceneSwitcher.getInstance().loadScene(SceneSwitcher.SceneType.GAME_BOARD);
    }

    public static void loadAboutScene() {
        SceneSwitcher.getInstance().loadScene(SceneSwitcher.SceneType.ABOUT);
    }
}
