package projekt;

import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class SubmissionExecutionHandler {

    public static final String INTERNAL_NAME = Type.getInternalName(SubmissionExecutionHandler.class);

    private static SubmissionExecutionHandler instance;

    private final Map<String, Map<String, List<Invocation>>> methodInvocations = new HashMap<>();
    private final Map<String, Map<String, Boolean>> methodDelegationWhitelist = new HashMap<>();

    private SubmissionExecutionHandler() {}

    public static SubmissionExecutionHandler getInstance() {
        if (instance == null) {
            instance = new SubmissionExecutionHandler();
        }
        return instance;
    }

    // Method invocations

    public void resetMethodInvocationLogging() {
        methodInvocations.clear();
    }

    public void enableMethodInvocationLogging(Method method) {
        enableMethodInvocationLogging(Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
    }

    public void enableMethodInvocationLogging(String className, String name, String descriptor) {
        methodInvocations.putIfAbsent(className, new HashMap<>());
        methodInvocations.get(className).putIfAbsent(name + descriptor, new ArrayList<>());
    }

    public List<Invocation> getInvocationsForMethod(Method method) {
        return getInvocationsForMethod(Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
    }

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
     * @return {@code true} if invocation logging is enabled for the caller, otherwise {@code false}
     */
    public boolean logInvocation(String className, String name, String descriptor) {
        return methodInvocations.getOrDefault(className, Collections.emptyMap()).get(name + descriptor) != null;
    }

    /**
     * Adds an invocation to the list of invocations for the calling method.
     * Must only be used in bytecode transformations when intercepting method invocations.
     *
     * @param className  the declaring class' name
     * @param name       the method's name
     * @param descriptor the method's descriptor
     * @param invocation the invocation on the method, i.e. the parameters it has been called with
     */
    public void addInvocation(String className, String name, String descriptor, Invocation invocation) {
        List<Invocation> invocations = methodInvocations.getOrDefault(className, Collections.emptyMap()).get(name + descriptor);
        if (invocations == null) {
            throw new IllegalStateException("Invocation logging was not enabled for the calling method. owner: %s, name: %s, descriptor: %s".formatted(className, name, descriptor));
        } else {
            invocations.add(invocation);
        }
    }

    // Method delegation

    public void resetMethodDelegation() {
        methodDelegationWhitelist.clear();
    }

    public void disableMethodDelegation(Method method) {
        disableMethodDelegation(Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
    }

    public void disableMethodDelegation(String className, String name, String descriptor) {
        methodDelegationWhitelist.putIfAbsent(className, new HashMap<>());
        methodDelegationWhitelist.get(className).put(name + descriptor, true);
    }

    public boolean useStudentImpl(String className, String name, String descriptor) {
        return methodDelegationWhitelist.getOrDefault(className, Collections.emptyMap())
            .getOrDefault(name + descriptor, false);
    }

    @SuppressWarnings("unchecked")
    public static class Invocation {

        public static final String INTERNAL_NAME = Type.getInternalName(Invocation.class);

        private final List<Object> parameterValues = new ArrayList<>();

        public List<Object> getParameters() {
            return Collections.unmodifiableList(parameterValues);
        }

        public <T> T getParameter(int index) {
            return (T) parameterValues.get(index);
        }

        public <T> T getParameter(int index, Class<T> clazz) {
            return clazz.cast(parameterValues.get(index));
        }

        public boolean getBooleanParameter(int index) {
            return getParameter(index, boolean.class);
        }

        public byte getByteParameter(int index) {
            return getParameter(index, byte.class);
        }

        public short getShortParameter(int index) {
            return getParameter(index, short.class);
        }

        public char getCharParameter(int index) {
            return getParameter(index, char.class);
        }

        public int getIntParameter(int index) {
            return getParameter(index, int.class);
        }

        public long getLongParameter(int index) {
            return getParameter(index, long.class);
        }

        public float getFloatParameter(int index) {
            return getParameter(index, float.class);
        }

        public double getDoubleParameter(int index) {
            return getParameter(index, double.class);
        }

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
}
