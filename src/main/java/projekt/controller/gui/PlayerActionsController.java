package projekt.controller.gui;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Builder;
import javafx.util.Subscription;
import projekt.controller.PlayerController;
import projekt.controller.PlayerObjective;
import projekt.controller.actions.AcceptTradeAction;
import projekt.controller.actions.BuildRoadAction;
import projekt.controller.actions.BuildVillageAction;
import projekt.controller.actions.DropCardsAction;
import projekt.controller.actions.EndTurnAction;
import projekt.controller.actions.RollDiceAction;
import projekt.controller.actions.SelectRobberTileAction;
import projekt.controller.actions.StealCardAction;
import projekt.controller.actions.TradeAction;
import projekt.controller.actions.UpgradeVillageAction;
import projekt.model.Player;
import projekt.model.PlayerState;
import projekt.model.ResourceType;
import projekt.model.TradePayload;
import projekt.model.tiles.Tile;
import projekt.view.gameControls.AcceptTradeDialog;
import projekt.view.gameControls.DropCardsDialog;
import projekt.view.gameControls.PlayerActionsBuilder;
import projekt.view.gameControls.SelectCardToStealDialog;
import projekt.view.gameControls.TradeDialog;

public class PlayerActionsController implements Controller {
    private final PlayerActionsBuilder builder;
    private final GameBoardController gameBoardController;
    private final Property<PlayerController> playerControllerProperty = new SimpleObjectProperty<>();
    private final Property<PlayerObjective> playerObjectiveProperty = new SimpleObjectProperty<>(PlayerObjective.IDLE);
    private final Property<PlayerState> playerStateProperty = new SimpleObjectProperty<>();
    private Subscription playerObjectiveSubscription = Subscription.EMPTY;
    private Subscription playerStateSubscription = Subscription.EMPTY;

    @DoNotTouch
    public PlayerActionsController(final GameBoardController gameBoardController,
            final Property<PlayerController> playerControllerProperty) {
        this.playerControllerProperty.subscribe((oldValue, newValue) -> {
            Platform.runLater(() -> {
                playerObjectiveSubscription.unsubscribe();
                playerObjectiveSubscription = newValue.getPlayerObjectiveProperty().subscribe((oldObjective,
                        newObjective) -> Platform.runLater(() -> this.playerObjectiveProperty.setValue(newObjective)));
                this.playerObjectiveProperty.setValue(newValue.getPlayerObjectiveProperty().getValue());

                playerStateSubscription.unsubscribe();
                playerStateSubscription = newValue.getPlayerStateProperty().subscribe(
                        (oldState, newState) -> Platform.runLater(() -> this.playerStateProperty.setValue(newState)));
                this.playerStateProperty.setValue(newValue.getPlayerStateProperty().getValue());
            });
        });
        this.gameBoardController = gameBoardController;
        playerControllerProperty.subscribe((oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (newValue == null) {
                    return;
                }
                this.playerControllerProperty.setValue(newValue);
            });
        });
        Platform.runLater(() -> {
            this.playerControllerProperty.setValue(playerControllerProperty.getValue());
        });

        this.builder = new PlayerActionsBuilder(actionWrapper(this::buildVillageButtonAction, true),
                actionWrapper(this::upgradeVillageButtonAction, true),
                actionWrapper(this::buildRoadButtonAction, true),
                actionWrapper(this::buyDevelopmentCardButtonAction, false),
                actionWrapper(this::endTurnButtonAction, false),
                actionWrapper(this::rollDiceButtonAction, false),
                actionWrapper(this::tradeButtonAction, false),
                this::abortButtonAction);
    }

    private void enableButtonBasedOnObjective(final PlayerObjective objective) {
        System.out.println("objective: " + objective);
        removeAllHighlights();
        drawEdges();
        drawIntersections();
        gameBoardController.updatePlayerInformation(getPlayer());
        builder.disableAllButtons();
        if (objective == null) {
            System.out.println("I am confusion");
            return;
        }
        gameBoardController.updatePlayerInformation(getPlayer());
        switch (objective) {
            case REGULAR_TURN:
                updateBuildVillageButtonState();
                updateUpgradeVillageButtonState();
                updateBuildRoadButtonState();
                builder.enableTradeButton();
                builder.enableEndTurnButton();
                break;
            case DROP_HALF_CARDS:
                dropCardsAction(getPlayer().getResources().values().stream().mapToInt(Integer::intValue).sum() / 2);
                break;
            case SELECT_CARD_TO_STEAL:
                selectCardToStealAction();
                break;
            case SELECT_ROBBER_TILE:
                getHexGridController().highlightTiles(this::selectRobberTileAction);
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
            case ACCEPT_TRADE:
                acceptTradeOffer();
                break;
        }
    }

    private PlayerController getPlayerController() {
        return playerControllerProperty.getValue();
    }

    private PlayerState getPlayerState() {
        return playerStateProperty.getValue();
    }

    private Player getPlayer() {
        return getPlayerController().getPlayer();
    }

    private void drawIntersections() {
        getHexGridController().drawIntersections();
    }

    private void removeAllHighlights() {
        getHexGridController().getEdgeControllers().forEach(EdgeController::unhighlight);
        getHexGridController().getIntersectionControllers().forEach(IntersectionController::unhighlight);
        getHexGridController().unhighlightTiles();
    }

    private HexGridController getHexGridController() {
        return gameBoardController.getHexGridController();
    }

    private PlayerObjective getPlayerObjective() {
        return playerObjectiveProperty.getValue();
    }

    @DoNotTouch
    private Consumer<ActionEvent> actionWrapper(final Consumer<ActionEvent> handler, final boolean abortable) {
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

    // Build actions

    @DoNotTouch
    private Consumer<MouseEvent> buildActionWrapper(final Consumer<MouseEvent> handler) {
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
    private void buildVillageButtonAction(final ActionEvent event) {
        getPlayerState().buildableVillageIntersections().stream()
                .map(intersection -> getHexGridController().getIntersectionControllersMap().get(intersection))
                .forEach(ic -> ic.highlight(buildActionWrapper(e -> {
                    getPlayerController().triggerAction(new BuildVillageAction(ic.getIntersection()));
                    drawIntersections();
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
    private void upgradeVillageButtonAction(final ActionEvent event) {
        getPlayerState().upgradebleVillageIntersections().stream()
                .map(intersection -> getHexGridController().getIntersectionControllersMap().get(intersection))
                .forEach(ic -> ic.highlight(buildActionWrapper(e -> {
                    getPlayerController().triggerAction(new UpgradeVillageAction(ic.getIntersection()));
                    drawIntersections();
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
    private void buildRoadButtonAction(final ActionEvent event) {
        getPlayerState().buildableRoadEdges().stream()
                .map(edge -> getHexGridController().getEdgeControllersMap().get(edge))
                .forEach(ec -> ec.highlight(buildActionWrapper(e -> {
                    getPlayerController().triggerAction(new BuildRoadAction(ec.getEdge()));
                    drawEdges();
                })));
    }

    private void drawEdges() {
        getHexGridController().drawEdges();
    }

    private void buyDevelopmentCardButtonAction(final ActionEvent event) {
        System.out.println("Buying development card");
    }

    private void endTurnButtonAction(final ActionEvent event) {
        getPlayerController().triggerAction(new EndTurnAction());
    }

    private void rollDiceButtonAction(final ActionEvent event) {
        getPlayerController().triggerAction(new RollDiceAction());
    }

    // Robber actions

    private void selectRobberTileAction(final Tile tile) {
        System.out.println(Thread.currentThread().getName());
        getHexGridController().unhighlightTiles();
        getPlayerController().triggerAction(new SelectRobberTileAction(tile.getPosition()));
        getHexGridController().drawTiles();
    }

    private void selectCardToStealAction() {
        if (getPlayerState().playersToStealFrom().isEmpty()) {
            getPlayerController().triggerAction(new EndTurnAction());
            return;
        }
        final SelectCardToStealDialog dialog = new SelectCardToStealDialog(getPlayerState().playersToStealFrom());
        dialog.showAndWait().ifPresentOrElse(
                result -> getPlayerController().triggerAction(new StealCardAction(result.getValue(), result.getKey())),
                () -> getPlayerController().triggerAction(new EndTurnAction()));
    }

    private void dropCardsAction(final int amountToDrop) {
        final DropCardsDialog dropCardsDialog = new DropCardsDialog(getPlayer().getResources(), amountToDrop,
                getPlayer());
        Optional<Map<ResourceType, Integer>> result = dropCardsDialog.showAndWait();
        while (result.isEmpty() || result.get() == null) {
            result = dropCardsDialog.showAndWait();
        }
        getPlayerController().triggerAction(new DropCardsAction(result.get()));
    }

    // Trade actions

    private void tradeButtonAction(final ActionEvent event) {
        System.out.println("Trading");
        final TradeDialog dialog = new TradeDialog(new TradePayload(null, null, false, getPlayer()));
        dialog.showAndWait().ifPresentOrElse(payload -> {
            getPlayerController().triggerAction(new TradeAction(payload));
        }, () -> System.out.println("Trade cancelled"));
        enableButtonBasedOnObjective(getPlayerObjective());
    }

    private void acceptTradeOffer() {
        final Optional<Boolean> optionalResult = new AcceptTradeDialog(getPlayerState().offeredTrade(), getPlayer())
                .showAndWait();
        optionalResult.ifPresent(result -> getPlayerController().triggerAction(new AcceptTradeAction(result)));
    }

    private void abortButtonAction(final ActionEvent event) {
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
        final Region view = builder.build();

        playerObjectiveProperty.subscribe((oldValue, newValue) -> enableButtonBasedOnObjective(newValue));
        builder.disableAllButtons();
        if (getPlayerController() != null) {
            enableButtonBasedOnObjective(getPlayerObjective());
        }
        return view;
    }
}
