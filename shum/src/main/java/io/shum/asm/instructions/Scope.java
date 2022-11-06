package io.shum.asm.instructions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class Scope {

    private final Map<String, VariableDeclaration> variables;
    private int nextLocalVariableIndex;

    public Scope(int startIndex) {
        this.variables = new HashMap<>();
        this.nextLocalVariableIndex = startIndex;
    }

    private Scope(Map<String, VariableDeclaration> variables, int nextLocalVariableIndex) {
        this.variables = variables;
        this.nextLocalVariableIndex = nextLocalVariableIndex;
    }

    public int nextIndex(VariableDeclaration vd) {
        variables.put(vd.getName(), vd);
        return ++nextLocalVariableIndex;
    }

    public VariableDeclaration getLocalVariable(String name) {
        return variables.get(name);
    }

    public Scope cloneScope() {
        var newVariables = variables.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Scope(
                newVariables, nextLocalVariableIndex
        );
    }

    public void allocateFunctionParameter(FunctionDeclaration.FunctionParameter param, int index) {
        var vd = new VariableDeclaration(param.name(), param.type());
        vd.setVariableIndex(index);
        this.variables.put(param.name(), vd);
    }
}
