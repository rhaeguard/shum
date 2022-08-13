package io.shum;

import io.shum.asm.Context;
import io.shum.asm.generation.ClassGenerator;
import io.shum.asm.instructions.Instruction;
import io.shum.language.Lexer;
import io.shum.language.Parser;
import io.shum.language.Token;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Compiler {

    public static void main(String[] args) {
        String filename = "playground.uk";//args[0];
        File file = new File(filename);
        List<Token> tokens = new Lexer().lex(file);
        for (Token token : tokens) {
            System.out.println(token);
        }
        System.out.println("==========");
        Parser parser = new Parser(tokens, new Context());
        parser.parse();
        Compiler compiler = new Compiler();
        System.out.println("Compiling...");
        for (Instruction instruction : parser.getInstructions()) {
            System.out.println(instruction);
        }
        compiler.compile(parser.getInstructions(), null);
    }

    public void compile(List<Instruction> instructions, Map<String, List<Instruction>> functionMap) {
        var classGenerator = new ClassGenerator("DummyClass");
        classGenerator.generate(instructions);
        classGenerator.saveToFile();
    }

}
