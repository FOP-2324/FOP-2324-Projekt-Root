package projekt.controller.actions;

import projekt.controller.PlayerController;
import projekt.model.Player;
import projekt.model.ResourceType;

public record StealCardAction(ResourceType resourceToSteal, Player playerToStealFrom) implements PlayerAction {

    @Override
    public void execute(final PlayerController pc) throws IllegalActionException {
        pc.selectPlayerAndResourceToSteal(playerToStealFrom, resourceToSteal);
    }
}
