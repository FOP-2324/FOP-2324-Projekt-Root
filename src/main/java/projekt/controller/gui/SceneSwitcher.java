package projekt.controller.gui;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import projekt.controller.GameController;

/**
 * A SceneSwitcher is responsible for switching between the different
 * {@link Scene}s.
 */
public class SceneSwitcher {
    private final Stage stage;
    private static SceneSwitcher INSTANCE;

    private SceneSwitcher(Stage stage) {
        this.stage = stage;
    }

    public static SceneSwitcher getInstance(Stage stage) {
        if (INSTANCE == null) {
            INSTANCE = new SceneSwitcher(stage);
        }
        return INSTANCE;
    }

    public static SceneSwitcher getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("SceneSwitcher has not been initialized yet.");
        }
        return INSTANCE;
    }

    /**
     * An enum that represents the different scenes that can be switched to.
     */
    public enum SceneType {
        MAIN_MENU(new MainMenuSceneController()),
        GAME_BOARD(new GameBoardController(GameController.getInstance()));

        private final SceneController controller;

        SceneType(final SceneController controller) {
            this.controller = controller;
        }
    }

    /**
     * Loads the given {@link SceneType} and initializes its Controller.
     *
     * @param sceneType The {@link SceneType} to load.
     * @param stage     The {@link Stage} to show the {@link Scene} on.
     * @return The {@link Scene} that was switched to.
     * @see #loadScene(SceneAndController, Stage)
     */
    public Scene loadScene(final SceneType sceneType) {
        System.out.println("Loading scene: " + sceneType);
        final SceneController controller = sceneType.controller;
        final Scene scene = new Scene(controller.buildView());
        scene.setFill(Color.PINK);
        stage.setScene(scene);
        stage.setTitle(controller.getTitle());
        stage.show();
        return scene;
    }
}
