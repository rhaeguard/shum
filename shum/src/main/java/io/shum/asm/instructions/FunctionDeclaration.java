package io.shum.asm.instructions;

import io.shum.language.type.Type;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class FunctionDeclaration implements Instruction, WithScope {

    private final String name;
    private final int parameterCount;
    private final List<Type> parameters;
    private final boolean returns;
    private final List<Type> returnTypes;
    private final String descriptor;
    private final List<Instruction> instructions;

    private Scope scope;

    public FunctionDeclaration(String name, FunctionSignature signature, List<Instruction> instructions) {
        this.name = name;
        this.parameterCount = signature.parameters.size();
        this.parameters = signature.parameters;
        this.instructions = instructions;
        this.returns = !signature.returnTypes.isEmpty();
        this.returnTypes = signature.returnTypes;
        this.descriptor = createMethodDescriptor();
    }

    public static Scope createNewFunctionScope(FunctionDeclaration fd) {
        return new Scope(fd.parameterCount);
    }

    @Override
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    @Override
    public void apply(MethodVisitor mv) {
        // load all the parameters to the method call stack
        for (int i = 0; i < getParameterCount(); i++) {
            mv.visitVarInsn(ALOAD, i);
        }

        for (var instruction : instructions) {
            if (instruction instanceof VariableDeclaration || instruction instanceof VariableOperation) {
                ((WithScope) instruction).setScope(this.scope);
            } else if (instruction instanceof WithScope ws) {
                ws.setScope(this.scope.cloneScope());
            }
            instruction.apply(mv);
        }

        if (this.returns) {
            mv.visitInsn(ARETURN); // add return instruction
        } else {
            mv.visitInsn(RETURN); // add return instruction
        }
    }

    public String getName() {
        return name;
    }

    private String createMethodDescriptor() {
        var returnType = returns ? returnTypes.get(0).getTopLevelDataType().jvmType : "V";

        var params = parameters.stream()
                .map(p -> p.getTopLevelDataType().jvmType)
                .collect(joining());

        return String.format("(%s)%s", params, returnType);
    }

    @Override
    public String toString() {
        var name = this.name;
        var params = this.parameters.stream().map(p -> p.getTopLevelDataType().name).collect(joining(" ", "(", ")"));
        var returnType = this.returnTypes.stream().map(p -> p.getTopLevelDataType().name).collect(joining(" ", "(", ")"));
        return String.format("%s %s->%s [%s]", name, params, returnType, instructions.toString());
    }

    public String getDescriptor() {
        return descriptor;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public record FunctionSignature(List<Type> parameters, List<Type> returnTypes) {
    }
}
