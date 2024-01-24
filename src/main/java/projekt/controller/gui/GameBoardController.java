package projekt.controller.gui;

import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.controller.GameController;
import projekt.model.Player;
import projekt.view.GameBoardBuilder;

public class GameBoardController implements SceneController {
    private final GameController gameController;
    private final PlayerActionsController playerActionsController;
    private final HexGridController hexGridController;

    private final GameBoardBuilder gameBoardBuilder;

    public GameBoardController(final GameController gameController) {
        this.playerActionsController = new PlayerActionsController(this,
                gameController.getActivePlayerControllerProperty());
        this.hexGridController = new HexGridController(gameController.getState().getGrid());
        this.gameBoardBuilder = new GameBoardBuilder(hexGridController.buildView(), playerActionsController::buildView);
        this.gameController = gameController;
        gameController.getActivePlayerControllerProperty().addListener((observable, oldValue, newValue) -> {
            updatePlayerInformation(newValue.getPlayer());
        });
    }

    public HexGridController getHexGridController() {
        return hexGridController;
    }

    public GameController getGameController() {
        return gameController;
    }

    public void updatePlayerInformation(Player player) {
        gameBoardBuilder.updatePlayerInformation(player, gameController.getState().getPlayers());
    }

    @Override
    public String getTitle() {
        return "Catan";
    }

    @Override
    public Builder<Region> getBuilder() {
        return gameBoardBuilder;
    }

    @Override
    public Region buildView() {
        gameController.startGame();
        return gameBoardBuilder.build();
    }

}
