package projekt.model.buildings;

import projekt.model.Intersection;
import projekt.model.Player;

import java.util.Objects;

public record Road(Intersection nodeA, Intersection nodeB, Player owner) implements Structure {

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public Structure.Type getType() {
        return Structure.Type.ROAD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Road road = (Road) o;
        if ((nodeA == road.nodeA && nodeB == road.nodeB) || (nodeA == road.nodeB && nodeB == road.nodeA)) return true;
        return Objects.equals(nodeA, road.nodeA) && Objects.equals(nodeB, road.nodeB);
    }

    @Override
    public int hashCode() {
        return nodeA.hashCode() + nodeB.hashCode();
    }
}
