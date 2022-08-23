package io.shum;

import io.shum.asm.Context;
import io.shum.asm.generation.ClassGenerator;
import io.shum.asm.instructions.Instruction;
import io.shum.language.Lexer;
import io.shum.language.Parser;
import io.shum.language.Token;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class Compiler {

    private final List<Instruction> instructions;

    public Compiler(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void compile(boolean shouldRun) {
        var classGenerator = new ClassGenerator(MAIN_CLASS_NAME);
        classGenerator.generate(instructions);
        classGenerator.saveToFile();
        if (shouldRun) {
            run(classGenerator);
        }
    }

    public void run(ClassGenerator classGenerator) {
        var clazz = classGenerator.generateClass();
        try {
            String[] strings = {};
            Method main = clazz.getMethod("main", strings.getClass());
            main.invoke(null, (Object) new String[]{});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Compiler debugInstructions(boolean shouldDebugInstructions) {
        if (shouldDebugInstructions) {
            System.out.println("Instructions:");
            for (var instruction : instructions) {
                System.out.println(" - " + instruction.toString());
            }
        }
        return this;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("usage: <file>");
            System.exit(2);
        }
        compile(args);
    }

    private static void compile(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: [OPTIONS] <file>");
            System.err.println("Options:");
            System.err.println(" --run      compiles and runs the code");
        }

        var filename = args[args.length - 1];
        if (!filename.endsWith(".shum")) {
            System.err.println("Expected a .shum file");
            System.exit(2);
        }

        var options = Arrays.asList(Arrays.copyOfRange(args, 0, args.length - 1));

        boolean shouldRun = options.contains("--run");
        boolean debugInstructions = options.contains("--debugInstructions");

        var file = new File(filename);
        var tokens = new Lexer(file).lex();
        for (Token token : tokens) {
            System.out.println(token);
        }
        var instructions = new Parser(tokens, new Context()).parse();

        new Compiler(instructions)
                .debugInstructions(debugInstructions)
                .compile(shouldRun);
    }

    public static final String MAIN_CLASS_NAME = "Main";
}
