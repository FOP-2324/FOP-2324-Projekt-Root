package projekt.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import projekt.SubmissionExecutionHandler;
import projekt.model.buildings.Edge;
import projekt.model.buildings.Settlement;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class HexGridImplTest {

    private final SubmissionExecutionHandler executionHandler = SubmissionExecutionHandler.getInstance();
    private HexGrid hexGrid;
    private Player player;

    @BeforeEach
    public void setup() {
        hexGrid = new HexGridImpl(1);
        player = new PlayerImpl.Builder(0).build(hexGrid);
    }

    @AfterEach
    public void reset() {
        executionHandler.resetMethodInvocationLogging();
        executionHandler.resetMethodDelegation();
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/HexGridImpl/roads.json")
    public void testGetRoads(JsonParameterSet params) throws NoSuchMethodException {
        List<Set<TilePosition>> roads = parseRoads(params);
        Map<Set<TilePosition>, Edge> expected = hexGrid.getEdges()
            .entrySet()
            .stream()
            .filter(entry -> roads.contains(entry.getKey()))
            .peek(entry -> entry.getValue().getRoadOwnerProperty().setValue(player))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Context context = contextBuilder()
            .add("player", player)
            .add("owned road positions", roads)
            .build();

        executionHandler.disableMethodDelegation(HexGridImpl.class.getDeclaredMethod("getRoads", Player.class));
        assertCallEquals(expected, () -> hexGrid.getRoads(player), context, result ->
            "The return value of getRoads(Player) did not match the expected one");
    }

    @Disabled
    @ParameterizedTest
    @JsonParameterSetTest("/model/HexGridImpl/roads.json")
    public void testGetLongestRoad(JsonParameterSet params) throws NoSuchMethodException {
        List<Set<TilePosition>> roads = parseRoads(params);
        List<Edge> expected = hexGrid.getEdges()
            .entrySet()
            .stream()
            .filter(entry -> roads.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .peek(edge -> edge.getRoadOwnerProperty().setValue(player))
            .toList();
        Context context = contextBuilder()
            .add("player", player)
            .add("owned road positions", roads)
            .build();

        executionHandler.disableMethodDelegation(HexGridImpl.class.getDeclaredMethod("getLongestRoad", Player.class));
        List<Edge> actual = assertCallNotNull(() -> hexGrid.getLongestRoad(player), context, result ->
            "An exception occurred while invoking HexGridImpl.getLongestRoad(Player) or return value is null");
        assertEquals(expected.size(), actual.size(), context, result ->
            "The list returned by getLongestRoad(Player) does not have the expected size");
        assertTrue(actual.containsAll(expected), context, result ->
            "The list returned by getLongestRoad(Player) does not contain all expected elements");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testAddRoad(boolean checkVillages) throws ReflectiveOperationException {
        List<List<TilePosition>> roads = List.of(
            List.of(new TilePosition(0, 0), new TilePosition(0, 1)),
            List.of(new TilePosition(0, 0), new TilePosition(0, -1))
        );

        executionHandler.disableMethodDelegation(HexGridImpl.class.getDeclaredMethod("addRoad", TilePosition.class, TilePosition.class, Player.class, boolean.class));
        for (List<TilePosition> road : roads) {
            Context context = contextBuilder()
                .add("position0", road.get(0))
                .add("position1", road.get(1))
                .add("player", player)
                .add("checkVillages", checkVillages)
                .build();
            assertCallFalse(() -> hexGrid.addRoad(road.get(0), road.get(1), player, checkVillages), context, result ->
                "The return value of addRoad is incorrect");

            Intersection intersection = hexGrid.getEdge(road.get(0), road.get(1)).getIntersections().iterator().next();
            Field settlementField = IntersectionImpl.class.getDeclaredField("settlement");
            settlementField.trySetAccessible();
            settlementField.set(intersection, new Settlement(player, Settlement.Type.VILLAGE, intersection));
            context = contextBuilder()
                .add(context)
                .add("owned village", intersection.getAdjacentTilePositions())
                .build();
            assertCallEquals(checkVillages, () -> hexGrid.addRoad(road.get(0), road.get(1), player, checkVillages), context, result ->
                "The return value of addRoad is incorrect");
            if (checkVillages) {
                assertEquals(player, hexGrid.getEdge(road.get(0), road.get(1)).getRoadOwner(), context, result ->
                    "The added road is not owned by the expected player");
            }

            Edge ownedRoad = intersection.getConnectedEdges()
                .stream()
                .filter(edge -> !edge.getAdjacentTilePositions().equals(Set.of(road.get(0), road.get(1))))
                .findAny()
                .orElseThrow();
            ownedRoad.getRoadOwnerProperty().setValue(player);
            context = contextBuilder()
                .add(context)
                .add("owned road", ownedRoad)
                .build();
            assertCallEquals(!checkVillages, () -> hexGrid.addRoad(road.get(0), road.get(1), player, checkVillages), context, result ->
                "The return value of addRoad is incorrect");
            if (!checkVillages) {
                assertEquals(player, hexGrid.getEdge(road.get(0), road.get(1)).getRoadOwner(), context, result ->
                    "The added road is not owned by the expected player");
            }
        }
    }

    @Disabled
    @Test
    public void testAddRoadThrows() throws NoSuchMethodException {
        List<TilePosition> tilePositions = List.of(new TilePosition(100, 100), new TilePosition(100, 101));
        Context context = contextBuilder()
            .add("tile positions", tilePositions)
            .add("player", player)
            .build();

        executionHandler.disableMethodDelegation(HexGridImpl.class.getDeclaredMethod("addRoad", TilePosition.class, TilePosition.class, Player.class, boolean.class));
        assertThrows(IllegalArgumentException.class,
            () -> hexGrid.addRoad(tilePositions.get(0), tilePositions.get(1), player, false),
            contextBuilder().add(context).add("checkVillages", false).build(),
            result -> "Expected IllegalArgumentException to be thrown");
        assertThrows(IllegalArgumentException.class,
            () -> hexGrid.addRoad(tilePositions.get(0), tilePositions.get(1), player, true),
            contextBuilder().add(context).add("checkVillages", true).build(),
            result -> "Expected IllegalArgumentException to be thrown");
    }

    private static List<Set<TilePosition>> parseRoads(JsonParameterSet params) {
        return params.<List<Map<String, Map<String, Integer>>>>get("pairs")
            .stream()
            .map(pair -> pair.values()
                .stream()
                .map(tilePosition -> new TilePosition(tilePosition.get("q"), tilePosition.get("r")))
                .collect(Collectors.toSet()))
            .toList();
    }
}
