package projekt.controller;

import java.util.function.Function;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A SceneSwitcher is responsible for switching between the different
 * {@link Scene}s.
 */
public class SceneSwitcher {

    /**
     * Overrides the default constructor.
     */
    private SceneSwitcher() {
        throw new RuntimeException("Cannot instantiate SceneSwitcher");
    }

    /**
     * An enum that represents the different scenes that can be switched to.
     */
    public enum SceneType {
        // --Enum Constants-- //

        MAIN_MENU((stage) -> new MainMenuSceneController(stage)),
        GAME_BOARD((stage) -> GameController.getInstance());

        private final Function<Stage, SceneController> controllerGenerator;

        SceneType(final Function<Stage, SceneController> controller) {
            this.controllerGenerator = controller;
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
    public static Scene loadScene(final SceneType sceneType, final Stage stage) {
        final SceneController controller = sceneType.controllerGenerator.apply(stage);
        final Scene scene = new Scene(controller.getView());
        stage.setScene(scene);
        stage.setTitle(controller.getTitle());
        stage.show();
        return scene;
    }
}
