package io.shum.asm;

import io.shum.asm.instructions.FunctionDeclaration;
import io.shum.asm.instructions.MacroDeclaration;
import io.shum.asm.instructions.VariableDeclaration;
import io.shum.utils.Maybe;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private final Map<String, FunctionDeclaration> declaredFunctions;
    private final Map<String, MacroDeclaration> declaredMacros;
    private final Map<String, VariableDeclaration> declaredStaticVariables;

    public Context() {
        declaredFunctions = new HashMap<>();
        declaredMacros = new HashMap<>();
        declaredStaticVariables = new HashMap<>();
    }

    public void createNewFunctionDeclaration(FunctionDeclaration fd) {
        if (declaredFunctions.containsKey(fd.getName())) {
            throw new RuntimeException(String.format("Function '%s' has already been declared", fd.getName()));
        }

        if (declaredMacros.containsKey(fd.getName())) {
            throw new RuntimeException(String.format("There exists a macro with the name '%s'", fd.getName()));
        }

        declaredFunctions.put(fd.getName(), fd);
    }

    public void createNewMacroDeclaration(MacroDeclaration md) {
        if (declaredMacros.containsKey(md.getName())) {
            throw new RuntimeException(String.format("Macro '%s' has already been declared", md.getName()));
        }

        if (declaredFunctions.containsKey(md.getName())) {
            throw new RuntimeException(String.format("There exists a function with the name '%s'", md.getName()));
        }

        declaredMacros.put(md.getName(), md);
    }

    public Maybe<FunctionDeclaration> getFunctionDeclaration(String functionName) {
        return Maybe.of(declaredFunctions.get(functionName));
    }

    public Maybe<MacroDeclaration> getMacroDeclaration(String macroName) {
        return Maybe.of(declaredMacros.get(macroName));
    }

    public void createNewStaticVariableDeclaration(VariableDeclaration vd) {
        if (declaredStaticVariables.containsKey(vd.getName())){
            throw new RuntimeException(String.format("Static variable '%s' has already been declared", vd.getName()));
        }
        declaredStaticVariables.put(vd.getName(), vd);
    }

    public Maybe<VariableDeclaration> getVariableDeclaration(String varName) {
        return Maybe.of(declaredStaticVariables.get(varName));
    }
}
