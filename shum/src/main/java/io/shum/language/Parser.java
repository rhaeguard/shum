package io.shum.language;

import io.shum.asm.Context;
import io.shum.asm.instructions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;

public class Parser {

    private int instructionPointer;
    private final List<Token> tokens;
    private final Context context;
    private final List<Instruction> instructions;

    public Parser(List<Token> tokens, Context context) {
        this.tokens = tokens;
        this.instructionPointer = 0;
        this.instructions = new ArrayList<>();
        this.context = context;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    private Token next() {
        var res = tokens.get(instructionPointer);
        instructionPointer++;
        return res;
    }

    private Token current() {
        return tokens.get(instructionPointer - 1);
    }

    public List<Instruction> parse() {
        // instructions pointer
        while (instructionPointer < tokens.size()) {
            var token = next();

            var parsedInstruction = switch (token.tokenType()) {
                case MACRO -> parseMacroDeclaration();
                case FUNC -> processFuncDeclaration();
                case IF -> parseIfStatement();
                case LOOP -> parseLoopStatement();
                case VALUE -> parseValue(token);
                case FUNCTION_INVOCATION -> UserDefinedFunctionCall.createFunctionCall(token.value(), context);
                case LET -> parseLet();
                case VARIABLE_LOAD, VARIABLE_STORE -> parseVariableOp(token);
                default -> throw new IllegalStateException("Unexpected token: " + token.tokenType());
            };

            // remember user-defined functions and macros
            if (parsedInstruction instanceof MacroDeclaration md) {
                context.createNewMacroDeclaration(md);
            } else if (parsedInstruction instanceof FunctionDeclaration fd) {
                context.createNewFunctionDeclaration(fd);
            } else if (parsedInstruction instanceof VariableDeclaration vd) {
                context.createNewStaticVariableDeclaration(vd);
            }

            this.instructions.add(parsedInstruction);
        }

        return this.instructions;
    }

    private VariableOperation parseVariableOp(Token token) {
        var op = token.tokenType() == TokenType.VARIABLE_LOAD
                ? VariableOperation.Operation.LOAD
                : VariableOperation.Operation.STORE;
        return new VariableOperation(op, token.value(), context);
    }

    private VariableDeclaration parseLet() {
        var variableInfo = next();
        if (variableInfo.tokenType() != TokenType.FUNCTION_INVOCATION) {
            throw new RuntimeException("Unrecognized token: " + variableInfo.value());
        }

        var info = variableInfo.value();
        var pieces = info.split(":");
        if (pieces.length != 2) {
            throw new RuntimeException("Incorrect variable info: " + info);
        }

        var name = pieces[0];
        var type = pieces[1];

        if (!ShumDataType.contains(type)) {
            throw new RuntimeException("Unknown type: " + type);
        }

        return new VariableDeclaration(name, ShumDataType.getDataType(type));
    }

    private LoopStatement parseLoopStatement() {
        var conditionMacro = parseMacroBody("condition", Set.of(TokenType.DO));
        var loopBodyMacro = parseMacroBody("body", Set.of(TokenType.END));
        return new LoopStatement(conditionMacro, loopBodyMacro);
    }

    private IfElseCondition parseIfStatement() {
        var trueMacro = parseMacroBody("true", Set.of(TokenType.END, TokenType.ELSE));

        final MacroDeclaration falseMacro;
        if (current().tokenType() == TokenType.ELSE) {
            falseMacro = parseMacroBody("false", Set.of(TokenType.END));
        } else {
            next(); // it's the ELSE or END token, so we need to move to the next token
            falseMacro = new MacroDeclaration("false", emptyList());
        }
        return new IfElseCondition(trueMacro, falseMacro);
    }

    private MacroDeclaration parseMacroDeclaration() {
        var name = parseFunctionName();
        if (next().tokenType() != TokenType.EQUAL) {
            // TODO: better error messages needed
            throw new RuntimeException("'=' sign expected to define a macro body");
        }
        return parseMacroBody(name, Set.of(TokenType.END));
    }

    private MacroDeclaration parseMacroBody(String name, Set<TokenType> terminationTokens) {
        var instrToken = next();
        var macroInstructions = new ArrayList<Instruction>();
        while (!terminationTokens.contains(instrToken.tokenType())) {
            var instruction = parseCallableBodyInstruction(instrToken);
            macroInstructions.add(instruction);
            instrToken = next();
        }

        return new MacroDeclaration(name, macroInstructions);
    }

    private Instruction parseCallableBodyInstruction(Token token) {
        return switch (token.tokenType()) {
            case VALUE -> parseValue(token);
            case IF -> parseIfStatement();
            case LOOP -> parseLoopStatement();
            case FUNCTION_INVOCATION -> UserDefinedFunctionCall.createFunctionCall(token.value(), context);
            case VARIABLE_LOAD, VARIABLE_STORE -> parseVariableOp(token);
            default -> throw new IllegalStateException("Unexpected token: " + token.tokenType());
        };
    }

    private FunctionDeclaration processFuncDeclaration() {
        var name = parseFunctionName();
        var sig = parseFunctionSignature();

        var instrToken = next();
        var functionInstructions = new ArrayList<Instruction>();
        while (instrToken.tokenType() != TokenType.END) {
            if (instrToken.tokenType() == TokenType.RETURN) {
                instrToken = next();
                continue;
            }
            var instruction = parseCallableBodyInstruction(instrToken);

            functionInstructions.add(instruction);
            instrToken = next();
        }
        return new FunctionDeclaration(name, sig, functionInstructions);
    }

    private FunctionDeclaration.FunctionSignature parseFunctionSignature() {
        var instrToken = next();
        var signatureTokens = new ArrayList<Token>();

        while (instrToken.tokenType() != TokenType.EQUAL) {
            // TODO: maybe pick a better and more descriptive type
            if (!Set.of(TokenType.FUNCTION_INVOCATION, TokenType.ARROW).contains(instrToken.tokenType())) {
                throw new RuntimeException("Unexpected token: " + instrToken.value());
            }
            signatureTokens.add(instrToken);
            instrToken = next();
        }

        var paramTypes = signatureTokens.stream()
                .takeWhile(t -> t.tokenType() != TokenType.ARROW)
                .map(Token::value)
                .toList();

        var returnTypes = signatureTokens.stream()
                .dropWhile(t -> t.tokenType() != TokenType.ARROW)
                .skip(1)
                .map(Token::value)
                .toList();

        if (returnTypes.size() > 1) {
            throw new RuntimeException(String.format("Function must return one type. %d given: %s", returnTypes.size(), returnTypes));
        }

        return new FunctionDeclaration.FunctionSignature(paramTypes, returnTypes);
    }

    private String parseFunctionName() {
        var nameToken = next();
        if (Utils.isProperFunctionName(nameToken.value())) {
            return nameToken.value();
        }
        throw new RuntimeException("Function/macro name is not in a proper format. Does not match the pattern: [a-zA-Z_\\-][a-zA-Z_0-9\\-]*");
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
