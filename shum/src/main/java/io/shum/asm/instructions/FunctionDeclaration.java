package io.shum.asm.instructions;

import io.shum.language.type.ShumDataType;
import io.shum.utils.Maybe;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public final class FunctionDeclaration implements Instruction {

    private final String name;
    private final int parameterCount;
    private final List<String> parameters;
    private final boolean returns;
    private final List<String> returnTypes;
    private final String descriptor;
    private final List<Instruction> instructions;
    private final Map<String, VariableDeclaration> localVariables = new HashMap<>();

    public FunctionDeclaration(String name, FunctionSignature signature, List<Instruction> instructions) {
        this.name = name;
        this.parameterCount = signature.parameters.size();
        this.parameters = signature.parameters;
        this.instructions = instructions.stream().filter(ins -> !(ins instanceof VariableDeclaration)).toList();
        this.returns = !signature.returnTypes.isEmpty();
        this.returnTypes = signature.returnTypes;
        this.descriptor = createMethodDescriptor();

        int localVariableIndex = this.parameterCount;
        for (var ins : instructions) {
            if (ins instanceof VariableDeclaration vd) {
                localVariables.put(vd.getName(), vd.withIndex(localVariableIndex++));
            }
        }
    }

    @Override
    public void apply(MethodVisitor mv) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return name;
    }

    public List<VariableDeclaration> getVariables() {
        return this.localVariables.values().stream().toList();
    }

    public Maybe<VariableDeclaration> getVariable(String name) {
        return Maybe.of(localVariables.get(name));
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    private String createMethodDescriptor() {
        var returnType = returns ? ShumDataType.getDataType(returnTypes.get(0)).getTopLevelDataType().jvmType : "V";

        var params = parameters.stream()
                .map(p -> ShumDataType.getDataType(p).getTopLevelDataType().jvmType)
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
