package projekt.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.Property;
import projekt.controller.actions.AcceptTradeAction;
import projekt.controller.actions.BuildRoadAction;
import projekt.controller.actions.BuildVillageAction;
import projekt.controller.actions.EndTurnAction;
import projekt.controller.actions.PlayerAction;
import projekt.controller.actions.RollDiceAction;
import projekt.controller.actions.SelectCardsAction;
import projekt.controller.actions.SelectRobberTileAction;
import projekt.controller.actions.StealCardAction;
import projekt.model.GameState;
import projekt.model.HexGrid;
import projekt.model.Player;
import projekt.model.ResourceType;

public class AiController {
    private final PlayerController playerController;
    private final HexGrid hexGrid;
    private final GameState gameState;
    private final Property<PlayerController> activePlayerController;

    public AiController(final PlayerController playerController, final HexGrid hexGrid, final GameState gameState,
            final Property<PlayerController> activePlayerController) {
        this.playerController = playerController;
        this.hexGrid = hexGrid;
        this.gameState = gameState;
        this.activePlayerController = activePlayerController;
        playerController.getPlayerObjectiveProperty().subscribe(this::executeActionBasedOnObjective);
    }

    private void executeActionBasedOnObjective(final PlayerObjective objective) {
        final Set<Class<? extends PlayerAction>> actions = objective.getAllowedActions();

        if (actions.contains(RollDiceAction.class)) {
            playerController.triggerAction(new RollDiceAction());
        }
        if (actions.contains(BuildVillageAction.class)) {
            buildVillage();
        }
        if (actions.contains(BuildRoadAction.class)) {
            buildRoad();
        }
        if (actions.contains(SelectCardsAction.class)) {
            selectCards();
        }
        if (actions.contains(SelectRobberTileAction.class)) {
            selectRobberTileAction();
        }
        if (actions.contains(AcceptTradeAction.class)) {
            playerController.triggerAction(new AcceptTradeAction(true));
        }
        if (actions.contains(StealCardAction.class)) {
            stealCardAction();
        }
        if (actions.contains(EndTurnAction.class)) {
            playerController.triggerAction(new EndTurnAction());
        }
    }

    private void buildVillage() {
        playerController.getPlayerState().buildableVillageIntersections().stream().findAny().ifPresent(intersection -> {
            playerController.triggerAction(new BuildVillageAction(intersection));
        });
    }

    private void buildRoad() {
        playerController.getPlayerState().buildableRoadEdges().stream().findAny().ifPresent(edge -> {
            playerController.triggerAction(new BuildRoadAction(edge));
        });
    }

    private void selectCards() {
        final Map<ResourceType, Integer> selectedCards = new HashMap<>();
        for (int i = 0; i < playerController.getPlayerState().cradsToSelect(); i++) {
            playerController.getPlayer().getResources().entrySet().stream()
                    .filter(entry -> entry.getValue() - selectedCards.getOrDefault(entry.getKey(), 0) > 0).findAny()
                    .ifPresent(entry -> {
                        selectedCards.put(entry.getKey(), selectedCards.getOrDefault(entry.getKey(), 0) + 1);
                    });
        }
        playerController.triggerAction(new SelectCardsAction(selectedCards));
    }

    private void selectRobberTileAction() {
        playerController.triggerAction(
                new SelectRobberTileAction(hexGrid.getTiles().values().stream().findAny().get().getPosition()));
    }

    private void stealCardAction() {
        final Player playerToStealFrom = playerController.getPlayerState().playersToStealFrom().stream().findAny()
                .orElse(null);
        if (playerToStealFrom == null) {
            return;
        }
        final ResourceType resourceToSteal = playerToStealFrom.getResources().entrySet().stream()
                .filter(entry -> entry.getValue() > 0).findAny().get().getKey();
        playerController.triggerAction(new StealCardAction(resourceToSteal, playerToStealFrom));
    }
}
