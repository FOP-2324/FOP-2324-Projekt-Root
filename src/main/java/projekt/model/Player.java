package projekt.model;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.scene.paint.Color;
import projekt.model.buildings.Edge;
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
     * Returns a property of the amount of victory points the player has.
     *
     * @return a property of the amount of victory points the player has.
     */
    IntegerProperty getVictoryPointsProperty();

    /**
     * Returns the amount of victory points the player has.
     *
     * @return the amount of victory points the player has.
     */
    int getVictoryPoints();

    /**
     * Returns true if the player has the given resources, false otherwise.
     *
     * @return true if the player has the given resources, false otherwise
     */
    boolean hasResources(Map<ResourceType, Integer> resources);

    /**
     * Adds the given amount of the given resource to the player.
     *
     * @param resourceType the ResourceType to add to
     * @param amount       the amount to add
     */
    void addResource(ResourceType resourceType, int amount);

    /**
     * Adds the given resources to the player.
     *
     * @param resources
     */
    void addResources(Map<ResourceType, Integer> resources);

    /**
     * Removes the given amount of the given resource from the player.
     *
     * @param resourceType the ResourceType to remove from
     * @param amount       the amount to remove
     */
    boolean removeResource(ResourceType resourceType, int amount);

    /**
     * Removes the given resources from the player.
     *
     * @param resources the resources to remove
     * @return true if the player had enough resources to remove, false otherwise
     */
    boolean removeResources(Map<ResourceType, Integer> resources);

    /**
     * Returns the ratio the player can trade the given resource for with the bank.
     *
     * @param resourceType the resource to trade
     * @return the ratio the player can trade the given resource for with the bank
     */
    int getTradeRatio(ResourceType resourceType);

    /**
     * Returns all roads the player currently has.
     *
     * @return all roads the player currently has
     */
    default Map<Set<TilePosition>, Edge> getRoads() {
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
                .filter(intersection -> intersection.getSettlement().owner().equals(this))
                .map(Intersection::getSettlement)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the amount of villages the player can still build.
     *
     * @return the amount of villages the player can still build
     */
    int getRemainingVillages();

    /**
     * Returns the amount of cities the player can still build.
     *
     * @return the amount of cities the player can still build
     */
    int getRemainingCities();

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

    /**
     * Returns the name of the player.
     *
     * @return the name of the player
     */
    String getName();

    /**
     * Returns the Player ID, aka the Index of the Player, starting with 1
     *
     * @return the Player ID
     */
    int getID();

    /**
     * Returns true if the player is an AI, false otherwise.
     *
     * @return true if the player is an AI, false otherwise
     */
    default boolean isAi() {
        return false;
    }
}
