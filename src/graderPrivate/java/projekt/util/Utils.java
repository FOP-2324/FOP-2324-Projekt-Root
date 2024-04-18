package projekt.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import org.apache.commons.lang3.stream.Streams;
import org.sourcegrade.jagr.api.testing.extension.JagrExecutionCondition;
import projekt.ClassTransformer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Utils {

    public static final String PROJECT_PREFIX = "projekt";
    public static final boolean JAGR_PRESENT = !new JagrExecutionCondition().evaluateExecutionCondition(null).isDisabled();
    public static final Function<Object, Integer> AS_INTEGER = o -> (Integer) o;

    private static boolean submissionTransformed = false;

    private Utils() {}

    public static <K, V> Map<K, V> deserializeMap(List<Map<String, ?>> serializedMap, Function<Object, K> keyMapper, Function<Object, V> valueMapper) {
        return serializedMap == null ? null : serializedMap.stream()
            .collect(Collectors.toMap(map -> keyMapper.apply(map.get("key")), map -> valueMapper.apply(map.get("value"))));
    }

    public static <K extends Enum<K>, V> Map<K, V> deserializeEnumMap(List<Map<String, ?>> serializedMap, Class<K> enumClass, Function<Object, V> valueMapper) {
        Map<String, K> enums = Arrays.stream(enumClass.getEnumConstants())
            .collect(Collectors.toMap(Enum::name, Function.identity()));
        return deserializeMap(serializedMap, enums::get, valueMapper);
    }

    /**
     * Transforms this submission for use with {@link projekt.SubmissionExecutionHandler} without Jagr,
     * i.e., for plain JUnit tests.
     * Does nothing on subsequent calls or if Jagr is present.
     */
    public static void transformSubmission() {
        // skip if Jagr is present or already transformed
        if (JAGR_PRESENT || submissionTransformed)
            return;

        ByteBuddyAgent.install();
        ByteBuddy byteBuddy = new ByteBuddy();
        ClassTransformer classTransformer = new ClassTransformer(PROJECT_PREFIX);
        TransformingClassLoader transformingClassLoader = new TransformingClassLoader(PROJECT_PREFIX, classTransformer);

        List<String> classNames;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            classNames = Streams.of(objectMapper.readTree(ClassLoader.getSystemResourceAsStream("structure.json")).iterator())
                .map(jsonNode -> jsonNode.get("identifier").asText())
                .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String className : classNames) {
            try {
                byteBuddy
                    .redefine(Class.forName(className), new ClassFileLocator() {
                        @Override
                        public Resolution locate(String s) {
                            return new Resolution.Explicit(transformingClassLoader.getTransformedBytecode(s));
                        }

                        @Override
                        public void close() {}
                    })
                    .name(className)
                    .make()
                    .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        submissionTransformed = true;
    }
}
