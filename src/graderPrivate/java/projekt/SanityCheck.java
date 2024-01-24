package projekt;

import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.jupiter.json.JsonClasspathSource;
import org.opentest4j.AssertionFailedError;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class SanityCheck {

    private static final Collector<CharSequence, ?, String> JOINING_COLLECTOR = Collectors.joining(", ", "[", "]");
    private static final Map<String, Integer> MODIFIERS = Map.ofEntries(
        Map.entry("PUBLIC",       0x001),
        Map.entry("PRIVATE",      0x002),
        Map.entry("PROTECTED",    0x004),
        Map.entry("STATIC",       0x008),
        Map.entry("FINAL",        0x010),
        Map.entry("SYNCHRONIZED", 0x020),
        Map.entry("VOLATILE",     0x040),
        Map.entry("TRANSIENT",    0x080),
        Map.entry("NATIVE",       0x100),
        Map.entry("INTERFACE",    0x200),
        Map.entry("ABSTRACT",     0x400),
        Map.entry("STRICT",       0x800)
    );

    @ParameterizedTest
    @JsonClasspathSource("structure.json")
    public void test(ClassRecord classRecord) throws Throwable {
        Class<?> clazz;
        try {
             clazz = Class.forName(classRecord.identifier);
        } catch (ClassNotFoundException e) {
            throw new AssertionFailedError("Could not find class " + classRecord.identifier);
        }

        {
            int expectedModifiers = classRecord.modifiers
                .stream()
                .mapToInt(MODIFIERS::get)
                .sum();
            assertEquals(expectedModifiers, clazz.getModifiers() & expectedModifiers, emptyContext(), result ->
                "Incorrect modifiers - expected: %s, actual: %s".formatted(expandModifiers(expectedModifiers), expandModifiers(result.object())));
        }

        assertEquals(classRecord.superclass, clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "", emptyContext(), result ->
            "Class %s does not have correct superclass".formatted(classRecord.identifier));

        List<String> expectedInterfaces = classRecord.interfaces;
        Set<String> actualInterfaces = Arrays.stream(clazz.getInterfaces())
            .map(Class::getName)
            .collect(Collectors.toSet());
        assertTrue(actualInterfaces.containsAll(expectedInterfaces), emptyContext(), result ->
            "Class %s does not implement all required interfaces. Missing: %s".formatted(clazz.getName(),
                expectedInterfaces.stream().filter(interfaceName -> !actualInterfaces.contains(interfaceName)).collect(Collectors.toSet())));

        List<Field> actualFields = List.of(clazz.getDeclaredFields());
        for (FieldRecord fieldRecord : classRecord.fields) {
            Field field = actualFields.stream()
                .filter(f -> f.getName().equals(fieldRecord.identifier))
                .findAny()
                .orElseThrow(() -> fail(emptyContext(), result ->
                    "Field %s does not exist in class %s".formatted(fieldRecord.identifier, classRecord.identifier)));

            int expectedModifiers = fieldRecord.modifiers
                .stream()
                .mapToInt(MODIFIERS::get)
                .sum();
            assertEquals(expectedModifiers, field.getModifiers() & expectedModifiers, emptyContext(), result ->
                "Incorrect modifiers for field %s in class %s - expected: %s, actual: %s"
                    .formatted(fieldRecord.identifier, classRecord.identifier, expandModifiers(expectedModifiers), expandModifiers(result.object())));

            assertEquals(fieldRecord.type, field.getType().getName(), emptyContext(), result ->
                "Field %s in class %s does not have the expected type".formatted(fieldRecord.identifier, classRecord.identifier));
        }

        List<Method> actualMethods = Stream.of(clazz.getDeclaredMethods())
            .filter(m -> !m.getName().startsWith("lambda$") && !m.isSynthetic())
            .toList();
        for (MethodRecord methodRecord : classRecord.methods) {
            String parameterString = String.join(", ", methodRecord.parameterTypes);
            Method method = actualMethods.stream()
                .filter(m ->
                    m.getName().equals(methodRecord.identifier) &&
                    m.getParameterCount() == methodRecord.parameterTypes.size() &&
                    Arrays.stream(m.getParameterTypes()).map(Class::getName).toList().containsAll(methodRecord.parameterTypes))
                .findAny()
                .orElseThrow(() -> fail(emptyContext(), result ->
                    "Method %s(%s) does not exist in class %s"
                        .formatted(methodRecord.identifier, parameterString, classRecord.identifier)));

            int expectedModifiers = methodRecord.modifiers
                .stream()
                .mapToInt(MODIFIERS::get)
                .sum();
            assertEquals(expectedModifiers, method.getModifiers() & expectedModifiers, emptyContext(), result ->
                "Incorrect modifiers for method %s(%s) in class %s - expected: %s, actual: %s"
                    .formatted(methodRecord.identifier, parameterString, classRecord.identifier, expandModifiers(expectedModifiers), expandModifiers(result.object())));

            assertEquals(methodRecord.returnType, method.getReturnType().getName(), emptyContext(), result ->
                "Method %s(%s) in class %s does not have the expected return type"
                    .formatted(methodRecord.identifier, parameterString, classRecord.identifier));
        }
    }

    public String generateJson() {
        return Stream.of(
                "projekt",
                "projekt.controller",
                "projekt.controller.tiles",
                "projekt.model",
                "projekt.model.buildings",
                "projekt.model.tiles",
                "projekt.view",
                "projekt.view.menus",
                "projekt.view.tiles"
            )
            .flatMap(packageName ->
                new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"))))
                    .lines()
                    .filter(s -> s.endsWith(".class"))
                    .map(className -> getClass(packageName, className))
                    .filter(Objects::nonNull)
                    .map(SanityCheck::serializeClass))
            .collect(Collectors.joining(",\n", "[\n", "\n]"));
    }

    private static Class<?> getClass(String packageName, String className) {
        try {
            return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String expandModifiers(int modifiers) {
        String[] modifierNames = MODIFIERS.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .toArray(String[]::new);

        return IntStream.range(0, modifierNames.length)
            .filter(i -> (1 << i & modifiers) != 0)
            .mapToObj(i -> "\"%s\"".formatted(modifierNames[i]))
            .collect(JOINING_COLLECTOR);
    }

    private static String serializeClass(Class<?> clazz) {
        return """
                {
                    "modifiers": %s,
                    "identifier": "%s",
                    "superclass": "%s",
                    "interfaces": %s,
                    "fields": [%s],
                    "methods": [%s]
                }
            """.formatted(
                expandModifiers(clazz.getModifiers()),
                clazz.getName(),
                clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "",
                Arrays.stream(clazz.getInterfaces())
                    .map(c -> "\"%s\"".formatted(c.getName()))
                    .collect(JOINING_COLLECTOR),
                Arrays.stream(clazz.getDeclaredFields())
                    .map(SanityCheck::serializeField)
                    .collect(Collectors.joining(", ")),
                Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> !m.getName().startsWith("lambda$") && !m.isSynthetic())
                    .map(SanityCheck::serializeMethod)
                    .collect(Collectors.joining(", "))
            )
            .replaceAll("\n$", "");
    }

    private static String serializeField(Field field) {
        return """
            {
                "modifiers": %s,
                "identifier": "%s",
                "type": "%s"
            }""".formatted(
                expandModifiers(field.getModifiers()),
                field.getName(),
                field.getType().getName()
            )
            .replaceAll(",\n", ", \n")
            .replaceAll("\n\\s*", "");
    }

    private static String serializeMethod(Method method) {
        return """
            {
                "modifiers": %s,
                "identifier": "%s",
                "returnType": "%s",
                "parameterTypes": %s
            }""".formatted(
                expandModifiers(method.getModifiers()),
                method.getName(),
                method.getReturnType().getName(),
                Arrays.stream(method.getParameterTypes())
                    .map(c -> "\"%s\"".formatted(c.getName()))
                    .collect(JOINING_COLLECTOR)
            )
            .replaceAll(",\n", ", \n")
            .replaceAll("\n\\s*", "");
    }

    public record ClassRecord(
        List<String> modifiers,
        String identifier,
        String superclass,
        List<String> interfaces,
        List<FieldRecord> fields,
        List<MethodRecord> methods
    ) {}

    public record FieldRecord(
        List<String> modifiers,
        String identifier,
        String type
    ) {}

    public record MethodRecord(
        List<String> modifiers,
        String identifier,
        String returnType,
        List<String> parameterTypes
    ) {}
}
