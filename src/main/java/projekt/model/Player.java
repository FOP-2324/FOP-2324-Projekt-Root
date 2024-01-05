package projekt.model;

import java.util.List;
import java.util.Map;

import projekt.model.buildings.Road;
import projekt.model.buildings.Settlement;

public interface Player {
    /**
     * Returns a map of all resources the player currently has and how many of each.
     *
     * @return a map of all resources the player currently has and how many of each.
     */
    public Map<ResourceType, Integer> getResources();

    public List<Road> getRoads();

    public List<Map<DevelopmentCardType, Integer>> getDevelopmentCards();
}
