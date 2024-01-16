package projekt;

import javafx.application.Application;
import javafx.stage.Stage;
import projekt.controller.gui.SceneSwitcher;
import projekt.controller.gui.SceneSwitcher.SceneType;

public class MyApplication extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setMinWidth(854);
        stage.setMinHeight(480);
        stage.setWidth(1280);
        stage.setHeight(720);

        SceneSwitcher.getInstance(stage).loadScene(SceneType.GAME_BOARD);
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
