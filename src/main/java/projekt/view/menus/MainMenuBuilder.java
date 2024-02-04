package projekt.view.menus;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class MainMenuBuilder extends MenuBuilder {
    private final Runnable loadGameScene;
    private final Runnable loadSettingsScene;
    private final Runnable loadHighscoreScene;

    public MainMenuBuilder(
            final Runnable quitHandler, final Runnable createGameScene, final Runnable loadSettingsScene,
            final Runnable loadHighscoreScene) {
        super("Main Menu", "Quit", quitHandler);
        this.loadGameScene = createGameScene;
        this.loadSettingsScene = loadSettingsScene;
        this.loadHighscoreScene = loadHighscoreScene;
    }

    @Override
    protected Node initCenter() {
        final VBox mainBox = new VBox();
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setSpacing(10);

        final Button startButton = new Button("Create Game");
        startButton.setOnAction(e -> loadGameScene.run());

        final Button settingsButton = new Button("Settings");
        settingsButton.setOnAction(e -> loadSettingsScene.run());

        final Button scoresButton = new Button("Highscores");
        scoresButton.setOnAction(e -> loadHighscoreScene.run());

        mainBox.getChildren().addAll(startButton, settingsButton, scoresButton);

        return mainBox;
    }
}
