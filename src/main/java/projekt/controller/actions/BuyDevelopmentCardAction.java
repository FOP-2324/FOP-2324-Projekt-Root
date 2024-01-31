package projekt.controller.actions;

import projekt.controller.PlayerController;
import projekt.model.DevelopmentCardType;

public record BuyDevelopmentCardAction(DevelopmentCardType type) implements PlayerAction {
    @Override
    public void execute(final PlayerController pc) throws IllegalActionException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }
}
