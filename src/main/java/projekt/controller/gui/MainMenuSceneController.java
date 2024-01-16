package projekt.controller.gui;

import projekt.view.menus.MainMenuBuilder;

public class MainMenuSceneController extends SceneController {

    public MainMenuSceneController() {
        super(new MainMenuBuilder(SceneController::quit, SceneController::gameSceneLoader,
                SceneController::settingsSceneLoader, SceneController::highscoreSceneLoader));
    }

    @Override
    public String getTitle() {
        return "Hauptmenü";
    }

}
