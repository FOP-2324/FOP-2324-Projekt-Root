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
import projekt.controller.actions.BuyDevelopmentCardAction;
import projekt.controller.actions.DropCardsAction;
import projekt.controller.actions.EndTurnAction;
import projekt.controller.actions.PlayDevelopmentCardAction;
import projekt.controller.actions.RollDiceAction;
import projekt.controller.actions.SelectCardsAction;
import projekt.controller.actions.SelectRobberTileAction;
import projekt.controller.actions.StealCardAction;
import projekt.controller.actions.TradeAction;
import projekt.controller.actions.UpgradeVillageAction;
import projekt.model.DevelopmentCardType;
import projekt.model.Player;
import projekt.model.PlayerState;
import projekt.model.ResourceType;
import projekt.model.TradePayload;
import projekt.model.tiles.Tile;
import projekt.view.gameControls.AcceptTradeDialog;
import projekt.view.gameControls.DropCardsDialog;
import projekt.view.gameControls.PlayerActionsBuilder;
import projekt.view.gameControls.SelectCardToStealDialog;
import projekt.view.gameControls.SelectResourceDialog;
import projekt.view.gameControls.TradeDialog;
import projekt.view.gameControls.UseDevelopmentCardDialog;

/**
 * This class is responsible for handling all player actions performed through
 * the UI. It ensures that the correct buttons are enabled and disabled based on
 * the current player objective and state.
 * It also ensures that the correct actions are triggered when a button is
 * clicked and that the user is prompted when a action requires user input.
 * Additionally it triggers the respective actions based on the user input.
 *
 * <b>Do not touch any of the given attributes these are constructed in a way to
 * ensure thread safety.</b>
 */
public class PlayerActionsController implements Controller {
    private final PlayerActionsBuilder builder;
    private final GameBoardController gameBoardController;
    private final Property<PlayerController> playerControllerProperty = new SimpleObjectProperty<>();
    private final Property<PlayerObjective> playerObjectiveProperty = new SimpleObjectProperty<>(PlayerObjective.IDLE);
    private final Property<PlayerState> playerStateProperty = new SimpleObjectProperty<>();
    private Subscription playerObjectiveSubscription = Subscription.EMPTY;
    private Subscription playerStateSubscription = Subscription.EMPTY;

    /**
     * Creates a new PlayerActionsController.
     * It attaches listeners to populate the playerController, playerState and
     * playerObjective properties. This is necessary to ensure these properties are
     * always on the correct thread.
     * Additionally the PlayerActionsBuilder is created with all necessary event
     * handlers.
     *
     * <b>Do not touch this constructor.</b>
     *
     * @param gameBoardController      the game board controller
     * @param playerControllerProperty the property that contains the player
     *                                 controller that is currently active
     */
    @DoNotTouch
    public PlayerActionsController(final GameBoardController gameBoardController,
            final Property<PlayerController> playerControllerProperty) {
        this.playerControllerProperty.subscribe((oldValue, newValue) -> {
            Platform.runLater(() -> {
                playerStateSubscription.unsubscribe();
                playerStateSubscription = newValue.getPlayerStateProperty().subscribe(
                        (oldState, newState) -> Platform.runLater(() -> this.playerStateProperty.setValue(newState)));
                this.playerStateProperty.setValue(newValue.getPlayerStateProperty().getValue());

                playerObjectiveSubscription.unsubscribe();
                playerObjectiveSubscription = newValue.getPlayerObjectiveProperty().subscribe((oldObjective,
                        newObjective) -> Platform.runLater(() -> this.playerObjectiveProperty.setValue(newObjective)));
                this.playerObjectiveProperty.setValue(newValue.getPlayerObjectiveProperty().getValue());
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
                actionWrapper(this::useDevelopmentCardButtonAction, false),
                actionWrapper(this::endTurnButtonAction, false),
                actionWrapper(this::rollDiceButtonAction, false),
                actionWrapper(this::tradeButtonAction, false),
                this::abortButtonAction);
    }

    /**
     * Updates the UI based on the given objective. This includes enabling and
     * disabling buttons and prompting the user if necessary.
     *
     * @param objective the objective to check
     */
    @StudentImplementationRequired
    private void updateUIBasedOnObjective(final PlayerObjective objective) {
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
        switch (objective) {
            case REGULAR_TURN:
                updateBuildVillageButtonState();
                updateUpgradeVillageButtonState();
                updateBuildRoadButtonState();
                updateBuyDevelopmentCardButtonState();
                updateUseDevelopmentCardButtonState();
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
            case SELECT_CARDS:
                selectResources(getPlayerState().cradsToSelect());
                break;
            default:
                break;
        }
    }

    /**
     * Returns the player controller that is currently active.
     * Please do not use this method to get the playerState or playerObjective.
     * Use the {@link PlayerActionsController#getPlayerState()} and
     * {@link PlayerActionsController#getPlayerObjective()} instead.
     *
     * @return the player controller that is currently active
     */
    @DoNotTouch
    private PlayerController getPlayerController() {
        return playerControllerProperty.getValue();
    }

    /**
     * Returns the player state of the player that is currently active.
     *
     * @return the player state of the player that is currently active
     */
    @DoNotTouch
    private PlayerState getPlayerState() {
        return playerStateProperty.getValue();
    }

    /**
     * Returns the player objective of the player that is currently active.
     *
     * @return the player objective of the player that is currently active
     */
    @DoNotTouch
    private PlayerObjective getPlayerObjective() {
        return playerObjectiveProperty.getValue();
    }

    /**
     * Returns the HexGridController of the game board.
     *
     * @return the HexGridController of the game board
     */
    @DoNotTouch
    private HexGridController getHexGridController() {
        return gameBoardController.getHexGridController();
    }

    /**
     * Returns the player that is currently active.
     *
     * @return the player that is currently active
     */
    @DoNotTouch
    private Player getPlayer() {
        return getPlayerController().getPlayer();
    }

    /**
     * ReDraws the intersections.
     */
    @DoNotTouch
    private void drawIntersections() {
        getHexGridController().drawIntersections();
    }

    /**
     * ReDraws the edges.
     */
    @DoNotTouch
    private void drawEdges() {
        getHexGridController().drawEdges();
    }

    /**
     * Removes all highlights from the game board.
     */
    @DoNotTouch
    private void removeAllHighlights() {
        getHexGridController().getEdgeControllers().forEach(EdgeController::unhighlight);
        getHexGridController().getIntersectionControllers().forEach(IntersectionController::unhighlight);
        getHexGridController().unhighlightTiles();
    }

    /**
     * Wraps a event handler (primarily button Actions) to ensure that all
     * highlights are removed, intersections are redrawn and all buttons except the
     * abort button (if abortable) are disabled.
     * This method is intended to be used when a button is clicked, to ensure a
     * common state before a action is performed.
     *
     * @param handler   the handler to wrap
     * @param abortable whether the action is abortable
     * @return the wrapped handler
     */
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
            // gameBoardController.updatePlayerInformation(getPlayer());
        };
    }

    // Build actions

    /**
     * Wraps a event handler to ensure that all highlights are removed, the correct
     * buttons are reenabled and the player information is up to date.
     *
     * This method is intended to be used when a action is triggered on the
     * player controller to ensure a common state after the action is performed.
     *
     * @param handler the handler to wrap
     * @return the wrapped handler
     */
    @DoNotTouch
    private Consumer<MouseEvent> buildActionWrapper(final Consumer<MouseEvent> handler) {
        return e -> {
            handler.accept(e);

            removeAllHighlights();
            if (getPlayerController() != null) {
                updateUIBasedOnObjective(getPlayerObjective());
                gameBoardController.updatePlayerInformation(getPlayer());
            }
        };
    }

    /**
     * Enables or disable the build village button based on the currently allowed
     * actions and if there are any buildable intersections.
     */
    @StudentImplementationRequired
    private void updateBuildVillageButtonState() {
        if (getPlayerObjective().getAllowedActions().contains(BuildVillageAction.class)
                && getPlayerState().buildableVillageIntersections().size() > 0) {
            builder.enableBuildVillageButton();
            return;
        }
        builder.disableBuildVillageButton();
    }

    /**
     * Attaches the logic to build a village to all buildable intersections and
     * highlights them.
     * When an intersection is selected, it triggers the BuildVillageAction.
     * The logic is wrapped in a buildActionWrapper to ensure a common state after a
     * village is built.
     *
     * This method is prepared to be used with a button.
     *
     * @param event the event that triggered the action
     */
    @StudentImplementationRequired
    private void buildVillageButtonAction(final ActionEvent event) {
        getPlayerState().buildableVillageIntersections().stream()
                .map(intersection -> getHexGridController().getIntersectionControllersMap().get(intersection))
                .forEach(ic -> ic.highlight(buildActionWrapper(e -> {
                    getPlayerController().triggerAction(new BuildVillageAction(ic.getIntersection()));
                    drawIntersections();
                })));
    }

    /**
     * Enables or disable the upgrade village button based on the currently allowed
     * actions and if there are any upgradeable villages.
     */
    @StudentImplementationRequired
    private void updateUpgradeVillageButtonState() {
        if (getPlayerObjective().getAllowedActions().contains(UpgradeVillageAction.class)
                && getPlayerState().upgradebleVillageIntersections().size() > 0) {
            builder.enableUpgradeVillageButton();
            return;
        }
        builder.disableUpgradeVillageButton();
    }

    /**
     * Attaches the logic to upgrade a village to all upgradeable intersections and
     * highlights them.
     * When an intersection is selected, it triggers the UpgradeVillageAction.
     * The logic is wrapped in a buildActionWrapper to ensure a common state after a
     * village is upgraded.
     *
     * This method is prepared to be used with a button.
     *
     * @param event the event that triggered the action
     */
    @StudentImplementationRequired
    private void upgradeVillageButtonAction(final ActionEvent event) {
        getPlayerState().upgradebleVillageIntersections().stream()
                .map(intersection -> getHexGridController().getIntersectionControllersMap().get(intersection))
                .forEach(ic -> ic.highlight(buildActionWrapper(e -> {
                    getPlayerController().triggerAction(new UpgradeVillageAction(ic.getIntersection()));
                    drawIntersections();
                })));
    }

    /**
     * Enables or disable the build road button based on the currently allowed
     * actions and if there are any edges to build on.
     */
    @StudentImplementationRequired
    private void updateBuildRoadButtonState() {
        if (getPlayerObjective().getAllowedActions().contains(BuildRoadAction.class)
                && getPlayerState().buildableRoadEdges().size() > 0) {
            builder.enableBuildRoadButton();
            return;
        }
        builder.disableBuildRoadButton();
    }

    /**
     * Attaches the logic to build a road to all buildable edges and highlights
     * them.
     * When an edge is selected, it triggers the BuildRoadAction.
     * The logic is wrapped in a buildActionWrapper to ensure a common state after a
     * road is built.
     *
     * This method is prepared to be used with a button.
     *
     * @param event the event that triggered the action
     */
    @StudentImplementationRequired
    private void buildRoadButtonAction(final ActionEvent event) {
        getPlayerState().buildableRoadEdges().stream()
                .map(edge -> getHexGridController().getEdgeControllersMap().get(edge))
                .forEach(ec -> ec.highlight(buildActionWrapper(e -> {
                    getPlayerController().triggerAction(new BuildRoadAction(ec.getEdge()));
                    drawEdges();
                })));
    }

    /**
     * The action that is triggered when the end turn button is clicked.
     *
     * @param event the event that triggered the action
     */
    private void endTurnButtonAction(final ActionEvent event) {
        getPlayerController().triggerAction(new EndTurnAction());
    }

    /**
     * The action that is triggered when the roll dice button is clicked.
     *
     * @param event the event that triggered the action
     */
    private void rollDiceButtonAction(final ActionEvent event) {
        getPlayerController().triggerAction(new RollDiceAction());
    }

    // Robber actions

    /**
     * Triggers the SelectRobberTileAction with the selected tile and unhighlights
     * all tiles. After the action is triggered, the tiles are redrawn.
     *
     * @param tile the tile that was clicked
     */
    @StudentImplementationRequired
    private void selectRobberTileAction(final Tile tile) {
        getHexGridController().unhighlightTiles();
        getPlayerController().triggerAction(new SelectRobberTileAction(tile.getPosition()));
        getHexGridController().drawTiles();
    }

    /**
     * Performs the action of selecting a card to steal from another player.
     * If there are no players to steal from, triggers the EndTurnAction.
     * Prompts the user to select a card to steal from a player and triggers the
     * StealCardAction with the selected card.
     * If no card is selected, triggers the EndTurnAction.
     */
    @StudentImplementationRequired
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

    /**
     * Performs the DropCardsAction and prompts the user to select the cards to
     * drop.
     * If the use cancels or selects an invalid amount of cards, the user is
     * prompted again.
     * Triggers the DropCardsAction with the selected cards.
     *
     * @param amountToDrop the amount of cards to drop
     */
    @StudentImplementationRequired
    private void dropCardsAction(final int amountToDrop) {
        final DropCardsDialog dropCardsDialog = new DropCardsDialog(getPlayer().getResources(), amountToDrop,
                getPlayer());
        Optional<Map<ResourceType, Integer>> result = dropCardsDialog.showAndWait();
        while (result.isEmpty() || result.get() == null) {
            result = dropCardsDialog.showAndWait();
        }
        getPlayerController().triggerAction(new DropCardsAction(result.get()));
    }

    private void selectResources(final int amountToSelect) {
        final SelectResourceDialog dialog = new SelectResourceDialog(amountToSelect, getPlayer());
        Optional<Map<ResourceType, Integer>> result = dialog.showAndWait();
        while (result.isEmpty() || result.get() == null) {
            result = dialog.showAndWait();
        }
        getPlayerController().triggerAction(new SelectCardsAction(result.get()));
    }

    // Development card actions

    private void updateBuyDevelopmentCardButtonState() {
        if (getPlayerObjective().getAllowedActions().contains(BuyDevelopmentCardAction.class)
                && getPlayerController().canBuyDevelopmentCard()) {
            builder.enableBuyDevelopmentCardButton();
            return;
        }
        builder.disableBuyDevelopmentCardButton();
    }

    /**
     * Performs the action of buying a development card.
     * Triggers the BuyDevelopmentCardAction.
     *
     * This method is prepared to be used with a button.
     *
     * @param event the event that triggered the action
     */
    private void buyDevelopmentCardButtonAction(final ActionEvent event) {
        getPlayerController().triggerAction(new BuyDevelopmentCardAction());
        updateUIBasedOnObjective(getPlayerObjective());
    }

    private void updateUseDevelopmentCardButtonState() {
        if (getPlayerObjective().getAllowedActions().contains(PlayDevelopmentCardAction.class)
                && getPlayer().getDevelopmentCards().entrySet().stream().anyMatch(
                        entry -> entry.getKey() != DevelopmentCardType.VICTORY_POINTS && entry.getValue() > 0)) {
            builder.enablePlayDevelopmentCardButton();
            return;
        }
        builder.disablePlayDevelopmentCardButton();
    }

    public void useDevelopmentCardButtonAction(final ActionEvent event) {
        final UseDevelopmentCardDialog dialog = new UseDevelopmentCardDialog(getPlayer());
        dialog.showAndWait()
                .ifPresent(result -> getPlayerController().triggerAction(new PlayDevelopmentCardAction(result)));
        updateUIBasedOnObjective(getPlayerObjective());
    }

    // Trade actions

    /**
     * Performs the trading action.
     * Prompts the user to select the cards to offer and the cards to request.
     * If the user cancels, the trade is cancelled.
     * Triggers the TradeAction with the selected cards.
     *
     * This method is prepared to be used with a button.
     *
     * @param event the event that triggered the action
     */
    private void tradeButtonAction(final ActionEvent event) {
        System.out.println("Trading");
        final TradeDialog dialog = new TradeDialog(new TradePayload(null, null, false, getPlayer()));
        dialog.showAndWait().ifPresentOrElse(payload -> {
            getPlayerController().triggerAction(new TradeAction(payload));
        }, () -> System.out.println("Trade cancelled"));
        updateUIBasedOnObjective(getPlayerObjective());
    }

    /**
     * Performs the action of accepting a trade offer.
     * Prompts the user to accept or decline the trade offer.
     * If the user cancels, the trade is declined.
     * Triggers the AcceptTradeAction with a boolean representing the players
     * decision.
     */
    private void acceptTradeOffer() {
        final Optional<Boolean> optionalResult = new AcceptTradeDialog(getPlayerState().offeredTrade(), getPlayer())
                .showAndWait();
        optionalResult.ifPresent(result -> getPlayerController().triggerAction(new AcceptTradeAction(result)));
    }

    /**
     * Aborts the current action by remove all highlights and reenabling the correct
     * buttons. Disables the abort button.
     *
     * This method is prepared to be used with a button.
     *
     * @param event the event that triggered the action
     */
    private void abortButtonAction(final ActionEvent event) {
        removeAllHighlights();
        updateUIBasedOnObjective(getPlayerObjective());
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

        playerObjectiveProperty.subscribe((oldValue, newValue) -> updateUIBasedOnObjective(newValue));
        playerStateProperty.subscribe((oldValue, newValue) -> updateUIBasedOnObjective(getPlayerObjective()));
        builder.disableAllButtons();
        if (getPlayerController() != null) {
            updateUIBasedOnObjective(getPlayerObjective());
        }
        return view;
    }
}
