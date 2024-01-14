package projekt.model;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.scene.paint.Color;
import projekt.model.buildings.Road;
import projekt.model.buildings.Settlement;

public interface Player {
    /**
     * Returns the hexGrid instance
     *
     * @return the hexGrid instance
     */
    HexGrid getHexGrid();

    /**
     * Returns a map of all resources the player currently has and how many of each.
     *
     * @return a map of all resources the player currently has and how many of each.
     */
    Map<ResourceType, Integer> getResources();

    /**
     * Adds the given amount of the given resource to the player.
     *
     * @param resourceType the ResourceType to add to
     * @param amount       the amount to add
     */
    void addResource(ResourceType resourceType, int amount);

    /**
     * Removes the given amount of the given resource from the player.
     *
     * @param resourceType the ResourceType to remove from
     * @param amount       the amount to remove
     */
    boolean removeResource(ResourceType resourceType, int amount);

    /**
     * Returns all roads the player currently has.
     *
     * @return all roads the player currently has
     */
    default Map<Set<TilePosition>, Road> getRoads() {
        return getHexGrid().getRoads(this);
    }

    /**
     * Returns the amount of roads the player can still build.
     *
     * @return the amount of roads the player can still build
     */
    int getRemainingRoads();

    /**
     * Returns all settlements the player currently has.
     *
     * @return all settlements the player currently has
     */
    default Set<Settlement> getSettlements() {
        return getHexGrid().getIntersections().values().stream()
                .filter(intersection -> intersection.getSettlement() != null)
                .filter(intersection -> intersection.getSettlement().owner() == this)
                .map(intersection -> intersection.getSettlement())
                .collect(Collectors.toSet());
    }

    /**
     * Returns the amount of settlements the player can still build.
     *
     * @return the amount of settlements the player can still build
     */
    int getRemainingSettlements();

    /**
     * Returns a map of all development cards the player currently has and how many.
     *
     * @return a map of all development cards the player currently has and how many.
     */
    Map<DevelopmentCardType, Integer> getDevelopmentCards();

    /**
     * Adds the given development card to the player.
     *
     * @param developmentCardType the development card to add
     */
    void addDevelopmentCard(DevelopmentCardType developmentCardType);

    /**
     * Removes the given development card from the player.
     *
     * @param developmentCardType the development card to remove
     */
    boolean removeDevelopmentCard(DevelopmentCardType developmentCardType);

    /**
     * Returns the total amount of development cards the player has.
     *
     * @return the total amount of development cards the player has
     */
    int getTotalDevelopmentCards();

    /**
     * Returns the amount of knights the player has played.
     *
     * @return the amount of knights the player has played
     */
    int getKnightsPlayed();

    /**
     * Returns the color of the player.
     *
     * @return the color of the player
     */
    Color getColor();
}
