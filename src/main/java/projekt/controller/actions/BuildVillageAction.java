package projekt.controller.actions;

import projekt.controller.PlayerController;
import projekt.model.Intersection;

public class BuildVillageAction implements PlayerAction {

    private final Intersection intersection;

    public BuildVillageAction(Intersection intersection) {
        this.intersection = intersection;
    }

    public Intersection getSlot() {
        return intersection;
    }

    @Override
    public void execute(PlayerController pc) throws IllegalActionException {
        pc.buildVillage(intersection);
    }
}
