package projekt.controller.gui;

import java.util.function.Consumer;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.controller.PlayerController;
import projekt.controller.PlayerObjective;
import projekt.controller.actions.BuildRoadAction;
import projekt.controller.actions.BuildVillageAction;
import projekt.controller.actions.EndTurnAction;
import projekt.controller.actions.RollDiceAction;
import projekt.controller.actions.UpgradeVillageAction;
import projekt.model.Player;
import projekt.model.PlayerState;
import projekt.view.gameControls.PlayerActionsBuilder;
import projekt.model.buildings.Settlement;

public class PlayerActionsController implements Controller {
    private final PlayerActionsBuilder builder;
    private final GameBoardController gameBoardController;
    private final Property<PlayerController> playerControllerProperty;
    @DoNotTouch
    private final ChangeListener<PlayerObjective> playerObjectiveListener = (observableObjective, oldObjective,
            newObjective) -> {
        Platform.runLater(() -> enableButtonBasedOnObjective(newObjective));
    };

    @DoNotTouch
    public PlayerActionsController(GameBoardController gameBoardController,
            Property<PlayerController> playerController) {
        this.gameBoardController = gameBoardController;
        this.playerControllerProperty = playerController;

        this.builder = new PlayerActionsBuilder(actionWrapper(this::buildVillageButtonAction, true),
                actionWrapper(this::upgradeVillageButtonAction, true), actionWrapper(this::buildRoadButtonAction, true),
                actionWrapper(this::buyDevelopmentCardButtonAction, false),
                actionWrapper(this::endTurnButtonAction, false),
                this::rollDiceButtonAction, this::tradeButtonAction, this::abortButtonAction);
    }

    private void enableButtonBasedOnObjective(PlayerObjective objective) {
        System.out.println("objective: " + objective);
        removeAllHighlights();
        drawRoads();
        gameBoardController.updatePlayerInformation(getPlayer());
        builder.disableAllButtons();
        switch (objective) {
            case REGULAR_TURN:
                updateBuildVillageButtonState();
                updateUpgradeVillageButtonState();
                updateBuildRoadButtonState();
                builder.enableTradeButton();
                builder.enableEndTurnButton();
                break;
            case DROP_HALF_CARDS:
                builder.enableEndTurnButton();
                break;
            case SELECT_CARD_TO_STEAL:
                builder.enableEndTurnButton();
                break;
            case SELECT_ROBBER_TILE:
                builder.enableEndTurnButton();
                break;
            case PLACE_ROAD:
                updateBuildRoadButtonState();
                break;
            case PLACE_VILLAGE:
                updateBuildVillageButtonState();
                break;
            case DICE_ROLL:
                builder.enableRollDiceButton();
                break;
            case IDLE:
                builder.disableAllButtons();
                break;
        }
    }

    private PlayerController getPlayerController() {
        return playerControllerProperty.getValue();
    }

    private PlayerState getPlayerState() {
        return getPlayerController().getPlayerState();
    }

    private Player getPlayer() {
        return getPlayerController().getPlayer();
    }

    private void drawIntersections() {
        getHexGridController().drawIntersections();
    }

    private void removeAllHighlights() {
        getHexGridController().getEdgeControllers().forEach(ec -> ec.unhighlight());
        getHexGridController().getIntersectionControllers().forEach(ic -> ic.unhighlight());
    }

    private HexGridController getHexGridController() {
        return gameBoardController.getHexGridController();
    }

    private PlayerObjective getPlayerObjective() {
        return getPlayerController().getPlayerObjectiveProperty().getValue();
    }

    @DoNotTouch
    private Consumer<ActionEvent> actionWrapper(Consumer<ActionEvent> handler, boolean abortable) {
        return e -> {
            removeAllHighlights();
            drawIntersections();
            if (abortable) {
                builder.disableAllButtons();
                builder.enableAbortButton();
            }

            handler.accept(e);
            gameBoardController.updatePlayerInformation(getPlayer());
        };
    }

    @DoNotTouch
    private Consumer<MouseEvent> buildActionWrapper(Consumer<MouseEvent> handler) {
        return e -> {
            handler.accept(e);

            removeAllHighlights();
            if (getPlayerController() != null) {
                enableButtonBasedOnObjective(getPlayerObjective());
                gameBoardController.updatePlayerInformation(getPlayer());
            }
        };
    }

    private void updateBuildVillageButtonState() {
        if (getPlayerObjective().getAllowedActions().contains(BuildVillageAction.class)
                && getPlayerState().buildableVillageIntersections().size() > 0) {
            builder.enableBuildVillageButton();
            return;
        }
        builder.disableBuildVillageButton();
    }

    @StudentImplementationRequired
    private void buildVillageButtonAction(ActionEvent event) {
        getPlayerState().buildableVillageIntersections().stream()
                .map(intersection -> getHexGridController().getIntersectionControllersMap().get(intersection))
                .forEach(ic -> ic.highlight(buildActionWrapper(e -> {
                    getPlayerController().triggerAction(new BuildVillageAction(ic.getIntersection()));
                    drawIntersections();
                    enableButtonBasedOnObjective(getPlayerObjective());
                })));
    }

    private void updateUpgradeVillageButtonState() {
        if (getPlayerObjective().getAllowedActions().contains(UpgradeVillageAction.class)
                && getPlayerState().upgradebleVillageIntersections().size() > 0) {
            builder.enableUpgradeVillageButton();
            return;
        }
        builder.disableUpgradeVillageButton();
    }

    @StudentImplementationRequired
    private void upgradeVillageButtonAction(ActionEvent event) {
        getPlayerState().upgradebleVillageIntersections().stream()
                .map(intersection -> getHexGridController().getIntersectionControllersMap().get(intersection))
                .forEach(ic -> ic.highlight(buildActionWrapper(e -> {
                    getPlayerController().triggerAction(new UpgradeVillageAction(ic.getIntersection()));
                    drawIntersections();
                    enableButtonBasedOnObjective(getPlayerObjective());
                })));
    }

    private void updateBuildRoadButtonState() {
        if (getPlayerObjective().getAllowedActions().contains(BuildRoadAction.class)
                && getPlayerState().buildableRoadEdges().size() > 0) {
            builder.enableBuildRoadButton();
            return;
        }
        builder.disableBuildRoadButton();
    }

    @StudentImplementationRequired
    private void buildRoadButtonAction(ActionEvent event) {
        getPlayerState().buildableRoadEdges().stream()
                .map(edge -> getHexGridController().getEdgeControllersMap().get(edge))
                .forEach(ec -> ec.highlight(buildActionWrapper(e -> {
                    getPlayerController().triggerAction(new BuildRoadAction(ec.getEdge()));
                    drawRoads();
                    enableButtonBasedOnObjective(getPlayerObjective());
                })));
    }

    private void drawRoads() {
        getHexGridController().drawRoads();
    }

    private void buyDevelopmentCardButtonAction(ActionEvent event) {
        System.out.println("Buying development card");
    }

    private void endTurnButtonAction(ActionEvent event) {
        getPlayerController().triggerAction(new EndTurnAction());
    }

    private void rollDiceButtonAction(ActionEvent event) {
        getPlayerController().triggerAction(new RollDiceAction());
    }

    private void tradeButtonAction(ActionEvent event) {
        System.out.println("Trading");
    }

    private void abortButtonAction(ActionEvent event) {
        removeAllHighlights();
        enableButtonBasedOnObjective(getPlayerObjective());
        builder.disableAbortButton();
    }

    @Override
    public Builder<Region> getBuilder() {
        return builder;
    }

    @Override
    @DoNotTouch
    public Region buildView() {
        Region view = builder.build();
        playerControllerProperty.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (oldValue != null) {
                    oldValue.getPlayerObjectiveProperty().removeListener(playerObjectiveListener);
                }
                if (newValue == null) {
                    builder.disableAllButtons();
                    return;
                }

                attachObjectiveListener(newValue);
                if (newValue.getPlayerObjectiveProperty().getValue() != null) {
                    enableButtonBasedOnObjective(newValue.getPlayerObjectiveProperty().getValue());
                }
            });
        });
        if (getPlayerController() != null) {
            attachObjectiveListener(getPlayerController());
            enableButtonBasedOnObjective(getPlayerObjective());
        } else {
            builder.disableAllButtons();
        }
        return view;
    }

    private void attachObjectiveListener(PlayerController value) {
        value.getPlayerObjectiveProperty().removeListener(playerObjectiveListener);
        value.getPlayerObjectiveProperty()
                .addListener(playerObjectiveListener);
    }
}
