package projekt;

import kotlin.Pair;
import org.objectweb.asm.Type;
import projekt.util.Utils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A singleton class to configure the way a submission is executed.
 * This class can be used to
 * <ul>
 *     <li>log method invocations</li>
 *     <li>delegate invocations to the solution / a pre-defined external class</li>
 *     <li>delegate invocations to a custom programmatically-defined method (e.g. lambdas)</li>
 * </ul>
 * By default, all method calls are delegated to the solution.
 * To call the real method, delegation must be disabled before calling it.
 * This can be done either explicitly using {@link #disableMethodDelegation} or implicitly using
 * {@link #substituteMethod}.
 * <br>
 * To use any of these features, the submission classes need to be transformed using bytecode transformation,
 * either using Jagr's mechanism or {@link Utils#transformSubmission()}.
 * <br><br>
 * An example test class could look like this:
 * <pre>
 * {@code
 * public class ExampleTest {
 *
 *     private final SubmissionExecutionHandler executionHandler = SubmissionExecutionHandler.getInstance();
 *
 *     @BeforeAll
 *     public static void start() {
 *         Utils.transformSubmission(); // In case Jagr is not present
 *     }
 *
 *     @BeforeEach
 *     public void setup() {
 *         // Pre-test setup, if necessary. Useful for substitution:
 *         Method substitutedMethod = TestedClass.class.getDeclaredMethod("dependencyForTest");
 *         executionHandler.substituteMethod(substitutedMethod, invocation -> "Hello world!");
 *     }
 *
 *     @AfterEach
 *     public void reset() {
 *         // Optionally reset invocation logs, substitutions, etc.
 *         executionHandler.resetMethodInvocationLogging();
 *         executionHandler.resetMethodDelegation();
 *         executionHandler.resetMethodSubstitution();
 *     }
 *
 *     @Test
 *     public void test() throws ReflectiveOperationException {
 *         Method method = TestedClass.class.getDeclaredMethod("methodUnderTest");
 *         executionHandler.disableDelegation(method); // Disable delegation, i.e., use the original implementation
 *         ...
 *     }
 * }
 * }
 * </pre>
 *
 * @see ClassTransformer
 */
@SuppressWarnings("unused")
public class SubmissionExecutionHandler {

    /**
     * The internal name of this class. Only relevant for {@link ClassTransformer}.
     */
    public static final String INTERNAL_NAME = Type.getInternalName(SubmissionExecutionHandler.class);

    private static SubmissionExecutionHandler instance;

    private final Map<String, Map<String, List<Invocation>>> methodInvocations = new HashMap<>();
    private final Map<String, Map<String, Boolean>> methodDelegationWhitelist = new HashMap<>();
    private final Map<String, Map<String, MethodSubstitution>> methodSubstitutions = new HashMap<>();

    private SubmissionExecutionHandler() {}

    /**
     * Returns the global {@link SubmissionExecutionHandler} instance.
     *
     * @return the global {@link SubmissionExecutionHandler} instance
     */
    public static SubmissionExecutionHandler getInstance() {
        if (instance == null) {
            instance = new SubmissionExecutionHandler();
        }
        return instance;
    }

    // Method invocations

    /**
     * Resets the logging of method invocations to log no invocations.
     */
    public void resetMethodInvocationLogging() {
        methodInvocations.clear();
    }

    /**
     * Enables logging of method invocations for the given method.
     *
     * @param method the method to enable invocation logging for
     */
    public void enableMethodInvocationLogging(Method method) {
        enableMethodInvocationLogging(Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
    }

    /**
     * Enables logging of method invocations for the given method.
     * This method should only be used when one has no access to the method at compile-time.
     *
     * @param className  the internal name of the method's owner (see {@link Type#getInternalName()})
     * @param name       the name of the method ({@code <init>} for constructors)
     * @param descriptor the method's descriptor (see JVM specification)
     */
    public void enableMethodInvocationLogging(String className, String name, String descriptor) {
        methodInvocations.putIfAbsent(className, new HashMap<>());
        methodInvocations.get(className).putIfAbsent(name + descriptor, new ArrayList<>());
    }

    /**
     * Returns all logged invocations for the given method.
     *
     * @param method the method to get invocations of
     * @return a list of invocations on the given method
     */
    public List<Invocation> getInvocationsForMethod(Method method) {
        return getInvocationsForMethod(Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
    }

    /**
     * Returns all logged invocations for the given method.
     * This method should only be used when one has no access to the method at compile-time.
     *
     * @param className  the internal name of the method's owner (see {@link Type#getInternalName()})
     * @param name       the name of the method ({@code <init>} for constructors)
     * @param descriptor the method's descriptor (see JVM specification)
     * @return a list of invocations on the given method
     */
    public List<Invocation> getInvocationsForMethod(String className, String name, String descriptor) {
        Optional<List<Invocation>> invocations = Optional.ofNullable(methodInvocations.getOrDefault(className, Collections.emptyMap()).get(name + descriptor));
        return invocations.map(Collections::unmodifiableList).orElse(null);
    }

    /**
     * Returns whether the calling method's invocation is logged, i.e.
     * {@link #addInvocation(String, String, String, Invocation)} may be called or not.
     * Should only be used in bytecode transformations when intercepting method invocations.
     *
     * @param className  the declaring class' name
     * @param name       the method's name
     * @param descriptor the method's descriptor
     * @return {@code true} if invocation logging is enabled for the given method, otherwise {@code false}
     */
    public boolean logInvocation(String className, String name, String descriptor) {
        return methodInvocations.getOrDefault(className, Collections.emptyMap()).get(name + descriptor) != null;
    }

    /**
     * Adds an invocation to the list of invocations for the calling method.
     * Should only be used in bytecode transformations when intercepting method invocations.
     *
     * @param className  the declaring class' name
     * @param name       the method's name
     * @param descriptor the method's descriptor
     * @param invocation the invocation on the method, i.e. the context it has been called with
     */
    public void addInvocation(String className, String name, String descriptor, Invocation invocation) {
        List<Invocation> invocations = methodInvocations.getOrDefault(className, Collections.emptyMap()).get(name + descriptor);
        if (invocations == null) {
            throw new IllegalStateException("Invocation logging was not enabled for the calling method. owner: %s, name: %s, descriptor: %s".formatted(className, name, descriptor));
        } else {
            invocations.add(invocation);
        }
    }

    // Method delegation and substitution

    /**
     * Resets the delegation of methods.
     */
    public void resetMethodDelegation() {
        methodDelegationWhitelist.clear();
    }

    /**
     * Resets the substitution of methods.
     */
    public void resetMethodSubstitution() {
        methodSubstitutions.clear();
    }

    /**
     * Disables delegation to the solution for the given method.
     *
     * @param method the method to disable delegation for
     */
    public void disableMethodDelegation(Method method) {
        disableMethodDelegation(Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
    }

    /**
     * Disables delegation to the solution for the given method.
     * This method should only be used when one has no access to the method at compile-time.
     *
     * @param className  the internal name of the method's owner (see {@link Type#getInternalName()})
     * @param name       the name of the method ({@code <init>} for constructors)
     * @param descriptor the method's descriptor (see JVM specification)
     */
    public void disableMethodDelegation(String className, String name, String descriptor) {
        methodDelegationWhitelist.putIfAbsent(className, new HashMap<>());
        methodDelegationWhitelist.get(className).put(name + descriptor, true);
    }

    /**
     * Substitute calls to the given method with the invocation of the given {@link MethodSubstitution}.
     * In other words, instead of executing the instructions of either the original submission or the solution,
     * this can be used to make the method do and return anything during runtime.
     *
     * @param method     the method to substitute
     * @param substitute the {@link MethodSubstitution} the method will be substituted with
     */
    public void substituteMethod(Method method, MethodSubstitution substitute) {
        substituteMethod(Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method), substitute);
    }

    /**
     * Substitute calls to the given method with the invocation of the given {@link MethodSubstitution}.
     * In other words, instead of executing the instructions of either the original submission or the solution,
     * this can be used to make the method do and return anything during runtime.
     * This method should only be used when one has no access to the method at compile-time.
     *
     * @param className  the internal name of the method's owner (see {@link Type#getInternalName()})
     * @param name       the name of the method ({@code <init>} for constructors)
     * @param descriptor the method's descriptor (see JVM specification)
     * @param substitute the {@link MethodSubstitution} the method will be substituted with
     */
    public void substituteMethod(String className, String name, String descriptor, MethodSubstitution substitute) {
        methodSubstitutions.putIfAbsent(className, new HashMap<>());
        methodSubstitutions.get(className).put(name + descriptor, substitute);
    }

    /**
     * Returns whether the original instructions are used or not.
     * Should only be used in bytecode transformations when intercepting method invocations.
     *
     * @param className  the declaring class' name
     * @param name       the method's name
     * @param descriptor the method's descriptor
     * @return {@code true} if delegation is disabled for the given method, otherwise {@code false}
     */
    public boolean useStudentImpl(String className, String name, String descriptor) {
        return methodDelegationWhitelist.getOrDefault(className, Collections.emptyMap())
            .getOrDefault(name + descriptor, false);
    }

    /**
     * Returns whether the given method has a substitute or not.
     * Should only be used in bytecode transformations when intercepting method invocations.
     *
     * @param className  the declaring class' name
     * @param name       the method's name
     * @param descriptor the method's descriptor
     * @return {@code true} if substitution is enabled for the given method, otherwise {@code false}
     */
    public boolean useSubstitution(String className, String name, String descriptor) {
        return methodSubstitutions.getOrDefault(className, Collections.emptyMap()).containsKey(name + descriptor);
    }

    /**
     * Returns the substitute for the given method.
     * Should only be used in bytecode transformations when intercepting method invocations.
     *
     * @param className  the declaring class' name
     * @param name       the method's name
     * @param descriptor the method's descriptor
     * @return the substitute for the given method
     */
    public MethodSubstitution getSubstitution(String className, String name, String descriptor) {
        return methodSubstitutions.get(className).get(name + descriptor);
    }

    /**
     * This class holds information about the context of an invocation.
     * Context means the object a method was invoked on and the parameters it was invoked with.
     */
    @SuppressWarnings("unchecked")
    public static class Invocation {

        /**
         * The internal name of this class. Only relevant for {@link ClassTransformer}.
         */
        public static final String INTERNAL_NAME = Type.getInternalName(Invocation.class);

        private final Object instance;
        private final List<Object> parameterValues = new ArrayList<>();

        /**
         * Constructs a new invocation.
         */
        public Invocation() {
            this(null);
        }

        /**
         * Constructs a new invocation.
         *
         * @param instance the object on which this invocation takes place
         */
        public Invocation(Object instance) {
            this.instance = instance;
        }

        /**
         * Returns the object the method was invoked on.
         *
         * @return the object the method was invoked on.
         */
        public Object getInstance() {
            return instance;
        }

        /**
         * Returns the list of parameter values the method was invoked with.
         *
         * @return the list of parameter values the method was invoked with.
         */
        public List<Object> getParameters() {
            return Collections.unmodifiableList(parameterValues);
        }

        /**
         * Returns the value of the parameter at the given index.
         *
         * @param index the parameter's index
         * @return the parameter value
         */
        public <T> T getParameter(int index) {
            return (T) parameterValues.get(index);
        }

        /**
         * Returns the value of the parameter at the given index, cast to the given class.
         *
         * @param index the parameter's index
         * @param clazz the class the value will be cast to
         * @return the parameter value, cast to the given class
         */
        public <T> T getParameter(int index, Class<T> clazz) {
            return clazz.cast(parameterValues.get(index));
        }

        /**
         * Returns the value of the {@code boolean} parameter at the given index.
         *
         * @param index the parameter's index
         * @return the parameter value
         */
        public boolean getBooleanParameter(int index) {
            return getParameter(index, Boolean.class);
        }

        /**
         * Returns the value of the {@code byte} parameter at the given index.
         *
         * @param index the parameter's index
         * @return the parameter value
         */
        public byte getByteParameter(int index) {
            return getParameter(index, Byte.class);
        }

        /**
         * Returns the value of the {@code short} parameter at the given index.
         *
         * @param index the parameter's index
         * @return the parameter value
         */
        public short getShortParameter(int index) {
            return getParameter(index, Short.class);
        }

        /**
         * Returns the value of the {@code char} parameter at the given index.
         *
         * @param index the parameter's index
         * @return the parameter value
         */
        public char getCharParameter(int index) {
            return getParameter(index, Character.class);
        }

        /**
         * Returns the value of the {@code int} parameter at the given index.
         *
         * @param index the parameter's index
         * @return the parameter value
         */
        public int getIntParameter(int index) {
            return getParameter(index, Integer.class);
        }

        /**
         * Returns the value of the {@code long} parameter at the given index.
         *
         * @param index the parameter's index
         * @return the parameter value
         */
        public long getLongParameter(int index) {
            return getParameter(index, Long.class);
        }

        /**
         * Returns the value of the {@code float} parameter at the given index.
         *
         * @param index the parameter's index
         * @return the parameter value
         */
        public float getFloatParameter(int index) {
            return getParameter(index, Float.class);
        }

        /**
         * Returns the value of the {@code double} parameter at the given index.
         *
         * @param index the parameter's index
         * @return the parameter value
         */
        public double getDoubleParameter(int index) {
            return getParameter(index, Double.class);
        }

        /**
         * Adds a parameter value to the list of values.
         *
         * @param value the value to add
         */
        public void addParameter(Object value) {
            parameterValues.add(value);
        }

        @Override
        public String toString() {
            return "Invocation{parameterValues=%s}".formatted(parameterValues);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Invocation that = (Invocation) o;
            return Objects.equals(parameterValues, that.parameterValues);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parameterValues);
        }
    }

    public static class Field<T> implements Supplier<T> {

        public static final String INTERNAL_NAME = Type.getInternalName(Field.class);

        private final String originalDescriptor;
        private final String originalSignature;
        private Object value;
        private Function<Object, ? extends T> valueMapper;

        public Field(String originalDescriptor, String originalSignature) {
            this(originalDescriptor, originalSignature, null);
        }

        public Field(String originalDescriptor, String originalSignature, Object initialValue) {
            this(originalDescriptor, originalSignature, initialValue, o -> (T) o);
        }

        public Field(String originalDescriptor, String originalSignature, Object initialValue, Function<Object, ? extends T> valueMapper) {
            this.originalDescriptor = originalDescriptor;
            this.originalSignature = originalSignature;
            this.value = initialValue;
            this.valueMapper = valueMapper;
        }

        public String getOriginalDescriptor() {
            return originalDescriptor;
        }

        public String getOriginalSignature() {
            return originalSignature;
        }

        public Function<Object, ? extends T> getValueMapper() {
            return valueMapper;
        }

        public void setValueMapper(Function<Object, ? extends T> valueMapper) {
            this.valueMapper = valueMapper;
        }

        /**
         * Returns the current value of this field.ce
         *
         * @return the current value of this field
         */
        @Override
        public T get() {
            if (!originalDescriptor.startsWith("L") && value == null) {
                return valueMapper.apply(switch (originalDescriptor) {
                    case "Z" -> false;
                    case "B", "S", "C", "I" -> 0;
                    case "F" -> 0F;
                    case "J" -> 0L;
                    case "D" -> 0D;
                    default -> null;
                });
            }
            return valueMapper.apply(value);
        }

        /**
         * Sets the current value of this field to the specified one.
         *
         * @param value the new value for this field
         */
        public void set(T value) {
            this.value = value;
        }
    }

    /**
     * This functional interface represents a substitution for a method.
     * The functional method {@link #execute(Invocation)} is called with the original invocation's context.
     * Its return value is also the value that will be returned by the substituted method.
     */
    @FunctionalInterface
    public interface MethodSubstitution {

        String INTERNAL_NAME = Type.getInternalName(MethodSubstitution.class);

        /**
         * DO NOT USE, THIS METHOD HAS NO EFFECT RIGHT NOW.
         * TODO: implement constructor substitution
         * <br><br>
         * Defines the behaviour of method substitution when the substituted method is a constructor.
         * When a constructor method is substituted, either {@code super(...)} or {@code this(...)} must be called
         * before calling {@link #execute(Invocation)}.
         * This method returns a pair consisting of...
         * <ol>
         *     <li>the internal class name / owner of the target constructor and</li>
         *     <li>the values that are passed to the constructor of that class.</li>
         * </ol>
         * The first pair entry must be either the original method's owner (for {@code this(...)}) or
         * the superclass (for {@code super(...)}).
         * The second entry is an array of parameter values for that constructor.
         * Default behaviour assumes calling the constructor of {@link Object}, i.e., a class that has no superclass.
         *
         * @return a pair containing the target method's owner and arguments
         */
        default Pair<String, Object[]> constructorBehaviour() {
            return new Pair<>("java/lang/Object", new Object[0]);
        }

        /**
         * Defines the actions of the substituted method.
         *
         * @param invocation the context of an invocation
         * @return the return value of the substituted method
         */
        Object execute(Invocation invocation);
    }
}
