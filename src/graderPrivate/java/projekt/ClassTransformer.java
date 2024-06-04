package projekt;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;
import projekt.SubmissionExecutionHandler.Invocation;
import projekt.SubmissionExecutionHandler.MethodSubstitution;

import java.io.InputStream;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class ClassTransformer implements org.sourcegrade.jagr.api.testing.ClassTransformer {

    private final Map<String, Map<String, MethodNode>> methodNodes = new HashMap<>();
    private final String projectPrefix;

    public ClassTransformer(String projectPrefix) {
        this.projectPrefix = projectPrefix;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getWriterFlags() {
        return ClassWriter.COMPUTE_MAXS;
    }

    @Override
    public void transform(ClassReader reader, ClassWriter writer) {
        ClassReader solutionClassReader;
        try (InputStream is = getClass().getResourceAsStream("/classes/" + reader.getClassName() + ".bin")) {
            solutionClassReader = new ClassReader(is.readAllBytes());
        } catch (Exception e) {
            System.err.printf("Could not load solution for class %s. Using original class instead. Exception: %s%n", reader.getClassName(), e);
            reader.accept(writer, 0);
            return;
        }
        solutionClassReader.accept(new SolutionClassVisitor(reader.getClassName()), 0);
        reader.accept(new SubmissionClassVisitor(writer, reader.getClassName()), 0);
    }

    private class SubmissionClassVisitor extends ClassVisitor {

        private final String className;
        private final Map<String, MethodNode> classMethodNodes;
        private final List<String> visitedMethods = new ArrayList<>();

        protected SubmissionClassVisitor(ClassVisitor classVisitor, String className) {
            super(ASM9, classVisitor);
            this.className = className;
            this.classMethodNodes = methodNodes.get(className);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            String nameDescriptor = name + descriptor;
            visitedMethods.add(nameDescriptor);

            return new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                @Override
                public void visitCode() {
                    // if method is not abstract and a MethodNode for the current method exists
                    if ((access & ACC_ABSTRACT) == 0 && classMethodNodes.containsKey(nameDescriptor)) {
                        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
                        Label substitutionCheckLabel = new Label();
                        Label delegationCheckLabel = new Label();
                        Label submissionCodeLabel = new Label();

                        super.visitMethodInsn(INVOKESTATIC,
                            SubmissionExecutionHandler.INTERNAL_NAME,
                            "getInstance",
                            "()L" + SubmissionExecutionHandler.INTERNAL_NAME + ";",
                            false);

                        // check if invocation should be logged
                        super.visitInsn(DUP);
                        super.visitLdcInsn(className);
                        super.visitLdcInsn(name);
                        super.visitLdcInsn(descriptor);
                        super.visitMethodInsn(INVOKEVIRTUAL,
                            SubmissionExecutionHandler.INTERNAL_NAME,
                            "logInvocation",
                            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z",
                            false);
                        super.visitJumpInsn(IFEQ, name.equals("<init>") ? delegationCheckLabel : substitutionCheckLabel); // jump to label if logInvocation(...) == false
                        // intercept parameters
                        super.visitInsn(DUP); // duplicate SubmissionExecutionHandler reference
                        super.visitLdcInsn(className);
                        super.visitLdcInsn(name);
                        super.visitLdcInsn(descriptor);
                        buildInvocation(argumentTypes);
                        super.visitMethodInsn(INVOKEVIRTUAL,
                            SubmissionExecutionHandler.INTERNAL_NAME,
                            "addInvocation",
                            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;L" + Invocation.INTERNAL_NAME + ";)V",
                            false);

                        // check if substitution exists for this method if not constructor (because waaay too complex right now)
                        if (!name.equals("<init>")) {
                            super.visitFrame(F_SAME1, 0, null, 1, new Object[]{SubmissionExecutionHandler.INTERNAL_NAME});
                            super.visitLabel(substitutionCheckLabel);
                            super.visitInsn(DUP);
                            super.visitLdcInsn(className);
                            super.visitLdcInsn(name);
                            super.visitLdcInsn(descriptor);
                            super.visitMethodInsn(INVOKEVIRTUAL,
                                SubmissionExecutionHandler.INTERNAL_NAME,
                                "useSubstitution",
                                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z",
                                false);
                            super.visitJumpInsn(IFEQ, delegationCheckLabel); // jump to label if useSubstitution(...) == false
                            super.visitLdcInsn(className);
                            super.visitLdcInsn(name);
                            super.visitLdcInsn(descriptor);
                            super.visitMethodInsn(INVOKEVIRTUAL,
                                SubmissionExecutionHandler.INTERNAL_NAME,
                                "getSubstitution",
                                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)L" + MethodSubstitution.INTERNAL_NAME + ";",
                                false);
                            buildInvocation(argumentTypes);
                            super.visitMethodInsn(INVOKEINTERFACE,
                                MethodSubstitution.INTERNAL_NAME,
                                "execute",
                                "(L" + Invocation.INTERNAL_NAME + ";)Ljava/lang/Object;",
                                true);
                            Type returnType = Type.getReturnType(descriptor);
                            if (returnType.getSort() == Type.ARRAY || returnType.getSort() == Type.OBJECT) {
                                super.visitTypeInsn(CHECKCAST, returnType.getInternalName());
                            } else {
                                unboxType(getDelegate(), returnType);
                            }
                            super.visitInsn(returnType.getOpcode(IRETURN));
                        }

                        // else check if call should be delegated to solution or not
                        super.visitFrame(F_SAME1, 0, null, 1, new Object[] {SubmissionExecutionHandler.INTERNAL_NAME});
                        super.visitLabel(delegationCheckLabel);
                        super.visitLdcInsn(className);
                        super.visitLdcInsn(name);
                        super.visitLdcInsn(descriptor);
                        super.visitMethodInsn(INVOKEVIRTUAL,
                            SubmissionExecutionHandler.INTERNAL_NAME,
                            "useStudentImpl",
                            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z",
                            false);
                        super.visitJumpInsn(IFNE, submissionCodeLabel); // jump to label if useStudentImpl(...) == true
                        classMethodNodes.get(nameDescriptor).accept(getDelegate()); // replay instructions from solution

                        // calculate the frame for the beginning of the submission code
                        Object[] parameterTypes = Arrays.stream(argumentTypes)
                            .map(type -> switch (type.getSort()) {
                                case Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.CHAR, Type.INT -> INTEGER;
                                case Type.FLOAT -> FLOAT;
                                case Type.LONG -> LONG;
                                case Type.DOUBLE -> DOUBLE;
                                default -> type.getInternalName();
                            })
                            .toArray();
                        if ((access & ACC_STATIC) == 0) { // if method is not static
                            Object[] types = new Object[parameterTypes.length + 1];
                            types[0] = name.equals("<init>") ? UNINITIALIZED_THIS : className;
                            System.arraycopy(parameterTypes, 0, types, 1, parameterTypes.length);
                            super.visitFrame(F_FULL, types.length, types, 0, null);
                        } else {
                            super.visitFrame(F_FULL, parameterTypes.length, parameterTypes, 0, null);
                        }
                        super.visitLabel(submissionCodeLabel);
                        // else execute original code
                    }
                    super.visitCode();
                }

                private void buildInvocation(Type[] argumentTypes) {
                    super.visitTypeInsn(NEW, Invocation.INTERNAL_NAME);
                    super.visitInsn(DUP);
                    if ((access & ACC_STATIC) == 0 && !name.equals("<init>")) {
                        super.visitVarInsn(ALOAD, 0);
                        super.visitMethodInsn(INVOKESPECIAL, Invocation.INTERNAL_NAME, "<init>", "(Ljava/lang/Object;)V", false);
                    } else {
                        super.visitMethodInsn(INVOKESPECIAL, Invocation.INTERNAL_NAME, "<init>", "()V", false);
                    }
                    for (int i = 0; i < argumentTypes.length; i++) {
                        super.visitInsn(DUP);
                        // load parameter with opcode (ALOAD, ILOAD, etc.) for type and ignore "this", if it exists
                        super.visitVarInsn(argumentTypes[i].getOpcode(ILOAD), getLocalsIndex(argumentTypes, i) + ((access & ACC_STATIC) == 0 ? 1 : 0));
                        boxType(getDelegate(), argumentTypes[i]);
                        super.visitMethodInsn(INVOKEVIRTUAL,
                            Invocation.INTERNAL_NAME,
                            "addParameter",
                            "(Ljava/lang/Object;)V",
                            false);
                    }
                }
            };
        }

        @Override
        public void visitEnd() {
            // add missing methods (including lambdas)
            classMethodNodes.entrySet()
                .stream()
                .filter(entry -> !visitedMethods.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .forEach(methodNode -> methodNode.accept(getDelegate()));
            super.visitEnd();
        }

        /**
         * Automatically box primitive types using the supplied {@link MethodVisitor}.
         * If the given type is not a primitive type, this method does nothing.
         *
         * @param mv   the {@link MethodVisitor} to use
         * @param type the type of the value
         */
        private static void boxType(MethodVisitor mv, Type type) {
            switch (type.getSort()) {
                case Type.BOOLEAN -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                case Type.BYTE -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                case Type.SHORT -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                case Type.CHAR -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                case Type.INT -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                case Type.FLOAT -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                case Type.LONG -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                case Type.DOUBLE -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            }
        }

        /**
         * Automatically unbox primitive types using the supplied {@link MethodVisitor}.
         * If the given type is not a primitive type, this method does nothing.
         *
         * @param mv   the {@link MethodVisitor} to use
         * @param type the type of the value
         */
        private static void unboxType(MethodVisitor mv, Type type) {
            switch (type.getSort()) {
                case Type.BOOLEAN -> {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                }
                case Type.BYTE -> {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
                }
                case Type.SHORT -> {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
                }
                case Type.CHAR -> {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                }
                case Type.INT -> {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                }
                case Type.FLOAT -> {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                }
                case Type.LONG -> {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                }
                case Type.DOUBLE -> {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                }
            }
        }

        /**
         * Calculates the true index of variables in the locals array.
         * Variables with type long or double occupy two slots in the locals array,
         * so the "expected" or "natural" index of these variables might be shifted.
         *
         * @param types the parameter types
         * @param index the "natural" index of the variable
         * @return the true index
         */
        private static int getLocalsIndex(Type[] types, int index) {
            int localsIndex = 0;
            for (int i = 0; i < index; i++) {
                localsIndex += (types[i].getSort() == Type.LONG || types[i].getSort() == Type.DOUBLE) ? 2 : 1;
            }
            return localsIndex;
        }
    }

    private class SolutionClassVisitor extends ClassVisitor {

        private final String className;

        protected SolutionClassVisitor(String className) {
            super(ASM9);
            this.className = className;
            methodNodes.putIfAbsent(className, new HashMap<>());
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodNode methodNode;
            String nameDescriptor;

            if ((access & ACC_SYNTHETIC) != 0 && name.startsWith("lambda$")) {  // if method is lambda
                methodNode = getMethodNode(access, name + "$solution", descriptor, signature, exceptions);
                nameDescriptor = name + "$solution" + descriptor;
            } else {
                methodNode = getMethodNode(access, name, descriptor, signature, exceptions);
                nameDescriptor = name + descriptor;
            }
            methodNodes.get(className).putIfAbsent(nameDescriptor, methodNode);
            return methodNode;
        }

        private MethodNode getMethodNode(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodNode(ASM9, access, name, descriptor, signature, exceptions) {
                @Override
                public void visitMethodInsn(int opcodeAndSource, String owner, String name, String descriptor, boolean isInterface) {
                    super.visitMethodInsn(opcodeAndSource,
                        owner,
                        name + (name.startsWith("lambda$") ? "$solution" : ""),
                        descriptor,
                        isInterface);
                }

                @Override
                public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                    super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, Arrays.stream(bootstrapMethodArguments)
                        .map(o -> {
                            if (o instanceof Handle handle && handle.getName().startsWith("lambda$")) {
                                return new Handle(handle.getTag(),
                                    handle.getOwner(),
                                    handle.getName() + "$solution",
                                    handle.getDesc(),
                                    handle.isInterface());
                            } else {
                                return o;
                            }
                        })
                        .toArray());
                }
            };
        }
    }
}
