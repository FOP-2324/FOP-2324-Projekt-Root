package projekt.model;

import java.util.HashMap;
import java.util.Map;

public class Player {

    protected int victoryPoints;
    protected final Map<ResourceType, Integer> resources = new HashMap<>();

    public Player() {
        for (ResourceType resourceType : ResourceType.values()) {
            this.resources.put(resourceType, 0);
        }
    }

    public int getVictoryPoints() {
        return this.victoryPoints;
    }

    public void setVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }

    public int getAmount(ResourceType resourceType) {
        return resources.get(resourceType);
    }
}
