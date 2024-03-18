package projekt.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import projekt.SubmissionExecutionHandler;
import projekt.model.buildings.Edge;
import projekt.model.buildings.EdgeImpl;
import projekt.model.buildings.Port;
import projekt.model.buildings.Settlement;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class PlayerImplTest {

    private final SubmissionExecutionHandler executionHandler = SubmissionExecutionHandler.getInstance();
    private PlayerImpl instance;

    @BeforeEach
    public void setup() {
        instance = (PlayerImpl) new PlayerImpl.Builder(0).build(null);
    }

    @AfterEach
    public void reset() {
        executionHandler.resetMethodInvocationLogging();
        executionHandler.resetMethodDelegation();
    }

    @Test
    public void testGetResources() throws ReflectiveOperationException {
        Map<ResourceType, Integer> expected = IntStream.range(0, ResourceType.values().length)
            .mapToObj(i -> Map.entry(ResourceType.values()[i], i))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Field resourcesField = PlayerImpl.class.getDeclaredField("resources");
        resourcesField.trySetAccessible();
        resourcesField.set(instance, expected);
        Context context = contextBuilder()
            .add("resources", expected.toString())
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("getResources"));
        Map<ResourceType, Integer> actual = callObject(instance::getResources, context, result ->
            "An exception occurred while invoking PlayerImpl.getResources");

        assertNotNull(actual, context, result ->
            "The Map object returned by PlayerImpl.getResources is null");
        assertThrows(UnsupportedOperationException.class, () -> actual.putAll(Collections.emptyMap()), context, result ->
            "The Map object returned by PlayerImpl.getResources is not immutable");
        assertEquals(expected.size(), actual.size(), context, result ->
            "The Map object returned by PlayerImpl.getResources does not have the expected size");
        expected.forEach((key, value) -> {
            assertTrue(actual.containsKey(key), context, result ->
                "The Map object returned by PlayerImpl.getResources does not contain key " + key);
            assertEquals(value, actual.get(key), context, result ->
                "The Map object returned by PlayerImpl.getResources does not have the correct mapping for key " + key);
        });
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/addResource.json")
    public void testAddResource(JsonParameterSet params) throws ReflectiveOperationException {
        Context context = contextBuilder()
            .add("resourceType", params.get("resource"))
            .add("amount", params.get("amount"))
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("addResource", ResourceType.class, int.class));
        call(() -> instance.addResource(ResourceType.valueOf(params.get("resource")), params.get("amount")), context, result ->
            "An exception occurred while invoking PlayerImpl.addResource");
        assertEquals(params.getInt("amount"), instance.getResources().get(ResourceType.valueOf(params.get("resource"))), context, result ->
            "Field resources in PlayerImpl does not contain the correct mapping after invoking addResource");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/add_has_removeResources.json")
    public void testAddResources(JsonParameterSet params) throws ReflectiveOperationException {
        makeResourceMap(params.get("backing")).forEach(instance::addResource);

        Map<ResourceType, Integer> resourcesParam = Collections.unmodifiableMap(makeResourceMap(params.get("input")));
        Map<ResourceType, Integer> expectedResult = makeResourceMap(params.get("result_add"));
        Context context = contextBuilder()
            .add("resources", instance.getResources())
            .add("resources (parameter)", resourcesParam)
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("addResources", Map.class));
        call(() -> instance.addResources(resourcesParam), context, result ->
            "An exception occurred while invoking PlayerImpl.addResources");
        assertEquals(expectedResult.keySet(), instance.getResources().keySet(), context, result ->
            "Field resources has an unexpected key set after calling PlayerImpl.addResources");
        for (ResourceType resourceType : expectedResult.keySet()) {
            assertEquals(expectedResult.get(resourceType), instance.getResources().get(resourceType), context, result ->
                "Mapping with key " + resourceType + " in field resources has an unexpected value");
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/add_has_removeResources.json")
    public void testHasResources(JsonParameterSet params) throws ReflectiveOperationException {
        Map<ResourceType, Integer> resourcesPreMod = makeResourceMap(params.get("backing"));
        instance.addResources(resourcesPreMod);

        Map<ResourceType, Integer> resourcesParam = Collections.unmodifiableMap(makeResourceMap(params.get("input")));
        boolean expectedResult = params.getBoolean("result");
        Context context = contextBuilder()
            .add("resources", resourcesPreMod)
            .add("resources (parameter)", resourcesParam)
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("hasResources", Map.class));
        assertEquals(expectedResult,
            callObject(() -> instance.hasResources(resourcesParam), context, result ->
                "An exception occurred while invoking PlayerImpl.hasResources"),
            context,
            result -> "The return value of PlayerImpl.hasResources did not match the expected value");
        assertEquals(resourcesPreMod.keySet(), instance.getResources().keySet(), context, result ->
            "Field resources has an unexpected key set after calling PlayerImpl.hasResources");
        for (ResourceType resourceType : resourcesPreMod.keySet()) {
            assertEquals(resourcesPreMod.get(resourceType), instance.getResources().get(resourceType), context, result ->
                "Mapping with key " + resourceType + " in field resources has an unexpected value");
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/removeResource.json")
    public void testRemoveResource(JsonParameterSet params) throws ReflectiveOperationException {
        Map<ResourceType, Integer> resourcesPreMod = makeResourceMap(params.get("backing"));
        instance.addResources(resourcesPreMod);

        ResourceType resource = ResourceType.valueOf(params.get("resource"));
        int amount = params.getInt("amount");
        boolean expectedResult = params.getBoolean("result");
        Context context = contextBuilder()
            .add("resources", resourcesPreMod)
            .add("resourceType", resource)
            .add("amount", amount)
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("removeResource", ResourceType.class, int.class));
        assertEquals(expectedResult,
            callObject(() -> instance.removeResource(resource, amount), context, result ->
                "An exception occurred while invoking PlayerImpl.removeResource"),
            context,
            result -> "The return value of PlayerImpl.removeResource did not match the expected value");
        assertEquals(resourcesPreMod.keySet(), instance.getResources().keySet(), context, result ->
            "Field resources has an unexpected key set after calling PlayerImpl.removeResource");
        for (ResourceType resourceType : resourcesPreMod.keySet()) {
            assertEquals(resourcesPreMod.get(resourceType) - (expectedResult && resourceType == resource ? amount : 0),
                instance.getResources().get(resourceType),
                context,
                result -> "Mapping with key " + resourceType + " in field resources has an unexpected value");
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/add_has_removeResources.json")
    public void testRemoveResources(JsonParameterSet params) throws ReflectiveOperationException {
        Map<ResourceType, Integer> resourcesPreMod = makeResourceMap(params.get("backing"));
        instance.addResources(resourcesPreMod);

        Map<ResourceType, Integer> resourcesParam = Collections.unmodifiableMap(makeResourceMap(params.get("input")));
        boolean expectedResult = params.getBoolean("result");
        Context context = contextBuilder()
            .add("resources", resourcesPreMod)
            .add("resources (parameter)", resourcesParam)
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("removeResources", Map.class));
        assertEquals(expectedResult,
            callObject(() -> instance.removeResources(resourcesParam), context, result ->
                "An exception occurred while invoking PlayerImpl.removeResources"),
            context,
            result -> "The return value of PlayerImpl.removeResources did not match the expected value");
        assertEquals(resourcesPreMod.keySet(), instance.getResources().keySet(), context, result ->
            "Field resources has an unexpected key set after calling PlayerImpl.removeResources");
        for (ResourceType resourceType : resourcesPreMod.keySet()) {
            assertEquals(resourcesPreMod.get(resourceType) - (expectedResult ? resourcesParam.getOrDefault(resourceType, 0) : 0),
                instance.getResources().get(resourceType),
                context,
                result -> "Mapping with key " + resourceType + " in field resources has an unexpected value");
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/developmentCards.json")
    public void testGetDevelopmentCards(JsonParameterSet params) throws ReflectiveOperationException {
        Map<DevelopmentCardType, Integer> backing = makeDevelopmentCardMap(params.get("backing"));
        Field developmentCardsField = PlayerImpl.class.getDeclaredField("developmentCards");
        developmentCardsField.trySetAccessible();
        developmentCardsField.set(instance, backing);

        Context context = contextBuilder()
            .add("developmentCards", backing)
            .build();
        Map<DevelopmentCardType, Integer> expected = makeDevelopmentCardMap(params.get("backing"));
        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("getDevelopmentCards"));
        Map<DevelopmentCardType, Integer> actual = callObject(instance::getDevelopmentCards, context, result ->
            "An exception occurred while invoking PlayerImpl.getDevelopmentCards");

        assertNotNull(actual, context, result ->
            "The Map object returned by PlayerImpl.getDevelopmentCards is null");
        assertThrows(UnsupportedOperationException.class, () -> actual.putAll(Collections.emptyMap()), context, result ->
            "The Map object returned by PlayerImpl.getDevelopmentCards is not immutable");
        assertEquals(expected.size(), actual.size(), context, result ->
            "The Map object returned by PlayerImpl.getDevelopmentCards does not have the expected size");
        expected.forEach((key, value) -> {
            assertTrue(actual.containsKey(key), context, result ->
                "The Map object returned by PlayerImpl.getDevelopmentCards does not contain key " + key);
            assertEquals(value, actual.get(key), context, result ->
                "The Map object returned by PlayerImpl.getDevelopmentCards does not have the correct mapping for key " + key);
        });
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/developmentCards.json")
    public void testAddDevelopmentCard(JsonParameterSet params) throws ReflectiveOperationException {
        Map<DevelopmentCardType, Integer> backingPreMod = makeDevelopmentCardMap(params.get("backing"));
        Map<DevelopmentCardType, Integer> backing = makeDevelopmentCardMap(params.get("backing"));
        Field developmentCardsField = PlayerImpl.class.getDeclaredField("developmentCards");
        developmentCardsField.trySetAccessible();
        developmentCardsField.set(instance, backing);

        DevelopmentCardType input = params.get("input", DevelopmentCardType.class);
        Context context = contextBuilder()
            .add("developmentCards", backing)
            .add("developmentCard (parameter)", input)
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("addDevelopmentCard", DevelopmentCardType.class));
        call(() -> instance.addDevelopmentCard(input), context, result ->
            "An exception occurred while invoking PlayerImpl.addDevelopmentCard");
        backingPreMod.forEach((key, value) ->
            assertEquals((value != null ? value : 0) + (key == input ? 1 : 0), backing.get(key), context, result ->
                "Mapping with key " + key + " in field developmentCards has an unexpected value"));
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/developmentCards.json")
    public void testRemoveDevelopmentCard(JsonParameterSet params) throws ReflectiveOperationException {
        Map<DevelopmentCardType, Integer> backingPreMod = makeDevelopmentCardMap(params.get("backing"));
        backingPreMod.forEach((developmentCard, amount) -> {
            for (int i = 0; i < amount; i++) {
                instance.addDevelopmentCard(developmentCard);
            }
        });

        DevelopmentCardType input = params.get("input", DevelopmentCardType.class);
        boolean expected = params.getBoolean("result");
        Context context = contextBuilder()
            .add("developmentCards", instance.getDevelopmentCards())
            .add("developmentCard (parameter)", input)
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("removeDevelopmentCard", DevelopmentCardType.class));
        assertEquals(expected,
            callObject(() -> instance.removeDevelopmentCard(input), context, result ->
                "An exception occurred while invoking PlayerImpl.removeDevelopmentCard"),
            context,
            result -> "The return value of PlayerImpl.removeDevelopmentCard did not match the expected value");
        assertEquals(backingPreMod.keySet(), instance.getDevelopmentCards().keySet(), context, result ->
            "Field developmentCards has an unexpected key set after calling PlayerImpl.removeDevelopmentCard");
        backingPreMod.forEach((key, value) ->
            assertEquals(value - (key == input ? 1 : 0), instance.getDevelopmentCards().get(key), context, result ->
                "Mapping with key " + key + " in field developmentCards has an unexpected value"));
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/developmentCards.json")
    public void testGetTotalDevelopmentCards(JsonParameterSet params) throws ReflectiveOperationException {
        makeDevelopmentCardMap(params.get("backing")).forEach((developmentCard, amount) -> {
            for (int i = 0; i < amount; i++) {
                instance.addDevelopmentCard(developmentCard);
            }
        });

        Context context = contextBuilder()
            .add("method under test", "getTotalDevelopmentCards")
            .add("developmentCards", instance.getDevelopmentCards())
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("getTotalDevelopmentCards"));
        assertEquals(params.getInt("totalCards"),
            callObject(instance::getTotalDevelopmentCards, context, result ->
                "An exception occurred while invoking PlayerImpl.getTotalDevelopmentCards"),
            context,
            result -> "The returned value does not equal the expected one");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/developmentCards.json")
    public void testGetKnightsPlayed_viaField(JsonParameterSet params) throws ReflectiveOperationException {
        Field playedDevelopmentCardsField = PlayerImpl.class.getDeclaredField("playedDevelopmentCards");
        Map<DevelopmentCardType, Integer> backingNoMod = makeDevelopmentCardMap(params.get("backing"));
        Map<DevelopmentCardType, Integer> backing = makeDevelopmentCardMap(params.get("backing"));
        playedDevelopmentCardsField.trySetAccessible();
        playedDevelopmentCardsField.set(instance, backing);

        Context context = contextBuilder()
            .add("method under test", "getKnightsPlayed")
            .add("playedDevelopmentCards", backing)
            .build();

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("getKnightsPlayed"));
        assertEquals(backingNoMod.getOrDefault(DevelopmentCardType.KNIGHT, 0),
            callObject(instance::getKnightsPlayed, context, result ->
                "An exception occurred while invoking PlayerImpl.getKnightsPlayed"),
            context,
            result -> "The returned value does not equal the expected one");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/model/PlayerImpl/developmentCards.json")
    public void testGetKnightsPlayed_viaMethod(JsonParameterSet params) throws ReflectiveOperationException {
        Map<DevelopmentCardType, Integer> backingNoMod = makeDevelopmentCardMap(params.get("backing"));
        backingNoMod.forEach((developmentCard, amount) -> {
            for (int i = 0; i < amount; i++) {
                instance.addDevelopmentCard(developmentCard);
            }
        });

        Context context = contextBuilder()
            .add("method under test", "getKnightsPlayed")
            .add("developmentCards", instance.getDevelopmentCards())
            .build();
        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("removeDevelopmentCard", DevelopmentCardType.class));
        for (int i = 0; i < backingNoMod.getOrDefault(DevelopmentCardType.KNIGHT, 0); i++) {
            call(() -> instance.removeDevelopmentCard(DevelopmentCardType.KNIGHT), context, result ->
                "An exception occurred while invoking PlayerImpl.removeDevelopmentCard");
        }

        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("getKnightsPlayed"));
        assertEquals(backingNoMod.getOrDefault(DevelopmentCardType.KNIGHT, 0),
            callObject(instance::getKnightsPlayed, context, result ->
                "An exception occurred while invoking PlayerImpl.getKnightsPlayed"),
            context,
            result -> "The returned value does not equal the expected one");
    }

    private static Map<DevelopmentCardType, Integer> makeDevelopmentCardMap(List<Map<String, ?>> input) {
        return input.stream()
            .map(map -> Map.entry(DevelopmentCardType.valueOf((String) map.get("key")), (Integer) map.get("value")))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Test
    public void testGetTradeRatio() throws ReflectiveOperationException {
        HexGridImpl hexGrid = new HexGridImpl(1);
        Player player = new PlayerImpl.Builder(0).build(hexGrid);
        executionHandler.disableMethodDelegation(PlayerImpl.class.getDeclaredMethod("getTradeRatio", ResourceType.class));

        // Test default
        {
            Context context = contextBuilder()
                .add("hexGrid", "radius=1, intersections=6, no intersections/ports owned by player")
                .build();
            assertEquals(4, player.getTradeRatio(ResourceType.ORE), context, result ->
                "PlayerImpl.getTradeRatio did not return the correct value");
        }

        Field edgesField = HexGridImpl.class.getDeclaredField("edges");
        edgesField.trySetAccessible();
        Field settlementField = IntersectionImpl.class.getDeclaredField("settlement");
        settlementField.trySetAccessible();

        Map<Set<TilePosition>, Port> ports = Map.of(
            Set.of(new TilePosition(-1, 0), new TilePosition(0, 0)), new Port(3),
            Set.of(new TilePosition(1, 0), new TilePosition(0, 0)), new Port(2, ResourceType.ORE)
        );
        Context context = contextBuilder()
            .add("hexGrid", "radius=1, intersections=6, one port with ratio=3 and one with ratio=2 and resourceType=ORE owned by player")
            .build();

        ports.forEach((tilePositions, port) -> {
            try {
                @SuppressWarnings("unchecked") Map<Set<TilePosition>, Edge> edges = (Map<Set<TilePosition>, Edge>) edgesField.get(hexGrid);
                Edge oldEdge = edges.get(tilePositions);
                Edge newEdge = new EdgeImpl(oldEdge.getHexGrid(), oldEdge.getPosition1(), oldEdge.getPosition2(), oldEdge.getRoadOwnerProperty(), port);
                edges.put(tilePositions, newEdge);

                Intersection intersection = newEdge.getIntersections().stream().findAny().orElseThrow();
                settlementField.set(intersection, new Settlement(player, Settlement.Type.VILLAGE, intersection));
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });

        for (Port port : ports.values()) {
            ResourceType resourceType = port.resourceType() == null ? ResourceType.values()[0] : port.resourceType();
            assertEquals(port.ratio(),
                player.getTradeRatio(resourceType),
                contextBuilder().add(context).add("tested port", port).add("resourceType", resourceType).build(),
                result -> "PlayerImpl.getTradeRatio did not return the correct value");
        }
    }

    private static Map<ResourceType, Integer> makeResourceMap(List<Map<String, ?>> input) {
        return input.stream()
            .map(map -> Map.entry(ResourceType.valueOf((String) map.get("key")), (Integer) map.get("value")))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
