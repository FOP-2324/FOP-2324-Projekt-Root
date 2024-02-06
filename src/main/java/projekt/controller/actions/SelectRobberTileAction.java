package projekt.controller.actions;

import projekt.controller.PlayerController;
import projekt.model.TilePosition;

public record SelectRobberTileAction(TilePosition tilePosition) implements PlayerAction {
    @Override
    public void execute(PlayerController pc) throws IllegalActionException {
        pc.setRobberPosition(tilePosition);
    }

}
