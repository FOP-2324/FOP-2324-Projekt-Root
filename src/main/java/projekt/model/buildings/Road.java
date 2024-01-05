package projekt.model.buildings;

import projekt.model.Intersection;
import projekt.model.PlayerImpl;

import java.util.Objects;

public record Road(Intersection nodeA, Intersection nodeB, PlayerImpl owner) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Road road = (Road) o;
        return Objects.equals(nodeA, road.nodeA) && Objects.equals(nodeB, road.nodeB) ||
                Objects.equals(nodeA, road.nodeB) && Objects.equals(nodeB, road.nodeA);
    }

    @Override
    public int hashCode() {
        return nodeA.hashCode() + nodeB.hashCode();
    }
}
