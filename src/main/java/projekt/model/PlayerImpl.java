package projekt.model;

import static projekt.Config.MAX_CITIES;
import static projekt.Config.MAX_ROADS;
import static projekt.Config.MAX_VILLAGES;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;
import projekt.Config;
import projekt.model.buildings.Port;
import projekt.model.buildings.Settlement;

public class PlayerImpl implements Player {
    private final HexGrid hexGrid;
    private final Color color;
    private final int id;
    protected boolean ai;
    private final String name;
    private final IntegerProperty victoryPoints;
    private final Map<ResourceType, Integer> resources = new HashMap<>();
    private final Map<DevelopmentCardType, Integer> developmentCards = new HashMap<>();
    private final Map<DevelopmentCardType, Integer> playedDevelopmentCards = new HashMap<>();

    private PlayerImpl(final HexGrid hexGrid, final Color color, final int id, final String name, final boolean ai) {
        this.hexGrid = hexGrid;
        this.color = color;
        this.victoryPoints = new SimpleIntegerProperty(0);
        this.id = id;
        this.name = name;
        this.ai = ai;
    }

    @Override
    public HexGrid getHexGrid() {
        return this.hexGrid;
    }

    @Override
    public Map<ResourceType, Integer> getResources() {
        return Collections.unmodifiableMap(this.resources);
    }

    @Override
    @StudentImplementationRequired("H1.1")
    public void addResource(final ResourceType resourceType, final int amount) {
        this.resources.put(resourceType, this.resources.getOrDefault(resourceType, 0) + amount);
    }

    @Override
    @StudentImplementationRequired("H1.1")
    public void addResources(final Map<ResourceType, Integer> resources) {
        for (final var entry : resources.entrySet()) {
            addResource(entry.getKey(), entry.getValue());
        }
    }

    @Override
    @StudentImplementationRequired("H1.1")
    public boolean removeResource(final ResourceType resourceType, final int amount) {
        if (!hasResources(Map.of(resourceType, amount))) {
            return false;
        }
        this.resources.put(resourceType, this.resources.getOrDefault(resourceType, 0) - amount);
        return true;
    }

    @Override
    @StudentImplementationRequired("H1.1")
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
    @StudentImplementationRequired("H1.1")
    public int getTradeRatio(final ResourceType resourceType) {
        final var intersections = getHexGrid().getIntersections();
        return intersections.values().stream()
                .filter(intersection -> intersection.getPort() != null
                        && resourceType.equals(intersection.getPort().resourceType()))
                .filter(intersection -> intersection.getSettlement() != null
                        && intersection.getSettlement().owner().equals(this))
                .map(Intersection::getPort)
                .findAny().map(Port::ratio).orElse(4);
    }

    @Override
    @StudentImplementationRequired("H1.1")
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
    public int getRemainingCities() {
        return (int) (MAX_CITIES - getSettlements().stream()
                .filter(settlement -> settlement.type().equals(Settlement.Type.CITY)).count());
    }

    @Override
    public int getRemainingVillages() {
        return (int) (MAX_VILLAGES - getSettlements().stream()
                .filter(settlement -> settlement.type().equals(Settlement.Type.VILLAGE)).count());
    }

    @Override
    @StudentImplementationRequired("H1.2")
    public Map<DevelopmentCardType, Integer> getDevelopmentCards() {
        return Collections.unmodifiableMap(this.developmentCards);
    }

    @Override
    @StudentImplementationRequired("H1.2")
    public void addDevelopmentCard(final DevelopmentCardType developmentCardType) {
        this.developmentCards.put(developmentCardType, this.developmentCards.getOrDefault(developmentCardType, 0) + 1);
    }

    @Override
    @StudentImplementationRequired("H1.2")
    public boolean removeDevelopmentCard(final DevelopmentCardType developmentCardType) {
        if (this.developmentCards.getOrDefault(developmentCardType, 0) <= 0) {
            return false;
        }
        this.developmentCards.put(developmentCardType, this.developmentCards.getOrDefault(developmentCardType, 0) - 1);
        this.playedDevelopmentCards.put(developmentCardType,
                this.playedDevelopmentCards.getOrDefault(developmentCardType, 0) + 1);
        return true;
    }

    @Override
    @StudentImplementationRequired("H1.2")
    public int getTotalDevelopmentCards() {
        return this.developmentCards.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    @StudentImplementationRequired("H1.2")
    public int getKnightsPlayed() {
        return this.playedDevelopmentCards.getOrDefault(DevelopmentCardType.KNIGHT, 0);
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public IntegerProperty getVictoryPointsProperty() {
        return this.victoryPoints;
    }

    @Override
    public int getVictoryPoints() {
        return this.victoryPoints.get();
    }

    @Override
    public boolean isAi() {
        return this.ai;
    }

    public static class Builder {
        private int id;
        private Color color;
        private @Nullable String name;
        private final SimpleBooleanProperty ai = new SimpleBooleanProperty(false);

        public Builder(final int id) {
            this.id = id;
            color(null);
        }

        public Color getColor() {
            return this.color;
        }

        public Builder color(final Color playerColor) {
            this.color = playerColor == null
                    ? new Color(
                            Config.RANDOM.nextDouble(),
                            Config.RANDOM.nextDouble(),
                            Config.RANDOM.nextDouble(),
                            1)
                    : playerColor;
            return this;
        }

        public @Nullable String getName() {
            return this.name;
        }

        public Builder name(final @Nullable String playerName) {
            this.name = playerName;
            return this;
        }

        public String nameOrDefault() {
            return this.name == null ? String.format("Player%d", this.id) : this.name;
        }

        public Builder id(final int newId) {
            this.id = newId;
            return this;
        }

        public int getId() {
            return this.id;
        }

        public boolean isAi() {
            return this.ai.get();
        }

        public SimpleBooleanProperty aiProperty() {
            return this.ai;
        }

        public Builder ai(final boolean ai) {
            this.ai.set(ai);
            return this;
        }

        public Player build(final HexGrid grid) {
            return new PlayerImpl(grid, this.color, this.id, nameOrDefault(), this.ai.get());
        }
    }

    @Override
    public String toString() {
        return String.format("Player %d %s (%s)", getID(), getName(), getColor());
    }
}
