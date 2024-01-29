package projekt.controller.gui;

import java.util.function.Supplier;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;

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
    private final GameController gameController;
    private static SceneSwitcher INSTANCE;
    private final Runnable gameLoopStarter;

    @DoNotTouch
    private SceneSwitcher(final Stage stage, final GameController gameController, final Runnable gameLoopStarter) {
        this.stage = stage;
        this.gameController = gameController;
        this.gameLoopStarter = gameLoopStarter;
    }

    @DoNotTouch
    public static SceneSwitcher getInstance(final Stage stage, final GameController gameController,
            final Runnable gameLoopStarter) {
        if (INSTANCE == null) {
            INSTANCE = new SceneSwitcher(stage, gameController, gameLoopStarter);
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
        CREATE_GAME(() -> new CreateGameController(SceneSwitcher.getInstance().gameController.getState())),
        GAME_BOARD(() -> {
            getInstance().gameLoopStarter.run();
            final GameBoardController gameBoardController = new GameBoardController(
                    getInstance().gameController.getState(),
                    getInstance().gameController.getActivePlayerControllerProperty(),
                    getInstance().gameController.getCurrentDiceRollProperty(),
                    getInstance().gameController.getState().getWinnerProperty());

            return gameBoardController;
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
        scene.setFill(Color.PINK);
        stage.setScene(scene);
        stage.setTitle(controller.getTitle());
        stage.show();
    }
}
