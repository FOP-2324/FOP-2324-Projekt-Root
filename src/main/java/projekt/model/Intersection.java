package projekt.model;

import java.util.Set;
import java.util.stream.Collectors;

import projekt.model.buildings.Port;
import projekt.model.buildings.Road;
import projekt.model.buildings.Settlement;

public interface Intersection {
    /**
     * Returns the settlement on this intersection or null
     *
     * @return the settlement on this intersection
     */
    Settlement getSettlement();

    /**
     * Returns all settlements adjacent to this intersection.
     *
     * @return all settlements adjacent to this intersection
     */
    default Set<Settlement> getAdjacentSettlements() {
        return getAdjacentIntersections().stream().map(Intersection::getSettlement)
                .filter(settlement -> settlement != null).collect(Collectors.toSet());
    }

    /**
     * Places a village on this intersection for the given player.
     *
     * @param player the player who places the settlement
     */
    void placeVillage(Player player);

    /**
     * Upgrades the settlement on this intersection to a city.
     *
     * @param player the player who owns the settlement
     */
    void upgradeSettlement(Player player);

    /**
     * Returns the port on this intersection or null
     *
     * @return the port on this intersection
     */
    Port getPort();

    /**
     * Returns all Roads connected to this intersection.
     *
     * @return all Roads connected to this intersection
     */
    Set<Road> getConnectedRoads();

    /**
     * Returns all Intersection that are adjacent to this intersection.
     *
     * @return all Intersection that are adjacent to this intersection
     */
    Set<Intersection> getAdjacentIntersections();
}
