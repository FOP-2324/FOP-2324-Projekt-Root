package projekt;

import org.junit.jupiter.params.ParameterizedTest;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import projekt.model.*;
import projekt.model.buildings.Settlement;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class IntersectionImplTest {

    @ParameterizedTest
    @JsonParameterSetTest("/IntersectionImpl/tilePositions.json")
    public void testPlaceVillage_roadCheck(JsonParameterSet params) {
        testPlaceVillage(false, params);
    }

    @ParameterizedTest
    @JsonParameterSetTest("/IntersectionImpl/tilePositions.json")
    public void testPlaceVillage_noRoadCheck(JsonParameterSet params) {
        testPlaceVillage(true, params);
    }

    private static void testPlaceVillage(boolean ignoreRoadCheck, JsonParameterSet params) {
        HexGrid hexGrid = new HexGridImpl(1);
        Player player = new PlayerImpl(hexGrid, null);
        List<TilePosition> tilePositions = params.<List<Map<String, Integer>>>get("tilePositions")
            .stream()
            .map(map -> new TilePosition(map.get("q"), map.get("r")))
            .toList();
        Intersection intersection = hexGrid.getIntersectionAt(tilePositions.get(0), tilePositions.get(1), tilePositions.get(2));
        AtomicBoolean connectedRoad = new AtomicBoolean();
        Context context = contextBuilder()
            .add("intersection", intersection)
            .add("player", player)
            .add("ignoreRoadCheck", ignoreRoadCheck)
            .add("connected road", connectedRoad)
            .build();

        if (!ignoreRoadCheck) {
            assertCallFalse(() -> intersection.placeVillage(player, false), context, result ->
                "Return value of placeVillage is incorrect");
            hexGrid.getEdge(tilePositions.get(0), tilePositions.get(1)).roadOwner().setValue(player);
            connectedRoad.set(true);
        }

        assertCallTrue(() -> intersection.placeVillage(player, ignoreRoadCheck), context, result ->
            "Return value of placeVillage is incorrect");
        Settlement actualSettlement = assertCallNotNull(intersection::getSettlement, context, result ->
            "Method getSettlement returned null after invoking placeVillage");
        assertEquals(new Settlement(player, Settlement.Type.VILLAGE, intersection), actualSettlement, context, result ->
            "Placed settlement does not have the expected specifications");
        assertCallFalse(() -> intersection.placeVillage(player, ignoreRoadCheck), context, result ->
            "Return value of placeVillage is incorrect when invoked again after placing village");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/IntersectionImpl/tilePositions.json")
    public void testUpgradeSettlement(JsonParameterSet params) throws ReflectiveOperationException {
        HexGrid hexGrid = new HexGridImpl(1);
        Player player = new PlayerImpl(hexGrid, null);
        List<TilePosition> tilePositions = params.<List<Map<String, Integer>>>get("tilePositions")
            .stream()
            .map(map -> new TilePosition(map.get("q"), map.get("r")))
            .toList();
        Intersection intersection = hexGrid.getIntersectionAt(tilePositions.get(0), tilePositions.get(1), tilePositions.get(2));
        Field settlementField = IntersectionImpl.class.getDeclaredField("settlement");
        settlementField.trySetAccessible();
        AtomicReference<Settlement> settlementRef = new AtomicReference<>();
        Context context = contextBuilder()
            .add("intersection", intersection)
            .add("player", player)
            .add("settlement", settlementRef)
            .build();

        assertCallFalse(() -> intersection.upgradeSettlement(player), context, result ->
            "Return value of upgradeSettlement is incorrect");

        settlementRef.set(new Settlement(new PlayerImpl(hexGrid, null), Settlement.Type.CITY, intersection));
        settlementField.set(intersection, settlementRef.get());
        assertCallFalse(() -> intersection.upgradeSettlement(player), context, result ->
            "Return value of upgradeSettlement is incorrect");

        settlementRef.set(new Settlement(new PlayerImpl(hexGrid, null), Settlement.Type.VILLAGE, intersection));
        settlementField.set(intersection, settlementRef.get());
        assertCallFalse(() -> intersection.upgradeSettlement(player), context, result ->
            "Return value of upgradeSettlement is incorrect");

        settlementRef.set(new Settlement(player, Settlement.Type.VILLAGE, intersection));
        settlementField.set(intersection, settlementRef.get());
        assertCallTrue(() -> intersection.upgradeSettlement(player), context, result ->
            "Return value of upgradeSettlement is incorrect");

        assertCallEquals(new Settlement(player, Settlement.Type.CITY, intersection), intersection::getSettlement, context, result ->
            "Upgraded settlement does not have the expected specifications");
    }
}
