package projekt.model;

import static projekt.Config.MAX_CITIES;
import static projekt.Config.MAX_ROADS;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;

public class PlayerImpl implements Player {
    private final HexGrid hexGrid;
    private final Color color;
    private final Map<ResourceType, Integer> resources = new HashMap<>();
    private final Map<DevelopmentCardType, Integer> developmentCards = new HashMap<>();

    public PlayerImpl(final HexGrid hexGrid, final Color color) {
        this.hexGrid = hexGrid;
        this.color = color;
    }

    @Override
    public HexGrid getHexGrid() {
        return hexGrid;
    }

    @Override
    public Map<ResourceType, Integer> getResources() {
        return Collections.unmodifiableMap(resources);
    }

    @Override
    public void addResource(final ResourceType resourceType, final int amount) {
        resources.put(resourceType, resources.getOrDefault(resourceType, 0) + amount);
    }

    @Override
    public boolean removeResource(final ResourceType resourceType, final int amount) {
        if (resources.getOrDefault(resourceType, 0) <= amount) {
            return false;
        }
        resources.put(resourceType, resources.getOrDefault(resourceType, 0) - amount);
        return true;
    }

    @Override
    public int getRemainingRoads() {
        return MAX_ROADS - getRoads().size();
    }

    @Override
    public int getRemainingSettlements() {
        return MAX_CITIES - getSettlements().size();
    }

    @Override
    public Map<DevelopmentCardType, Integer> getDevelopmentCards() {
        return Collections.unmodifiableMap(developmentCards);
    }

    @Override
    public void addDevelopmentCard(final DevelopmentCardType developmentCardType) {
        developmentCards.put(developmentCardType, developmentCards.getOrDefault(developmentCardType, 0) + 1);
    }

    @Override
    public boolean removeDevelopmentCard(final DevelopmentCardType developmentCardType) {
        if (developmentCards.getOrDefault(developmentCardType, 0) <= 0) {
            return false;
        }
        developmentCards.put(developmentCardType, developmentCards.getOrDefault(developmentCardType, 0) - 1);
        return true;
    }

    @Override
    public int getTotalDevelopmentCards() {
        return developmentCards.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public int getKnightsPlayed() {
        return developmentCards.getOrDefault(DevelopmentCardType.KNIGHT, 0);
    }

    @Override
    public Color getColor() {
        return color;
    }

}
