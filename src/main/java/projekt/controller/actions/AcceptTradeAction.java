package projekt.controller.actions;

import projekt.controller.PlayerController;

public record AcceptTradeAction(boolean accepted) implements PlayerAction {

    @Override
    public void execute(final PlayerController pc) throws IllegalActionException {
        pc.acceptTradeOffer(accepted);
    }
}
