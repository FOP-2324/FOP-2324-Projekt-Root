package projekt.model;

import java.util.HashMap;
import java.util.Map;

public class Player {

    private int victoryPoints;
    private final Map<Resource, Integer> resources = new HashMap<>();

    public Player() {
        for (Resource resource : Resource.values()) {
            this.resources.put(resource, 0);
        }
    }

    public int getVictoryPoints() {
        return this.victoryPoints;
    }

    public void setVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }
}
