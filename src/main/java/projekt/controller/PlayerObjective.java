package projekt.controller;

import java.util.Set;

import org.tudalgo.algoutils.student.annotation.StudentCreationRequired;

import projekt.controller.actions.BuildRoadAction;
import projekt.controller.actions.BuildVillageAction;
import projekt.controller.actions.DropCardsAction;
import projekt.controller.actions.EndTurnAction;
import projekt.controller.actions.PlayerAction;
import projekt.controller.actions.RollDiceAction;
import projekt.controller.actions.SelectRobberTileAction;
import projekt.controller.actions.StealCardAction;
import projekt.controller.actions.TradeAction;
import projekt.controller.actions.UpgradeVillageAction;

/**
 * This enum represents the different objectives a player can have and what
 * actions are allowed when the player has this
 * objective.
 */
@StudentCreationRequired
public enum PlayerObjective {
    DROP_HALF_CARDS(Set.of(DropCardsAction.class)),
    SELECT_CARD_TO_STEAL(Set.of(StealCardAction.class, EndTurnAction.class)),
    SELECT_ROBBER_TILE(Set.of(SelectRobberTileAction.class)),
    REGULAR_TURN(Set.of(BuildRoadAction.class, EndTurnAction.class, BuildVillageAction.class,
            UpgradeVillageAction.class, TradeAction.class)),
    PLACE_VILLAGE(Set.of(BuildVillageAction.class)),
    PLACE_ROAD(Set.of(BuildRoadAction.class)),
    DICE_ROLL(Set.of(RollDiceAction.class)),
    IDLE(Set.of());

    final Set<Class<? extends PlayerAction>> allowedActions;

    PlayerObjective(final Set<Class<? extends PlayerAction>> allowedActions) {
        this.allowedActions = allowedActions;
    }

    /**
     * Returns the actions that are allowed when the player has this objective.
     *
     * @return the actions that are allowed when the player has this objective
     */
    public Set<Class<? extends PlayerAction>> getAllowedActions() {
        return allowedActions;
    }
}
