package projekt.controller;

import javafx.stage.Stage;
import projekt.view.menus.MainMenuBuilder;

public class MainMenuSceneController extends SceneController {

    public MainMenuSceneController(Stage stage) {
        super(new MainMenuBuilder(SceneController::quit, SceneController.gameSceneLoader(stage),
                SceneController.settingsSceneLoader(stage), SceneController.highscoreSceneLoader(stage)));
    }

    @Override
    public String getTitle() {
        return "Hauptmenü";
    }

}
