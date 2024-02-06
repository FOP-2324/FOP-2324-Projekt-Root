package projekt.controller.actions;

import java.util.Map;

import projekt.controller.PlayerController;
import projekt.model.ResourceType;

public record SelectCardsAction(Map<ResourceType, Integer> selectedCards) implements PlayerAction {

    @Override
    public void execute(final PlayerController pc) throws IllegalActionException {
        pc.processSelectedResources(selectedCards);
    }

}
