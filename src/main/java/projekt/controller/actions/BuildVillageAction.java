package projekt.controller.actions;

import projekt.controller.PlayerController;
import projekt.model.Intersection;

public record BuildVillageAction(Intersection intersection) implements PlayerAction {

    @Override
    public void execute(final PlayerController pc) throws IllegalActionException {
        pc.buildVillage(intersection);
    }
}
