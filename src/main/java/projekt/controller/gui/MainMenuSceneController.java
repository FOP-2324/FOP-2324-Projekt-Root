package projekt.controller.gui;

import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.view.menus.MainMenuBuilder;

public class MainMenuSceneController implements SceneController {
    private final Builder<Region> builder;

    public MainMenuSceneController() {
        builder = new MainMenuBuilder(SceneController::quit, SceneController::gameSceneLoader,
                SceneController::settingsSceneLoader, SceneController::highscoreSceneLoader);
    }

    @Override
    public String getTitle() {
        return "Hauptmenü";
    }

    @Override
    public Builder<Region> getBuilder() {
        return builder;
    }

}
