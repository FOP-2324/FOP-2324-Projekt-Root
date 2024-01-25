package projekt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import projekt.model.*;
import projekt.model.buildings.Port;
import projekt.model.buildings.Settlement;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class PlayerImplTest {

    @Nested
    public class InventorySystem {

        private PlayerImpl instance;
        private Field resourcesField;

        @BeforeEach
        public void setup() throws ReflectiveOperationException {
            instance = new PlayerImpl(null, null);
            resourcesField = PlayerImpl.class.getDeclaredField("resources");
            resourcesField.trySetAccessible();
        }

        @Test
        public void testGetResources() throws ReflectiveOperationException {
            Map<ResourceType, Integer> expected = IntStream.range(0, ResourceType.values().length)
                .mapToObj(i -> Map.entry(ResourceType.values()[i], i))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            resourcesField.set(instance, expected);
            Context context = contextBuilder()
                .add("resources", expected.toString())
                .build();

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
        @JsonParameterSetTest("/PlayerImpl/addResource.json")
        public void testAddResource(JsonParameterSet params) throws ReflectiveOperationException {
            Map<ResourceType, Integer> resourcesBacking = (Map<ResourceType, Integer>) resourcesField.get(instance);
            Context context = contextBuilder()
                .add("resourceType", params.get("resource"))
                .add("amount", params.get("amount"))
                .build();

            call(() -> instance.addResource(ResourceType.valueOf(params.get("resource")), params.get("amount")), context, result ->
                "An exception occurred while invoking PlayerImpl.addResource");
            assertEquals(params.getInt("amount"), resourcesBacking.get(ResourceType.valueOf(params.get("resource"))), context, result ->
                "Field resources in PlayerImpl does not contain the correct mapping after invoking addResource");
        }

        @ParameterizedTest
        @JsonParameterSetTest("/PlayerImpl/removeResource.json")
        public void testRemoveResource(JsonParameterSet params) throws ReflectiveOperationException {
            Map<ResourceType, Integer> resourcesPreMod = makeResourceMap(params.get("backing"));
            Map<ResourceType, Integer> resourcesBacking = makeResourceMap(params.get("backing"));
            resourcesField.set(instance, resourcesBacking);

            ResourceType resource = ResourceType.valueOf(params.get("resource"));
            int amount = params.getInt("amount");
            boolean expectedResult = params.getBoolean("result");
            Context context = contextBuilder()
                .add("resources", resourcesPreMod)
                .add("resourceType", resource)
                .add("amount", amount)
                .build();

            assertEquals(expectedResult,
                callObject(() -> instance.removeResource(resource, amount), context, result ->
                    "An exception occurred while invoking PlayerImpl.removeResource"),
                context,
                result -> "The return value of PlayerImpl.removeResource did not match the expected value");
            assertEquals(resourcesPreMod.keySet(), resourcesBacking.keySet(), context, result ->
                "Field resources has an unexpected key set after calling PlayerImpl.removeResource");
            for (ResourceType resourceType : resourcesPreMod.keySet()) {
                assertEquals(resourcesPreMod.get(resourceType) - (expectedResult && resourceType == resource ? amount : 0),
                    resourcesBacking.get(resourceType),
                    context,
                    result -> "Mapping with key " + resourceType + " in field resources has an unexpected value");
            }
        }

        @ParameterizedTest
        @JsonParameterSetTest("/PlayerImpl/has_removeResources.json")
        public void testRemoveResources(JsonParameterSet params) throws ReflectiveOperationException {
            Map<ResourceType, Integer> resourcesPreMod = makeResourceMap(params.get("backing"));
            Map<ResourceType, Integer> resourcesBacking = makeResourceMap(params.get("backing"));
            resourcesField.set(instance, resourcesBacking);

            Map<ResourceType, Integer> resourcesParam = Collections.unmodifiableMap(makeResourceMap(params.get("input")));
            boolean expectedResult = params.getBoolean("result");
            Context context = contextBuilder()
                .add("resources", resourcesPreMod)
                .add("resources (parameter)", resourcesParam)
                .build();

            assertEquals(expectedResult,
                callObject(() -> instance.removeResources(resourcesParam), context, result ->
                    "An exception occurred while invoking PlayerImpl.removeResources"),
                context,
                result -> "The return value of PlayerImpl.removeResources did not match the expected value");
            assertEquals(resourcesPreMod.keySet(), resourcesBacking.keySet(), context, result ->
                "Field resources has an unexpected key set after calling PlayerImpl.removeResources");
            for (ResourceType resourceType : resourcesPreMod.keySet()) {
                assertEquals(resourcesPreMod.get(resourceType) - (expectedResult ? resourcesParam.getOrDefault(resourceType, 0) : 0),
                    resourcesBacking.get(resourceType),
                    context,
                    result -> "Mapping with key " + resourceType + " in field resources has an unexpected value");
            }
        }

        @ParameterizedTest
        @JsonParameterSetTest("/PlayerImpl/has_removeResources.json")
        public void testHasResources(JsonParameterSet params) throws ReflectiveOperationException {
            Map<ResourceType, Integer> resourcesPreMod = makeResourceMap(params.get("backing"));
            Map<ResourceType, Integer> resourcesBacking = makeResourceMap(params.get("backing"));
            resourcesField.set(instance, resourcesBacking);

            Map<ResourceType, Integer> resourcesParam = Collections.unmodifiableMap(makeResourceMap(params.get("input")));
            boolean expectedResult = params.getBoolean("result");
            Context context = contextBuilder()
                .add("resources", resourcesPreMod)
                .add("resources (parameter)", resourcesParam)
                .build();

            assertEquals(expectedResult,
                callObject(() -> instance.hasResources(resourcesParam), context, result ->
                    "An exception occurred while invoking PlayerImpl.hasResources"),
                context,
                result -> "The return value of PlayerImpl.hasResources did not match the expected value");
            assertEquals(resourcesPreMod.keySet(), resourcesBacking.keySet(), context, result ->
                "Field resources has an unexpected key set after calling PlayerImpl.hasResources");
            for (ResourceType resourceType : resourcesPreMod.keySet()) {
                assertEquals(resourcesPreMod.get(resourceType), resourcesBacking.get(resourceType), context, result ->
                    "Mapping with key " + resourceType + " in field resources has an unexpected value");
            }
        }

        private static Map<ResourceType, Integer> makeResourceMap(List<Map<String, ?>> input) {
            return input.stream()
                .map(map -> Map.entry(ResourceType.valueOf((String) map.get("key")), (Integer) map.get("value")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    @Test
    public void testGetTradeRatio() throws ReflectiveOperationException {
        HexGridImpl hexGrid = new HexGridImpl(1);
        Player player = new PlayerImpl(hexGrid, null);

        // Test default
        {
            Context context = contextBuilder()
                .add("hexGrid", "radius=1, intersections=6, no intersections/ports owned by player")
                .build();
            assertEquals(4, player.getTradeRatio(ResourceType.ORE), context, result ->
                "PlayerImpl.getTradeRatio did not return the correct value");
        }

        Field intersectionsField = HexGridImpl.class.getDeclaredField("intersections");
        intersectionsField.trySetAccessible();
        Field portField = IntersectionImpl.class.getDeclaredField("port");
        portField.trySetAccessible();
        Field settlementField = IntersectionImpl.class.getDeclaredField("settlment");
        settlementField.trySetAccessible();
        AtomicInteger i = new AtomicInteger();
        List<Port> ports = List.of(
            new Port(3),
            new Port(2, ResourceType.ORE)
        );
        ((Map<?, IntersectionImpl>) intersectionsField.get(hexGrid))
            .values()
            .stream()
            .limit(ports.size())
            .forEach(intersection -> {
                try {
                    settlementField.set(intersection, new Settlement(player, Settlement.Type.VILLAGE));
                    portField.set(intersection, ports.get(i.getAndIncrement()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        Context context = contextBuilder()
            .add("hexGrid", "radius=1, intersections=6, one port with ratio=3 and one with ratio=2 and resourceType=ORE owned by player")
            .build();

        for (Port port : ports) {
            ResourceType resourceType = port.resourceType() == null ? ResourceType.values()[0] : port.resourceType();
            assertEquals(port.ratio(),
                player.getTradeRatio(resourceType),
                contextBuilder().add(context).add("tested port", port).add("resourceType", resourceType).build(),
                result -> "PlayerImpl.getTradeRatio did not return the correct value");
        }
    }
}
