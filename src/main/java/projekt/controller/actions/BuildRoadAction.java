package projekt.controller.actions;

import projekt.controller.PlayerController;
import projekt.model.buildings.Edge;

public record BuildRoadAction(Edge edge) implements PlayerAction {

    @Override
    public void execute(final PlayerController pc) throws IllegalActionException {
        pc.buildRoad(edge.position1(), edge.position2());
    }
}
