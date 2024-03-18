package projekt.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import projekt.SubmissionExecutionHandler;
import projekt.model.buildings.Edge;
import projekt.model.buildings.EdgeImpl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class EdgeImplTest {

    private final SubmissionExecutionHandler executionHandler = SubmissionExecutionHandler.getInstance();
    private HexGrid hexGrid;

    @BeforeEach
    public void setup() {
        hexGrid = new HexGridImpl(1);
    }

    @AfterEach
    public void reset() {
        executionHandler.resetMethodInvocationLogging();
        executionHandler.resetMethodDelegation();
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/EdgeImpl/edges.json")
    public void testGetIntersections(JsonParameterSet params) throws NoSuchMethodException {
        List<TilePosition> tilePositions = getTilePositions(params, "edge");
        Edge instance = hexGrid.getEdge(tilePositions.get(0), tilePositions.get(1));
        Context context = contextBuilder()
            .add("edge", instance)
            .build();
        executionHandler.disableMethodDelegation(EdgeImpl.class.getDeclaredMethod("getIntersections"));

        Set<Set<TilePosition>> expected = Set.of(
            Set.copyOf(getTilePositions(params, "leftIntersection")),
            Set.copyOf(getTilePositions(params, "rightIntersection")));
        Set<Set<TilePosition>> actual = callObject(instance::getIntersections, context, result ->
            "An exception occurred while invoking EdgeImpl.getIntersections")
            .stream()
            .map(Intersection::getAdjacentTilePositions)
            .collect(Collectors.toSet());
        assertEquals(expected, actual, context, result ->
            "The tile positions of the returned intersections differ from the expected values");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/EdgeImpl/edges.json")
    public void testConnectsTo(JsonParameterSet params) throws NoSuchMethodException {
        List<TilePosition> tilePositions = getTilePositions(params, "edge");
        List<TilePosition> otherEdgeTilePositions = getTilePositions(params, "otherEdge");
        Edge instance = hexGrid.getEdge(tilePositions.get(0), tilePositions.get(1));
        Edge otherEdge = hexGrid.getEdge(otherEdgeTilePositions.get(0), otherEdgeTilePositions.get(1));
        Context context = contextBuilder()
            .add("edge", instance)
            .add("other edge (parameter)", otherEdge)
            .build();

        executionHandler.disableMethodDelegation(EdgeImpl.class.getDeclaredMethod("connectsTo", Edge.class));
        assertEquals(params.getBoolean("connectsToOtherEdge"),
            callObject(() -> instance.connectsTo(otherEdge), context, result ->
                "An exception occurred while invoking EdgeImpl.connectsTo"),
            context,
            result -> "The returned value does not match the expected one");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/EdgeImpl/edges.json")
    public void testGetConnectedRoads(JsonParameterSet params) throws NoSuchMethodException {
        List<TilePosition> tilePositions = getTilePositions(params, "edge");
        Edge instance = hexGrid.getEdge(tilePositions.get(0), tilePositions.get(1));
        Map<Set<TilePosition>, Edge> edges = hexGrid.getEdges();
        Player player = new PlayerImpl.Builder(0).build(hexGrid);
        Set<Set<TilePosition>> roads = params.<List<List<Map<String, Integer>>>>get("roads")
            .stream()
            .map(EdgeImplTest::getTilePositions)
            .map(Set::copyOf)
            .collect(Collectors.toSet());
        roads.stream()
            .map(edges::get)
            .forEach(edge -> edge.getRoadOwnerProperty().setValue(player));
        Context context = contextBuilder()
            .add("edge", instance)
            .add("player", player)
            .add("roads owned by " + player, roads)
            .build();

        executionHandler.disableMethodDelegation(EdgeImpl.class.getDeclaredMethod("getConnectedRoads", Player.class));
        assertEquals(roads,
            callObject(() -> instance.getConnectedRoads(player), context, result ->
                "An exception occurred while invoking EdgeImpl.getConnectedRoads")
                .stream()
                .map(Edge::getAdjacentTilePositions)
                .collect(Collectors.toSet()),
            context,
            result -> "The tile positions of the returned edges do not match the expected values");
    }

    private static List<TilePosition> getTilePositions(JsonParameterSet params, String key) {
        return getTilePositions(params.get(key));
    }

    private static List<TilePosition> getTilePositions(List<Map<String, Integer>> serializedTilePositions) {
        return serializedTilePositions.stream()
            .map(map -> new TilePosition(map.get("q"), map.get("r")))
            .toList();
    }
}
