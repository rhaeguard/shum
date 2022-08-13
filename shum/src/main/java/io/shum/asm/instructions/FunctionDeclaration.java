package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

public final class FunctionDeclaration implements Instruction {

    private final String name;
    private final int parameterCount;
    private final boolean returns;
    private final String descriptor;
    private final List<Instruction> instructions;

    public FunctionDeclaration(String name, int parameterCount, boolean returns, List<Instruction> instructions) {
        this.name = name;
        this.parameterCount = parameterCount;
        this.instructions = instructions;
        this.returns = returns;
        this.descriptor = createMethodDescriptor();
    }

    @Override
    public void apply(MethodVisitor mv) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return name;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    private String createMethodDescriptor() {
        var returnType = returns ? "Ljava/lang/Object;" : "V";
        var params = range(0, parameterCount)
                .mapToObj(i -> "Ljava/lang/Object;")
                .collect(joining());

        return String.format("(%s)%s", params, returnType);
    }

    @Override
    public String toString() {
        return String.format("%s [%d]%s[%s]", name, parameterCount, returns ? " <returns> " : " ", instructions.toString());
    }

    public String getDescriptor() {
        return descriptor;
    }

    public int getParameterCount() {
        return parameterCount;
    }
}
