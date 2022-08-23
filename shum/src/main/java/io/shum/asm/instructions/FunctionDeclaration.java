package io.shum.asm.instructions;

import io.shum.language.ShumDataType;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class FunctionDeclaration implements Instruction {

    private final String name;
    private final int parameterCount;
    private final List<String> parameters;
    private final boolean returns;
    private final List<String> returnTypes;
    private final String descriptor;
    private final List<Instruction> instructions;

    public FunctionDeclaration(String name, FunctionSignature signature, List<Instruction> instructions) {
        this.name = name;
        this.parameterCount = signature.parameters.size();
        this.parameters = signature.parameters;
        this.instructions = instructions;
        this.returns = !signature.returnTypes.isEmpty();
        this.returnTypes = signature.returnTypes;
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
        var returnType = returns ? ShumDataType.getDataType(returnTypes.get(0)).jvmType : "V";

        var params = parameters.stream()
                .map(p -> ShumDataType.getDataType(p).jvmType)
                .collect(joining());

        return String.format("(%s)%s", params, returnType);
    }

    @Override
    public String toString() {
        var name = this.name;
        var params = this.parameters.stream().collect(joining(" ", "(", ")"));
        var returnType = this.returnTypes.stream().collect(joining(" ", "(",")"));
        return String.format("%s %s->%s [%s]", name, params, returnType, instructions.toString());
    }

    public String getDescriptor() {
        return descriptor;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public boolean returns() {
        return returns;
    }

    public record FunctionSignature(List<String> parameters, List<String> returnTypes) {}
}
