package io.shum.asm.instructions;

import java.util.List;

public final class AnonymousFunctionDeclaration extends FunctionDeclaration {

    public AnonymousFunctionDeclaration(String name, List<Instruction> instructions) {
        super(name, instructions);
    }
}
