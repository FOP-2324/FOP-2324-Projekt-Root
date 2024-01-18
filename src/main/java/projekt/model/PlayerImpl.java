package projekt.model;

import static projekt.Config.MAX_CITIES;
import static projekt.Config.MAX_ROADS;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.IntegerProperty;
import javafx.scene.paint.Color;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;
import projekt.model.buildings.Port;

public class PlayerImpl implements Player {
    private final HexGrid hexGrid;
    private final Color color;
    private String name;
    private IntegerProperty victoryPoints;
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
    public boolean removeResources(final Map<ResourceType, Integer> resources) {
        if (!hasResources(resources)) {
            return false;
        }
        for (final var entry : resources.entrySet()) {
            removeResource(entry.getKey(), entry.getValue());
        }
        return true;
    }

    @Override
    public int getTradeRatio(final ResourceType resourceType) {
        final var intersections = getHexGrid().getIntersections();
        return intersections.values().stream()
            .filter(intersection -> intersection.getPort() != null && intersection.getPort().resourceType() == resourceType)
            .filter(intersection -> intersection.playerHasConnectedRoad(this))
            .map(Intersection::getPort)
            .findAny().map(Port::ratio).orElse(4);
    }

    @Override
    @StudentImplementationRequired
    public boolean hasResources(final Map<ResourceType, Integer> resources) {
        return resources
            .entrySet()
            .stream()
            .noneMatch(e -> this.resources.getOrDefault(e.getKey(), 0) < e.getValue());
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

    public String getName() {
        return name;
    }

    @Override
    public IntegerProperty getVictoryPointsProperty() {
        return victoryPoints;
    }

    @Override
    public int getVictoryPoints() {
        return victoryPoints.get();
    }
}
