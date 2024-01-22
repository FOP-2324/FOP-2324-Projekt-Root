package projekt.controller.gui;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Builder;
import projekt.Config;
import projekt.model.GameState;
import projekt.view.menus.CreateGameBuilder;

public class CreateGameController implements SceneController {
    private final CreateGameBuilder builder;
    private final GameState gameState;

    public CreateGameController(GameState gameState) {
        this.gameState = gameState;
        this.builder = new CreateGameBuilder(gameState.getPlayers(), SceneController::quit, this::addPlayerHandler,
                this::startGameHandler);
    }

    private boolean addPlayerHandler(final String name, final Color color) {
        try {
            gameState.newPlayer(color);
        } catch (IllegalStateException e) {
            return false;
        }
        builder.updatePlayers(gameState.getPlayers());
        return true;
    }

    private boolean startGameHandler() {
        if (gameState.getPlayers().size() < Config.MIN_PLAYERS) {
            return false;
        }
        SceneController.loadGameScene();
        return true;
    }

    @Override
    public Builder<Region> getBuilder() {
        return builder;
    }

    @Override
    public String getTitle() {
        return "Create Game";
    }

}
