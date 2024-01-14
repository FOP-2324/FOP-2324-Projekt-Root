package projekt;

import javafx.application.Application;
import javafx.stage.Stage;
import projekt.controller.SceneSwitcher;
import projekt.controller.SceneSwitcher.SceneType;

public class MyApplication extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setMinWidth(450);
        stage.setMinHeight(400);

        SceneSwitcher.loadScene(SceneType.MAIN_MENU, stage);
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
