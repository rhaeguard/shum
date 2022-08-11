package io.shum;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.objectweb.asm.Opcodes.*;

class Parser {

    private int instructionPointer;
    private final List<Token> tokens;

    public List<Instruction> getInstructions() {
        return instructions;
    }

    private final List<Instruction> instructions;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.instructionPointer = 0;
        this.instructions = new ArrayList<>();
    }

    private Token next() {
        var res = tokens.get(instructionPointer);
        instructionPointer++;
        return res;
    }

    private Token prev() {
        var res = tokens.get(instructionPointer);
        instructionPointer--;
        return res;
    }

    public void parse() {
        // instructions pointer
        while (instructionPointer < tokens.size()) {
            var token = next();

            var parsedInstruction = switch (token.tokenType()) {
                case DEF -> throw new IllegalStateException("def not implemented yet");
                case FUNC -> {
                    var name = next().value();
                    if (next().tokenType() != TokenType.EQUAL) {
                        // TODO: better error messages needed
                        throw new RuntimeException("'=' sign expected");
                    }
                    var instrToken = next();
                    var functionInstructions = new ArrayList<Instruction>();
                    while (instrToken.tokenType() != TokenType.END) {
                        var instruction = DefaultFunctionCall
                                .createFunctionCall(instrToken.value())
                                .get();
                        functionInstructions.add(instruction);
                        instrToken = next();
                    }
                    yield new FunctionDeclaration(name, functionInstructions);
                }
                case VALUE -> {
                    var tokenValue = token.value();
                    if (Utils.isDoubleQuoted(tokenValue) || Utils.isSingleQuoted(tokenValue)) {
                        yield new Constant(ShumDataType.STRING, tokenValue.substring(1, tokenValue.length() - 1));
                    }

                    if (Utils.isInteger(tokenValue)) {
                        yield new Constant(ShumDataType.INT, tokenValue);
                    }

                    if (Utils.isFloatingPoint(tokenValue)) {
                        yield new Constant(ShumDataType.DOUBLE, tokenValue);
                    }

                    throw new RuntimeException(String.format("Token '%s' is unknown", tokenValue));
                }
                case FUNCTION_INVOCATION -> {
                    var functionName = token.value();
                    yield DefaultFunctionCall.createFunctionCall(functionName).get();
                }
                default -> throw new IllegalStateException("Unexpected token: " + token.tokenType());
            };

            this.instructions.add(parsedInstruction);
        }
    }

}

class Utils {
    public static boolean isInteger(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isFloatingPoint(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isDoubleQuoted(String str) {
        return str.startsWith("\"") && str.endsWith("\"");
    }

    public static boolean isSingleQuoted(String str) {
        return str.startsWith("'") && str.endsWith("'");
    }
}

enum TokenType {
    VALUE, FUNCTION_INVOCATION,
    DEF, FUNC, EQUAL, L_CURLY, R_CURLY,
    END
}

record Token(String value, TokenType tokenType) {
}

class Lexer {

    public Token convertTokenToInstruction(String token) {
        if (token.startsWith("\"") && token.endsWith("\"") || token.startsWith("'") && token.endsWith("'")) {
            return new Token(token, TokenType.VALUE);
        }
        if (Utils.isFloatingPoint(token)) {
            return new Token(token, TokenType.VALUE);
        }

        Map<String, TokenType> knownKeywords = Map.of(
                "def", TokenType.DEF,
                "func", TokenType.FUNC,
                "{", TokenType.L_CURLY,
                "}", TokenType.R_CURLY,
                "=", TokenType.EQUAL,
                ".", TokenType.END
        );

        if (knownKeywords.containsKey(token)) {
            return new Token(token, knownKeywords.get(token));
        }

        return new Token(token, TokenType.FUNCTION_INVOCATION);
    }

    public List<Token> lex(File file) {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return lines
                    .map(String::trim)
                    .map(Lexer::shellSplit)
                    .flatMap(Collection::stream)
                    .map(this::convertTokenToInstruction)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Reference: https://gist.github.com/raymyers/8077031
    static List<String> shellSplit(CharSequence string) {
        var tokens = new ArrayList<String>();

        boolean escaping = false;
        char quoteChar = ' ';
        boolean quoting = false;
        int lastCloseQuoteIndex = Integer.MIN_VALUE;

        var current = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (escaping) {
                current.append(c);
                escaping = false;
            } else if (c == '\\' && !(quoting && quoteChar == '\'')) {
                escaping = true;
            } else if (quoting && c == quoteChar) {
                quoting = false;
                lastCloseQuoteIndex = i;
                current.append(c);
            } else if (!quoting && (c == '\'' || c == '"')) {
                quoting = true;
                quoteChar = c;
                current.append(c);
            } else if (!quoting && Character.isWhitespace(c)) {
                if (current.length() > 0 || lastCloseQuoteIndex == (i - 1)) {
                    tokens.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0 || lastCloseQuoteIndex == (string.length() - 1)) {
            tokens.add(current.toString());
        }

        return tokens;
    }

}

public class Compiler {

    public static void main(String[] args) {
        String filename = "hello_world.uk";//args[0];
        File file = new File(filename);
        List<Token> tokens = new Lexer().lex(file);
        Parser parser = new Parser(tokens);
        parser.parse();
        Compiler compiler = new Compiler();

//        var instructions = List.<Instruction>of(
//                new Constant(ShumDataType.INT, "16"), // weight
//                new Constant(ShumDataType.INT, "2"), // height
//                new Constant(ShumDataType.INT, "2"),  // power
//                new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.POW),
//                new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.DIV),
//                new PrintCall(ShumDataType.INT)
//        );

        compiler.compile(parser.getInstructions(), null);
    }

    public void compile(List<Instruction> instructions, Map<String, List<Instruction>> functionMap) {
        var classGenerator = new ClassGenerator("DummyClass");
        classGenerator.generate(instructions);
        classGenerator.saveToFile();
    }

}

class ClassGenerator {

    private final String className;
    private final ClassWriter cw;

    ClassGenerator(String className) {
        this.className = className;
        this.cw = new ClassWriter(0);
    }

    public void generate(List<Instruction> instructions) {
        cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

        var mg = new MethodGenerator(cw);

//        instructions.stream()
//                .filter(ins -> ins instanceof FunctionDeclaration)
//                .map(ins -> (FunctionDeclaration) ins)
//                .forEach(fd -> mg.generate(fd.getName(), "()V", fd.getInstructions()));

        List<Instruction> immediatelyExecutedInstructions = instructions.stream()
                .filter(ins -> !(ins instanceof FunctionDeclaration))
                .toList();

        mg.generate("main", "([Ljava/lang/String;)V", immediatelyExecutedInstructions);

        cw.visitEnd();
    }

    public void saveToFile() {
        try (var os = new FileOutputStream(String.format("%s.class", className))) {
            os.write(cw.toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

}

class MethodGenerator {

    private final ClassWriter cw;

    MethodGenerator(ClassWriter cw) {
        this.cw = cw;
    }

    public void generate(String methodName, String signature, List<Instruction> instructions) {
        var mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, signature, null, null);

        for (var instruction : instructions) {
            instruction.apply(mv);
        }
        mv.visitInsn(RETURN); //add return instruction

        mv.visitEnd();

        mv.visitMaxs(1000, (int) 10); //set max stack and max local variables
    }

}

enum ShumDataType {
    INT, DOUBLE, STRING
}

sealed interface Instruction extends Opcodes permits Constant, FunctionCall, FunctionDeclaration {
    void apply(MethodVisitor mv);
}

sealed class FunctionDeclaration implements Instruction {

    private final String name;
    private final List<Instruction> instructions;

    public FunctionDeclaration(String name, List<Instruction> instructions) {
        this.name = name;
        this.instructions = instructions;
    }

    @Override
    public void apply(MethodVisitor mv) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return name;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }
}

final class AnonymousFunctionDeclaration extends FunctionDeclaration {

    public AnonymousFunctionDeclaration(String name, List<Instruction> instructions) {
        super(name, instructions);
    }
}

final class Constant implements Instruction {

    private final ShumDataType dataType;
    private final String value;

    public Constant(ShumDataType dataType, String value) {
        this.dataType = dataType;
        this.value = value;
    }

    @Override
    public void apply(MethodVisitor mv) {
        switch (dataType) {
            case INT -> mv.visitLdcInsn(Integer.parseInt(value));
            case STRING -> mv.visitLdcInsn(value);
        }
    }
}

sealed interface FunctionCall extends Instruction permits ArithmeticFunctionCall, DefaultFunctionCall, PrintCall {
}

final class DefaultFunctionCall implements FunctionCall {

    private final String functionName;

    private DefaultFunctionCall(String functionName) {
        this.functionName = functionName;
    }

    public static Supplier<FunctionCall> createFunctionCall(String functionName) {
        if (PROVIDED_FUNCTIONS.containsKey(functionName)) {
            return PROVIDED_FUNCTIONS.get(functionName);
        }
        return () -> new DefaultFunctionCall(functionName);
    }

    @Override
    public void apply(MethodVisitor mv) {
        // TODO: implement this
        throw new UnsupportedOperationException();
    }

    private static Map<String, Supplier<FunctionCall>> PROVIDED_FUNCTIONS = Map.ofEntries(
            // unary functions
            entry("abs", () -> new UnaryArithmeticFunctionCall(UnaryArithmeticFunctionCall.Operation.ABS)),
            entry("neg", () -> new UnaryArithmeticFunctionCall(UnaryArithmeticFunctionCall.Operation.NEG)),
            entry("incr", () -> new UnaryArithmeticFunctionCall(UnaryArithmeticFunctionCall.Operation.INCR)),
            entry("decr", () -> new UnaryArithmeticFunctionCall(UnaryArithmeticFunctionCall.Operation.DECR)),
            entry("not", () -> new UnaryArithmeticFunctionCall(UnaryArithmeticFunctionCall.Operation.NOT)),
            // binary functions
            entry("+", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.ADD)),
            entry("-", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.SUB)),
            entry("*", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.MUL)),
            entry("/", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.DIV)),
            entry("%", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.REM)),
            entry("pow", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.POW)),
            // printing
            entry("print", () -> new PrintCall(ShumDataType.STRING)),
            entry("printInt", () -> new PrintCall(ShumDataType.INT)),
            entry("printDouble", () -> new PrintCall(ShumDataType.DOUBLE))
    );
}

final class PrintCall implements FunctionCall {

    private final ShumDataType type;

    public PrintCall(ShumDataType type) {
        this.type = type;
    }

    @Override
    public void apply(MethodVisitor mv) {
        String descriptor = switch (type) {
            case STRING -> "Ljava/lang/String;";
            case INT -> "I";
            case DOUBLE -> "D";
        };
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitInsn(SWAP); // we need to swap because println takes System.out as [this] pointer
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", String.format("(%s)V", descriptor));
    }
}

sealed interface ArithmeticFunctionCall extends FunctionCall permits UnaryArithmeticFunctionCall, BinaryArithmeticFunctionCall {
}

final class UnaryArithmeticFunctionCall implements ArithmeticFunctionCall {
    public enum Operation {
        ABS, NEG, INCR, DECR, NOT
    }

    private final Operation operation;

    public UnaryArithmeticFunctionCall(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {
        switch (operation) {
            case ABS -> {
                // TODO: only works for ints now
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I");
            }
            case NEG -> {
                // TODO: only works for ints now
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "negateExact", "(I)I");
            }
            case INCR -> {
                // TODO: only works for ints now
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "incrementExact", "(I)I");
            }
            case DECR -> {
                // TODO: only works for ints now
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "decrementExact", "(I)I");
            }
            case NOT -> {
                Label trueLabel = new Label();
                Label endLabel = new Label();
                mv.visitJumpInsn(IFNE, trueLabel);
                mv.visitInsn(ICONST_1);
                mv.visitJumpInsn(GOTO, endLabel);
                mv.visitLabel(trueLabel);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(endLabel);
            }
        }
    }
}

final class BinaryArithmeticFunctionCall implements ArithmeticFunctionCall {

    public enum Operation {
        ADD, SUB, MUL, DIV, REM, POW
    }

    private final Operation operation;

    public BinaryArithmeticFunctionCall(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {

        switch (operation) {
            case ADD -> mv.visitInsn(IADD);
            case SUB -> mv.visitInsn(ISUB);
            case MUL -> mv.visitInsn(IMUL);
            case DIV -> mv.visitInsn(IDIV);
            case REM -> mv.visitInsn(IREM);
            case POW -> {
                mv.visitIntInsn(ISTORE, 0);
                mv.visitInsn(I2D);
                mv.visitIntInsn(ILOAD, 0);
                mv.visitInsn(I2D);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D");
                mv.visitInsn(D2I);
            }
        }
    }
}