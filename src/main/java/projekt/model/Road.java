package projekt.model;

import java.util.Objects;

public class Road {

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

    public Player ownedBy() {
        return ownedBy;
    }

    public void setOwner(Player ownedBy) {
        this.ownedBy = ownedBy;
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
