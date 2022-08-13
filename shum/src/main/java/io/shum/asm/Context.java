package io.shum.asm;

import io.shum.asm.instructions.FunctionDeclaration;
import io.shum.asm.instructions.MacroDeclaration;
import io.shum.utils.Maybe;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private final Map<String, FunctionDeclaration> declaredFunctions;
    private final Map<String, MacroDeclaration> declaredMacros;

    public Context() {
        declaredFunctions = new HashMap<>();
        declaredMacros = new HashMap<>();
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
}
