package projekt.controller.actions;

import projekt.controller.PlayerController;

public class RollDiceAction implements PlayerAction {

    @Override
    public void execute(final PlayerController pc) throws IllegalActionException {
        pc.rollDice();
    }
}
