package io.shum.asm.instructions;

import io.shum.language.type.Type;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class FunctionDeclaration implements Instruction, WithScope {

    private final String name;
    private final int parameterCount;
    private final List<FunctionParameter> parameters;
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
        for (int i = 0; i < this.parameters.size(); i++) {
            var param = this.parameters.get(i);
                mv.visitVarInsn(ALOAD, i);
            if (param.name != null) {
                mv.visitVarInsn(ASTORE, i);
                // save the named parameters
                this.scope.allocateFunctionParameter(param, i);
            }
        }

        for (var instruction : instructions) {
            if (instruction instanceof VariableDeclaration || instruction instanceof VariableOperation) {
                // variable operations modify the current scope
                ((WithScope) instruction).setScope(this.scope);
            } else if (instruction instanceof WithScope ws) {
                // while operations that are scoped themselves clone the scope and
                // modify it just for themselves; outside that scope, the changes are not visible
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
                .map(p -> p.type.getTopLevelDataType().jvmType)
                .collect(joining());

        return String.format("(%s)%s", params, returnType);
    }

    @Override
    public String toString() {
        var name = this.name;
        var params = this.parameters.stream().map(p -> String.format("%s:%s", p.name, p.type.getTopLevelDataType().jvmType)).collect(joining(" ", "(", ")"));
        var returnType = this.returnTypes.stream().map(p -> p.getTopLevelDataType().name).collect(joining(" ", "(", ")"));
        return String.format("%s %s->%s [%s]", name, params, returnType, instructions.toString());
    }

    public String getDescriptor() {
        return descriptor;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public record FunctionSignature(List<FunctionParameter> parameters, List<Type> returnTypes) {}

    public record FunctionParameter(String name, Type type) {}
}
