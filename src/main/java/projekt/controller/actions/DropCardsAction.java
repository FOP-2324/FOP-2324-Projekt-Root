package projekt.controller.actions;

import java.util.Map;

import projekt.controller.PlayerController;
import projekt.model.ResourceType;

public record DropCardsAction(Map<ResourceType, Integer> cardsToDrop) implements PlayerAction {
    @Override
    public void execute(PlayerController pc) throws IllegalActionException {
        pc.selectResourcesToDrop(cardsToDrop);
    }

}
