package io.shum.language;

import io.shum.asm.Context;
import io.shum.asm.instructions.*;
import io.shum.language.type.ContainerType;
import io.shum.language.type.ShumDataType;
import io.shum.language.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    private boolean expect(TokenType tokenType) {
        if (instructionPointer >= tokens.size()) return false;
        return tokens.get(instructionPointer).tokenType() == tokenType;
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
                case FUNC -> parseFuncDeclaration();
                case IF -> parseIfStatement();
                case LOOP -> parseLoopStatement();
                case VALUE, LIST_NOTATION, SET_NOTATION, DICT_NOTATION -> parseValueGeneral(token);
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

            appendInstruction(this.instructions, parsedInstruction);
        }

        return this.instructions;
    }

    private Value parseValueGeneral(Token token) {
        return switch (token.tokenType()) {
            case VALUE -> parseValue(token);
            case LIST_NOTATION, SET_NOTATION, DICT_NOTATION -> parseCollectionNotation(token);
            case VARIABLE_LOAD -> new DynamicValue(parseVariableOp(token));
            default -> throw new IllegalStateException("Not a value producing token: " + token.tokenType());
        };
    }

    private CollectionValue parseCollectionNotation(Token token) {
        var notationType = token.tokenType();
        var value = token.value();
        var elementTokens = new Lexer(null).lex(value);

        // everything must be a constant!
        // todo: support VARIABLE_LOAD as well

        if (!elementTokens.stream().allMatch(e -> e.tokenType() == TokenType.VALUE || e.tokenType() == TokenType.VARIABLE_LOAD)) {
            throw new RuntimeException("Collection values must be constants!");
        }

        var elements = elementTokens.stream().map(this::parseValue).toList();

        var dataType = switch (notationType) {
            case LIST_NOTATION -> ShumDataType.LIST;
            case SET_NOTATION -> ShumDataType.SET;
            case DICT_NOTATION -> ShumDataType.DICT;
            default -> throw new RuntimeException("Unsupported collection data type: " + token.value());
        };

        return new CollectionValue(new ContainerType(dataType, Type.nothingType()), elements);
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

        var dataType = ShumDataType.getDataType(type);

        if (expect(TokenType.EQUAL)) {
            next(); // skip EQUAL
            var value = parseValueGeneral(next());
            return new VariableDeclaration(name, dataType, value);
        }

        return new VariableDeclaration(name, dataType);
    }

    private LoopStatement parseLoopStatement() {
        var conditionMacro = parseMacroBody(Set.of(TokenType.DO));
        var loopBodyMacro = parseMacroBody(Set.of(TokenType.END));
        return new LoopStatement(conditionMacro, loopBodyMacro);
    }

    private IfElseCondition parseIfStatement() {
        var trueMacro = parseMacroBody(Set.of(TokenType.END, TokenType.ELSE));

        // it's the ELSE or END token, so we need to move to the next token
        var falseMacro = current().tokenType() == TokenType.ELSE
                ? parseMacroBody(Set.of(TokenType.END))
                : Collections.<Instruction>emptyList();
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

    /**
     * Appends a new instruction into the existing list of instructions, and handles variable init syntactic sugar
     */
    private void appendInstruction(List<Instruction> instructions, Instruction newInstruction) {
        instructions.add(newInstruction);
        if (newInstruction instanceof VariableDeclaration vd && vd.hasInitialValue()) {
            var vo = new VariableOperation(VariableOperation.Operation.STORE, vd.getName(), context);
            instructions.add(vd.getInitialValue());
            instructions.add(vo);
        }
    }

    private MacroDeclaration parseMacroBody(String name, Set<TokenType> terminationTokens) {
        var instrToken = next();
        var macroInstructions = new ArrayList<Instruction>();
        while (!terminationTokens.contains(instrToken.tokenType())) {
            var instruction = parseCallableBodyInstruction(instrToken);
            appendInstruction(macroInstructions, instruction);
            instrToken = next();
        }

        return new MacroDeclaration(name, macroInstructions);
    }

    private List<Instruction> parseMacroBody(Set<TokenType> terminationTokens) {
        var instrToken = next();
        var macroInstructions = new ArrayList<Instruction>();
        while (!terminationTokens.contains(instrToken.tokenType())) {
            var instruction = parseCallableBodyInstruction(instrToken);
            appendInstruction(macroInstructions, instruction);
            instrToken = next();
        }

        return macroInstructions;
    }

    private Instruction parseCallableBodyInstruction(Token token) {
        return switch (token.tokenType()) {
            case VALUE, LIST_NOTATION, SET_NOTATION, DICT_NOTATION -> parseValueGeneral(token);
            case IF -> parseIfStatement();
            case LOOP -> parseLoopStatement();
            case FUNCTION_INVOCATION -> UserDefinedFunctionCall.createFunctionCall(token.value(), context);
            case LET -> parseLet();
            case VARIABLE_LOAD, VARIABLE_STORE -> parseVariableOp(token);
            default -> throw new IllegalStateException("Unexpected token: " + token.tokenType());
        };
    }

    private FunctionDeclaration parseFuncDeclaration() {
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
            appendInstruction(functionInstructions, instruction);
            instrToken = next();
        }
        var fd = new FunctionDeclaration(name, sig, functionInstructions);
        fd.setScope(FunctionDeclaration.createNewFunctionScope(fd));
        return fd;
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
                .map(Parser::getFunctionSignature)
                .toList();

        var returnTypes = signatureTokens.stream()
                .dropWhile(t -> t.tokenType() != TokenType.ARROW)
                .skip(1)
                .map(Token::value)
                .map(ShumDataType::getDataType)
                .toList();

        if (returnTypes.size() > 1) {
            throw new RuntimeException(String.format("Function must return one type. %d given: %s", returnTypes.size(), returnTypes));
        }

        return new FunctionDeclaration.FunctionSignature(paramTypes, returnTypes);
    }

    private static FunctionDeclaration.FunctionParameter getFunctionSignature(Token token) {
        var tokenStr = token.value();
        if (tokenStr.contains(":")) {
            var pieces = tokenStr.split(":");
            var name = pieces[0];
            var type = ShumDataType.getDataType(pieces[1]);
            return new FunctionDeclaration.FunctionParameter(name, type);
        }
        var type = ShumDataType.getDataType(tokenStr);
        return new FunctionDeclaration.FunctionParameter(null, type);
    }

    private String parseFunctionName() {
        var nameToken = next();
        if (Utils.isProperFunctionName(nameToken.value())) {
            return nameToken.value();
        }
        throw new RuntimeException("Function/macro name is not in a proper format. Does not match the pattern: [a-zA-Z_\\-][a-zA-Z_0-9\\-]*");
    }

    private Value parseValue(Token token) {
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

        if (token.tokenType() == TokenType.VARIABLE_LOAD) {
            return new DynamicValue(new VariableOperation(VariableOperation.Operation.LOAD, token.value(), context));
        }

        throw new RuntimeException(String.format("Token '%s' is unknown", tokenValue));
    }

}
