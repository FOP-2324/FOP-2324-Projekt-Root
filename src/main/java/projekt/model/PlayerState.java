package projekt.model;

import java.util.Set;

import projekt.model.buildings.Edge;

public record PlayerState(
        Set<Intersection> buildableVillageIntersections,
        Set<Intersection> upgradebleVillageIntersections,
        Set<Edge> buildableRoadEdges) {
}
