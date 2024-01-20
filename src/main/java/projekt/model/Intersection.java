package projekt.model;

import java.util.Set;
import java.util.stream.Collectors;

import projekt.model.buildings.Port;
import projekt.model.buildings.Road;
import projekt.model.buildings.Settlement;
import projekt.model.tiles.Tile;

public interface Intersection {
    /**
     * Returns the hexGrid instance
     *
     * @return the hexGrid instance
     */
    HexGrid getHexGrid();

    /**
     * Returns the settlement on this intersection or null
     *
     * @return the settlement on this intersection
     */
    Settlement getSettlement();

    /**
     * Places a village on this intersection for the given player.
     *
     * @param player the player who places the settlement
     * @return wether the placement was successful
     */
    boolean placeVillage(Player player);

    /**
     * Upgrades the settlement on this intersection to a city.
     *
     * @param player the player who owns the settlement
     * @return wether the upgrade was successful
     */
    boolean upgradeSettlement(Player player);

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

    /**
     * Returns a set of all adjacent Positions
     *
     * @return a set of all adjacent Positions
     */
    Set<TilePosition> getAdjacentTilePositions();

    /**
     * Returns a set of all adjacent Tiles
     *
     * @return a set of all adjacent Tiles
     */
    default Set<Tile> getAdjacentTiles() {
        return getHexGrid().getTiles().entrySet().stream()
                .filter(entrySet -> getAdjacentTilePositions().contains(entrySet.getKey()))
                .map(entrySet -> entrySet.getValue()).collect(Collectors.toSet());
    }

    /**
     * Checks wether this intersection is connected to the given position
     *
     * @param position the position to check
     * @return wether the position is connected
     */
    boolean isConnectedTo(TilePosition position);

    /**
     * Checks wether is connected to all given positions
     *
     * @param position the positions to check
     * @return wether all positions are connected
     */
    boolean isConnectedTo(TilePosition... position);

    /**
     * Checks whether has a connecting road to the given intersection
     *
     * @param intersection the intersection to check
     * @return whether there is a connecting road
     */
    boolean hasConnectingRoad(Intersection intersection);
}
