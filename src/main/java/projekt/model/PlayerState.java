package projekt.model;

import java.util.List;
import java.util.Set;

import projekt.model.buildings.Edge;

public record PlayerState(
        Set<Intersection> buildableVillageIntersections,
        Set<Intersection> upgradebleVillageIntersections,
        Set<Edge> buildableRoadEdges,
        List<Player> playersToStealFrom,
        TradePayload offeredTrade,
        int cradsToSelect) {
}
