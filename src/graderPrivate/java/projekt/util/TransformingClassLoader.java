package projekt.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.sourcegrade.jagr.api.testing.ClassTransformer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransformingClassLoader extends ClassLoader {

    private final String projectPrefix;
    private final ClassTransformer classTransformer;
    private final Map<String, byte[]> bytecodes = new HashMap<>();

    public TransformingClassLoader(String projectPrefix, ClassTransformer classTransformer) {
        this.projectPrefix = projectPrefix;
        this.classTransformer = classTransformer;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith(projectPrefix) && getSystemClassLoader().getResource("classes/" + name.replace('.', '/') + ".bin") != null) {
            byte[] bytecode;
            if (bytecodes.containsKey(name)) {
                bytecode = bytecodes.get(name);
            } else {
                bytecode = transform(name);
                bytecodes.put(name, bytecode);
            }

            return defineClass(name, bytecode, 0, bytecode.length);
        } else {
            return super.loadClass(name);
        }
    }

    public byte[] getTransformedBytecode(String className) {
        bytecodes.computeIfAbsent(className, this::transform);
        return bytecodes.get(className);
    }

    private byte[] transform(String className) {
        try {
            ClassReader classReader = new ClassReader(className);
            ClassWriter classWriter = new ClassWriter(classTransformer.getWriterFlags());
            classTransformer.transform(classReader, classWriter);
            return classWriter.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(new ClassNotFoundException("Could not find the requested class via ClassReader", e));
        }
    }
}
