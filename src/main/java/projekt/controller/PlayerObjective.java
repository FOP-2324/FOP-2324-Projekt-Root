package projekt.controller;

import java.util.Set;

import projekt.controller.actions.BuildRoadAction;
import projekt.controller.actions.BuildVillageAction;
import projekt.controller.actions.EndTurnAction;
import projekt.controller.actions.PlayerAction;
import projekt.controller.actions.RollDiceAction;
import projekt.controller.actions.UpgradeVillageAction;

public enum PlayerObjective {
    DROP_HALF_CARDS(Set.of(EndTurnAction.class)),
    SELECT_CARD_TO_STEAL(Set.of(EndTurnAction.class)),
    SELECT_ROBBER_TILE(Set.of(EndTurnAction.class)),
    REGULAR_TURN(
            Set.of(BuildRoadAction.class, EndTurnAction.class, BuildVillageAction.class, UpgradeVillageAction.class)),
    PLACE_VILLAGE(Set.of(BuildVillageAction.class)),
    PLACE_ROAD(Set.of(BuildRoadAction.class)),
    DICE_ROLL(Set.of(RollDiceAction.class)),
    IDLE(Set.of());

    final Set<Class<? extends PlayerAction>> allowedActions;

    PlayerObjective(final Set<Class<? extends PlayerAction>> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public Set<Class<? extends PlayerAction>> getAllowedActions() {
        return allowedActions;
    }
}
