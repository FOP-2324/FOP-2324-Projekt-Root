package projekt.controller.actions;

import projekt.controller.PlayerController;
import projekt.model.DevelopmentCardType;

public record PlayDevelopmentCardAction(DevelopmentCardType developmentCard) implements PlayerAction {

    @Override
    public void execute(final PlayerController pc) throws IllegalActionException {
        pc.playDevelopmentCard(developmentCard);
    }
}
