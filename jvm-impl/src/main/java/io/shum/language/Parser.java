package io.shum.language;

import io.shum.asm.instructions.Constant;
import io.shum.asm.instructions.DefaultFunctionCall;
import io.shum.asm.instructions.FunctionDeclaration;
import io.shum.asm.instructions.Instruction;

import java.util.ArrayList;
import java.util.List;

public class Parser {

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
                        var instruction = switch (instrToken.tokenType()) {
                            case VALUE -> parseValue(instrToken);
                            case FUNCTION_INVOCATION -> DefaultFunctionCall
                                        .createFunctionCall(instrToken.value())
                                        .get();
                            default -> throw new IllegalStateException("Unexpected value: " + instrToken.tokenType());
                        };

                        functionInstructions.add(instruction);
                        instrToken = next();
                    }
                    yield new FunctionDeclaration(name, functionInstructions);
                }
                case VALUE -> parseValue(token);
                case FUNCTION_INVOCATION -> {
                    var functionName = token.value();
                    yield DefaultFunctionCall.createFunctionCall(functionName).get();
                }
                default -> throw new IllegalStateException("Unexpected token: " + token.tokenType());
            };

            this.instructions.add(parsedInstruction);
        }
    }

    private Constant parseValue(Token token) {
        var tokenValue = token.value();
        if (Utils.isDoubleQuoted(tokenValue) || Utils.isSingleQuoted(tokenValue)) {
            return new Constant(ShumDataType.STRING, tokenValue.substring(1, tokenValue.length() - 1));
        }

        if (Utils.isInteger(tokenValue)) {
            return new Constant(ShumDataType.INT, tokenValue);
        }

        if (Utils.isFloatingPoint(tokenValue)) {
            return new Constant(ShumDataType.DOUBLE, tokenValue);
        }

        throw new RuntimeException(String.format("Token '%s' is unknown", tokenValue));
    }

}
