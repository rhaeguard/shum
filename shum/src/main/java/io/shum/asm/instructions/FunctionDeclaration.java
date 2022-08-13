package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

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
        Map<String, String> KNOWN_TYPES = new HashMap<>();
        KNOWN_TYPES.put("int", "Ljava/lang/Long;");
        KNOWN_TYPES.put("double", "Ljava/lang/Double;");
        KNOWN_TYPES.put("string", "Ljava/lang/String;");

        final String returnType;

        if (returns) {
            returnType = KNOWN_TYPES.get(returnTypes.get(0));
            if (returnType == null) {
                throw new RuntimeException("Unknown return type : " + returnTypes.get(0));
            }
        } else {
            returnType = "V";
        }

        var params = parameters.stream()
                .map(p -> {
                    var jvmType = KNOWN_TYPES.get(p);
                    if (jvmType == null) {
                        throw new RuntimeException("Unknown parameter type : " + p);
                    }
                    return jvmType;
                })
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
