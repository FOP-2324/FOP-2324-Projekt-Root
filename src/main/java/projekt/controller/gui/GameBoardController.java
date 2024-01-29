package projekt.controller.gui;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.controller.PlayerController;
import projekt.model.GameState;
import projekt.model.Player;
import projekt.view.GameBoardBuilder;

@DoNotTouch
public class GameBoardController implements SceneController {
    private final GameState gameState;
    private PlayerActionsController playerActionsController;
    private HexGridController hexGridController;
    private GameBoardBuilder gameBoardBuilder;

    public GameBoardController(final GameState gameState,
            final Property<PlayerController> activePlayerControllerProperty, final IntegerProperty diceRollProperty,
            final Property<Player> winnerProperty) {
        this.gameState = gameState;
        this.playerActionsController = new PlayerActionsController(this,
                activePlayerControllerProperty);
        this.hexGridController = new HexGridController(gameState.getGrid());
        this.gameBoardBuilder = new GameBoardBuilder(hexGridController.buildView(), playerActionsController::buildView);
        activePlayerControllerProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            Platform.runLater(() -> updatePlayerInformation(newValue.getPlayer()));
        });
        diceRollProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            Platform.runLater(() -> gameBoardBuilder.setDiceRoll(newValue.intValue()));
        });
        winnerProperty.subscribe((oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.INFORMATION, String.format("Player %s won!", newValue.getName()))
                        .showAndWait();
                SceneController.loadMainMenuScene();
            });
        });
    }

    public HexGridController getHexGridController() {
        return hexGridController;
    }

    public void updatePlayerInformation(final Player player) {
        Platform.runLater(() -> gameBoardBuilder.updatePlayerInformation(player, gameState.getPlayers()));
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
        return gameBoardBuilder.build();
    }
}
