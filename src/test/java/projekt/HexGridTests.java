package projekt;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import projekt.model.GameState;
import projekt.model.HexGrid;
import projekt.model.HexGridImpl;
import projekt.model.Player;
import projekt.model.PlayerImpl;
import projekt.model.TilePosition;

import java.util.List;

public class HexGridTests {
    private GameState gameState;
    private HexGrid hexGrid;
    private Player player;

    @BeforeEach
    void setUp() {
        gameState = new GameState(
            new HexGridImpl(3), List.of(
            new PlayerImpl.Builder(1).color(Color.AQUA).build(this.hexGrid)));
        this.hexGrid = gameState.getGrid();
        this.player = gameState.getPlayers().get(0);
    }

    @Test
    void testIntersections() {
        for (final var tile : this.hexGrid.getTiles().values()) {
            final var intersections = tile.getIntersections();
            Assertions.assertEquals(6, intersections.size());
            for (final var intersection : intersections) {
                Assertions.assertTrue(this.hexGrid.getIntersections().containsValue(intersection));
                Assertions.assertTrue(intersection.getAdjacentTiles().contains(tile));
            }
        }
    }

    @Test
    void testRoads() {
        // place roads in center
        TilePosition.forEachSpiral(new TilePosition(0, 0), 2, (p, nums) -> {
            final var tile = this.hexGrid.getTiles().get(p);
            TilePosition.EdgeDirection.stream().forEach(dir -> {
                final var neighbour = TilePosition.neighbour(p, dir);
                if (this.hexGrid.getTiles().containsKey(neighbour)) {
                    Assertions.assertFalse(tile.addRoad(dir, this.player, false));
                }
            });
        });

        final var center = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        for (final var intersection : center.getIntersections()) {
            Assertions.assertEquals(3, intersection.getConnectedEdges().size());
        }

        for (final var road : this.hexGrid.getEdges().values()) {
            final var intersections = road.getIntersections();
            Assertions.assertEquals(2, intersections.size());
        }
    }

    @Test
    @DisplayName("longest Edge: trivial case of one road")
    void testLongestRoad1() {
        // trivial case of one road
        final var tile = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        tile.addRoad(TilePosition.EdgeDirection.EAST, this.player, false);

        final var longestRoadLength = this.hexGrid.getLongestRoad(this.player).size();
        Assertions.assertEquals(1, longestRoadLength);
    }

    @Test
    @DisplayName("longest Edge: two roads of length 1")
    void testLongestRoad2() {
        // two roads of length 1
        final var tile = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        tile.addRoad(TilePosition.EdgeDirection.EAST, this.player, false);
        tile.addRoad(TilePosition.EdgeDirection.WEST, this.player, false);

        final var longestRoadLength = this.hexGrid.getLongestRoad(this.player).size();
        Assertions.assertEquals(1, longestRoadLength);
    }

    @Test
    @DisplayName("longest Edge: one road with length 1 and one with length 2")
    void testLongestRoad3() {
        // one road with length 1 and one with length 2
        final var tile = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        tile.addRoad(TilePosition.EdgeDirection.EAST, this.player, false);
        tile.addRoad(TilePosition.EdgeDirection.NORTH_EAST, this.player, false);
        tile.addRoad(TilePosition.EdgeDirection.WEST, this.player, false);

        final var longestRoadLength = this.hexGrid.getLongestRoad(this.player).size();
        Assertions.assertEquals(2, longestRoadLength);
    }

    @Test
    @DisplayName("longest Edge: one cycle around the center")
    void testLongestRoad4() {
        // one cycle around the center
        final var tile = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        TilePosition.EdgeDirection.stream().forEach(dir -> tile.addRoad(dir, this.player, false));

        final var longestRoadLength = this.hexGrid.getLongestRoad(this.player).size();
        Assertions.assertEquals(6, longestRoadLength);
    }

    @Test
    @DisplayName("longest Edge: one cycle around the center and one offspring")
    void testLongestRoad5() {
        // one cycle around the center and one offspring
        final var tile = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        TilePosition.EdgeDirection.stream().forEach(dir -> tile.addRoad(dir, this.player, false));
        tile.getNeighbour(TilePosition.EdgeDirection.WEST).addRoad(TilePosition.EdgeDirection.NORTH_EAST, this.player,
                                                                   false
        );

        final var longestRoadLength = this.hexGrid.getLongestRoad(this.player).size();
        Assertions.assertEquals(7, longestRoadLength);
    }

    @Test
    @DisplayName("longest Edge: two tiles")
    void testLongestRoad6() {
        // twoTiles
        final var tile = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        TilePosition.EdgeDirection.stream().forEach(dir -> tile.addRoad(dir, this.player, false));
        final var neighbour = tile.getNeighbour(TilePosition.EdgeDirection.WEST);
        TilePosition.EdgeDirection.stream().forEach(dir -> neighbour.addRoad(dir, this.player, false));

        final var longestRoadLength = this.hexGrid.getLongestRoad(this.player).size();
        Assertions.assertEquals(11, longestRoadLength);
    }

    @Test
    @DisplayName("longest Edge: two tiles without middle road")
    void testLongestRoad7() {
        // twoTiles without middle road
        final var tile = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        TilePosition.EdgeDirection.stream().forEach(dir -> tile.addRoad(dir, this.player, false));
        final var neighbour = tile.getNeighbour(TilePosition.EdgeDirection.WEST);
        TilePosition.EdgeDirection.stream().forEach(dir -> neighbour.addRoad(dir, this.player, false));

        this.hexGrid.removeRoad(tile.getPosition(), neighbour.getPosition());

        final var longestRoadLength = this.hexGrid.getLongestRoad(this.player).size();
        Assertions.assertEquals(10, longestRoadLength);
    }

    @Test
    @DisplayName("longest Edge: three tiles")
    void testLongestRoad8() {
        // threeTiles
        final var tile = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        TilePosition.EdgeDirection.stream().forEach(dir -> tile.addRoad(dir, this.player, false));
        final var neighbour1 = tile.getNeighbour(TilePosition.EdgeDirection.WEST);
        TilePosition.EdgeDirection.stream().forEach(dir -> neighbour1.addRoad(dir, this.player, false));
        final var neighbour2 = tile.getNeighbour(TilePosition.EdgeDirection.NORTH_WEST);
        TilePosition.EdgeDirection.stream().forEach(dir -> neighbour2.addRoad(dir, this.player, false));

        final var longestRoadLength = this.hexGrid.getLongestRoad(this.player).size();
        Assertions.assertEquals(14, longestRoadLength);
    }

    @Test
    @DisplayName("longest Edge: three tiles without middle road")
    void testLongestRoad9() {
        // threeTiles outline
        final var tile = this.hexGrid.getTiles().get(new TilePosition(0, 0));
        TilePosition.EdgeDirection.stream().forEach(dir -> tile.addRoad(dir, this.player, false));
        final var neighbour1 = tile.getNeighbour(TilePosition.EdgeDirection.WEST);
        TilePosition.EdgeDirection.stream().forEach(dir -> neighbour1.addRoad(dir, this.player, false));
        final var neighbour2 = tile.getNeighbour(TilePosition.EdgeDirection.NORTH_WEST);
        TilePosition.EdgeDirection.stream().forEach(dir -> neighbour2.addRoad(dir, this.player, false));

        this.hexGrid.removeRoad(tile.getPosition(), neighbour1.getPosition());
        this.hexGrid.removeRoad(tile.getPosition(), neighbour2.getPosition());

        final var longestRoadLength = this.hexGrid.getLongestRoad(this.player).size();
        Assertions.assertEquals(12, longestRoadLength);
    }
}
