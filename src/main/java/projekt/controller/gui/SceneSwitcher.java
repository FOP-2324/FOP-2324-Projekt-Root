package projekt.controller.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;

import javafx.scene.Scene;
import javafx.stage.Stage;
import projekt.controller.GameController;

/**
 * A SceneSwitcher is responsible for switching between the different
 * {@link Scene}s.
 */
@DoNotTouch
public class SceneSwitcher {
    private final Stage stage;
    private GameController gameController;
    private static SceneSwitcher INSTANCE;
    private final Consumer<GameController> gameLoopStarter;

    @DoNotTouch
    private SceneSwitcher(final Stage stage, final Consumer<GameController> gameLoopStarter) {
        this.stage = stage;
        this.gameLoopStarter = gameLoopStarter;
    }

    @DoNotTouch
    public static SceneSwitcher getInstance(final Stage stage, final Consumer<GameController> gameLoopStarter) {
        if (INSTANCE == null) {
            INSTANCE = new SceneSwitcher(stage, gameLoopStarter);
        }
        return INSTANCE;
    }

    @DoNotTouch
    public static SceneSwitcher getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("SceneSwitcher has not been initialized yet.");
        }
        return INSTANCE;
    }

    /**
     * An enum that represents the different scenes that can be switched to.
     */
    public static enum SceneType {
        MAIN_MENU(MainMenuSceneController::new),
        CREATE_GAME(() -> {
            SceneSwitcher.getInstance().gameController = new GameController();
            return new CreateGameController(SceneSwitcher.getInstance().gameController.getState());
        }),
        GAME_BOARD(() -> {
            SceneSwitcher.getInstance().gameLoopStarter.accept(SceneSwitcher.getInstance().gameController);
            return new GameBoardController(
                    getInstance().gameController.getState(),
                    getInstance().gameController.getActivePlayerControllerProperty(),
                    getInstance().gameController.getCurrentDiceRollProperty(),
                    getInstance().gameController.getState().getWinnerProperty());
        });

        private final Supplier<SceneController> controller;

        SceneType(final Supplier<SceneController> controller) {
            this.controller = controller;
        }
    }

    /**
     * Loads the given {@link SceneType} and initializes its Controller.
     * // TODO: UI Thread
     *
     * @param sceneType The {@link SceneType} to load.
     * @param stage     The {@link Stage} to show the {@link Scene} on.
     * @return The {@link Scene} that was switched to.
     * @see #loadScene(SceneAndController, Stage)
     */
    @DoNotTouch
    public void loadScene(final SceneType sceneType) {
        System.out.println("Loading scene: " + sceneType);
        final SceneController controller = sceneType.controller.get();
        final Scene scene = new Scene(controller.buildView());
        scene.getStylesheets().add("css/hexmap.css");
        stage.setScene(scene);
        stage.setTitle(controller.getTitle());
        stage.show();
    }
}
