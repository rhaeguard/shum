package io.shum;

import io.shum.asm.Context;
import io.shum.asm.generation.ClassGenerator;
import io.shum.asm.instructions.Instruction;
import io.shum.language.Lexer;
import io.shum.language.Parser;

import java.io.File;
import java.util.List;

public class Compiler {

    private final List<Instruction> instructions;

    public Compiler(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void compile() {
        var classGenerator = new ClassGenerator("Main");
        classGenerator.generate(instructions);
        classGenerator.saveToFile();
    }

    private Compiler debugInstructions() {
        System.out.println("Instructions:");
        for (var instruction : instructions) {
            System.out.println(" - " + instruction.toString());
        }
        return this;
    }

    public static void main(String[] args) {
        var filename = args[0];
        var file = new File(filename);
        var tokens = new Lexer(file).lex();
        var instructions = new Parser(tokens, new Context()).parse();

        new Compiler(instructions)
                .debugInstructions()
                .compile();
    }
}
