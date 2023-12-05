package projekt.model.buildings;

import projekt.model.Intersection;
import projekt.model.Player;

import java.util.Objects;

public class Road implements Structure {

    private final Intersection nodeA, nodeB;
    private Player ownedBy;

    public Road(Intersection nodeA, Intersection nodeB) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }

    public Intersection getNodeA() {
        return nodeA;
    }

    public Intersection getNodeB() {
        return nodeB;
    }

    @Override
    public Player getOwner() {
        return ownedBy;
    }

    public void setOwner(Player player) {
        this.ownedBy = player;
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
