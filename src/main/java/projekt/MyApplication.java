package projekt;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import projekt.controller.GameController;
import projekt.controller.gui.SceneSwitcher;
import projekt.controller.gui.SceneSwitcher.SceneType;

import java.util.Objects;

public class MyApplication extends Application {
    private final GameController gameController = new GameController();
    private final Runnable gameLoopStart = () -> {
        final Thread gameLoopThread = new Thread(gameController::startGame);
        gameLoopThread.setName("GameLoopThread");
        gameLoopThread.setDaemon(true);
        gameLoopThread.start();
    };

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setMinWidth(1000);
        stage.setMinHeight(520);
        stage.setWidth(1280);
        stage.setHeight(720);

        Font.loadFont(
            Objects.requireNonNull(
                MyApplication.class.getResource("/fonts/MaterialDesignIconsDesktop.ttf")
            ).toExternalForm(),
            10
        );

        SceneSwitcher.getInstance(stage, gameController, gameLoopStart).loadScene(SceneType.MAIN_MENU);
    }

    /**
     * The main method of the application.
     *
     * @param args The launch arguments of the application.
     */
    public static void main(final String[] args) {
        launch(args);
    }
}
