package projekt.model;

import java.util.Map;
import java.util.Set;

import javafx.scene.paint.Color;
import projekt.model.buildings.Road;
import projekt.model.buildings.Settlement;
import projekt.model.developmentCards.DevelopmentCardType;

public interface Player {
    /**
     * Returns a map of all resources the player currently has and how many of each.
     *
     * @return a map of all resources the player currently has and how many of each.
     */
    public Map<ResourceType, Integer> getResources();

    /**
     * Adds the given amount of the given resource to the player.
     *
     * @param resourceType the ResourceType to add to
     * @param amount       the amount to add
     */
    public void addResource(ResourceType resourceType, int amount);

    /**
     * Removes the given amount of the given resource from the player.
     *
     * @param resourceType the ResourceType to remove from
     * @param amount       the amount to remove
     */
    public void removeResource(ResourceType resourceType, int amount);

    /**
     * Returns all roads the player currently has.
     *
     * @return all roads the player currently has
     */
    public Set<Road> getRoads();

    /**
     * Adds the given road to the player.
     *
     * @param road the road to add
     */
    public void addRoad(Road road);

    /**
     * Returns the amount of roads the player can still build.
     *
     * @return the amount of roads the player can still build
     */
    public int getRemainingRoads();

    /**
     * Returns all settlements the player currently has.
     *
     * @return all settlements the player currently has
     */
    public Set<Settlement> getSettlements();

    /**
     * Adds the given settlement to the player.
     *
     * @param settlement the settlement to add
     */
    public void addSettlement(Settlement settlement);

    /**
     * Removes the given settlement from the player.
     *
     * @param settlement the settlement to remove
     */
    public void removeSettlement(Settlement settlement);

    /**
     * Returns the amount of settlements the player can still build.
     *
     * @return the amount of settlements the player can still build
     */
    public int getRemainingSettlements();

    /**
     * Returns a map of all development cards the player currently has and how many.
     *
     * @return a map of all development cards the player currently has and how many.
     */
    public Map<DevelopmentCardType, Integer> getDevelopmentCards();

    /**
     * Adds the given development card to the player.
     *
     * @param developmentCardType the development card to add
     */
    public void addDevelopmentCard(DevelopmentCardType developmentCardType);

    /**
     * Removes the given development card from the player.
     *
     * @param developmentCardType the development card to remove
     */
    public void removeDevelopmentCard(DevelopmentCardType developmentCardType);

    /**
     * Returns the total amount of development cards the player has.
     *
     * @return the total amount of development cards the player has
     */
    public int getTotalDevelopmentCards();

    /**
     * Returns the amount of knights the player has played.
     *
     * @return the amount of knights the player has played
     */
    public int getKnightsPlayed();

    /**
     * Returns the color of the player.
     *
     * @return the color of the player
     */
    Color getColor();
}
