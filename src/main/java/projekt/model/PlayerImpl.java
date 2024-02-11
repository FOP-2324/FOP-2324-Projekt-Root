package projekt.model;

import static projekt.Config.MAX_CITIES;
import static projekt.Config.MAX_ROADS;
import static projekt.Config.MAX_VILLAGES;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import projekt.Config;
import projekt.model.buildings.Settlement;

/**
 * Default implementation of {@link Player}.
 */
public class PlayerImpl implements Player {
    private final HexGrid hexGrid;
    private final String name;
    private final int id;
    private final Color color;
    protected boolean ai;
    private final Map<ResourceType, Integer> resources = new HashMap<>();
    private final Map<DevelopmentCardType, Integer> developmentCards = new HashMap<>();
    private final Map<DevelopmentCardType, Integer> playedDevelopmentCards = new HashMap<>();

    private PlayerImpl(final HexGrid hexGrid, final Color color, final int id, final String name, final boolean ai) {
        this.hexGrid = hexGrid;
        this.color = color;
        this.id = id;
        this.name = name;
        this.ai = ai;
    }

    @Override
    public HexGrid getHexGrid() {
        return this.hexGrid;
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
    public Color getColor() {
        return this.color;
    }

    @Override
    public boolean isAi() {
        return this.ai;
    }

    @Override
    public int getVictoryPoints() {
        int buildingVictoryPoints = getSettlements().stream()
            .mapToInt(settlement -> settlement.type().resourceAmount)
            .sum();
        int developmentCardsVictoryPoints = developmentCards.getOrDefault(DevelopmentCardType.VICTORY_POINTS, 0);

        return buildingVictoryPoints + developmentCardsVictoryPoints;
    }

    @Override
    @StudentImplementationRequired
    public Map<ResourceType, Integer> getResources() {
        return Collections.unmodifiableMap(this.resources);
    }

    @Override
    @StudentImplementationRequired
    public void addResource(final ResourceType resourceType, final int amount) {
        this.resources.merge(resourceType, amount, Integer::sum);
    }

    @Override
    @StudentImplementationRequired
    public void addResources(final Map<ResourceType, Integer> resources) {
        resources.forEach(this::addResource);
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
    @StudentImplementationRequired
    public boolean removeResource(final ResourceType resourceType, final int amount) {
        if (!hasResources(Map.of(resourceType, amount))) {
            return false;
        }
        this.resources.merge(resourceType, -amount, Integer::sum);
        return true;
    }

    @Override
    @StudentImplementationRequired
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
    @StudentImplementationRequired
    public int getTradeRatio(final ResourceType resourceType) {
        return getHexGrid().getIntersections()
            .values()
            .stream()
            .filter(intersection -> intersection.getPort() != null
                && (intersection.getPort().resourceType() == null || intersection.getPort().resourceType().equals(resourceType)))
            .filter(intersection -> intersection.getSettlement() != null
                && intersection.getSettlement().owner().equals(this))
            .map(intersection -> intersection.getPort().ratio())
            .sorted()
            .findAny()
            .orElse(4);
    }

    @Override
    public int getRemainingRoads() {
        return MAX_ROADS - getRoads().size();
    }

    @Override
    public int getRemainingVillages() {
        return (int) (MAX_VILLAGES - getSettlements().stream()
            .filter(settlement -> settlement.type().equals(Settlement.Type.VILLAGE)).count());
    }

    @Override
    public int getRemainingCities() {
        return (int) (MAX_CITIES - getSettlements().stream()
                .filter(settlement -> settlement.type().equals(Settlement.Type.CITY)).count());
    }

    @Override
    public Map<DevelopmentCardType, Integer> getDevelopmentCards() {
        return Collections.unmodifiableMap(this.developmentCards);
    }

    @Override
    @StudentImplementationRequired
    public void addDevelopmentCard(final DevelopmentCardType developmentCardType) {
        this.developmentCards.merge(developmentCardType, 1, Integer::sum);
    }

    @Override
    @StudentImplementationRequired
    public boolean removeDevelopmentCard(final DevelopmentCardType developmentCardType) {
        if (this.developmentCards.getOrDefault(developmentCardType, 0) <= 0) {
            return false;
        }
        this.developmentCards.merge(developmentCardType, -1, Integer::sum);
        this.playedDevelopmentCards.merge(developmentCardType, 1, Integer::sum);
        return true;
    }

    @Override
    public int getTotalDevelopmentCards() {
        return this.developmentCards.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public int getKnightsPlayed() {
        return this.playedDevelopmentCards.getOrDefault(DevelopmentCardType.KNIGHT, 0);
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
