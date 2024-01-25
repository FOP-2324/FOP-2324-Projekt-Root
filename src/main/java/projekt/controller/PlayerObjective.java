package projekt.controller;

import projekt.controller.actions.BuildVillageAction;
import projekt.controller.actions.PlayerAction;
import projekt.controller.actions.RollDiceAction;

import java.util.Set;

public enum PlayerObjective {
    DROP_HALF_CARDS(Set.of()),
    SELECT_CARD_TO_STEAL(Set.of()),
    SELECT_ROBBER_TILE(Set.of()),
    REGULAR_TURN(Set.of(BuildVillageAction.class)),
    PLACE_TWO_VILLAGES(Set.of()),
    PLACE_TWO_ROADS(Set.of()),
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
