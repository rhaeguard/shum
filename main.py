import sys
from random import randint
import shlex
import std

def is_float(n):
    try:
        float(n)
        return True
    except ValueError:
        return False

def castIfAvailable(val):
    try:
        if val.isnumeric():
            val = int(val)
        elif is_float(val):
            val = float(val)
        elif val.startswith("\"") and val.endswith("\""):
            val = val[1:-1]
        elif val.startswith("[") and val.endswith("]") and ":" in val:
            val = val[1:-1]
            start, end = val.split(":")
            start = int(start)
            end = int(end)            
            return list(range(start, end + 1))
        return val
    except:
        return val

def binaryArith(stack, op):
    b = stack.pop()
    a = stack.pop()
    try:
        stack.append(op(a, b))
    except:
        print(f"Arguments were: a={a} b = {b}")
        raise

arithmetic_ops = {
    "+": lambda a, b: a + b,
    "-": lambda a, b: a - b,
    "*": lambda a, b: a * b,
    "//": lambda a, b: a // b,
    "/": lambda a, b: a / b,
    "pow": lambda a, b: a ** b,
    "<": lambda a, b: a < b,
    "<=": lambda a, b: a <= b,
    ">": lambda a, b: a > b,
    ">=": lambda a, b: a >= b,
    "==": lambda a, b: a == b
}

EMPTY_BODY = ([], False)

lib = {
    # name => (instructions, isScoped)
    "dup": EMPTY_BODY,
    "show": EMPTY_BODY,
    "ifelse": EMPTY_BODY,
    "swap": EMPTY_BODY,
    "drop": EMPTY_BODY,
    "save": EMPTY_BODY
}

# name => value
globalVariables = dict()

def handleVar(instructions):
    name = instructions.pop(0)
    v = None
    if instructions[0] == "=":
        _ = instructions.pop(0)
        v = castIfAvailable(instructions.pop(0))
    globalVariables[name] = v

def handleScope(instructions):
    funcIns = []
    isScoped = True
    if instructions[0] != "{":
        v = castIfAvailable(instructions.pop(0))
        return ([v], False)

    while True:
        ins = instructions.pop(0)
        if ins == "{":
            if funcIns == []:
                isScoped = True
            else:
                instructions.insert(0, "{")
                scopeName = f"anon#{randint(0, 15000)}"
                lib[scopeName] = handleScope(instructions)
                funcIns.append(scopeName)
            continue
        
        if ins not in ["}"]:
            funcIns.append(castIfAvailable(ins))
        else:
            break
    return (funcIns, isScoped)

def handleDef(instructions):
    name = instructions.pop(0)
    v = instructions[0]
    funcIns, isScoped = [], False
    if v == "=":
        instructions.pop(0)
        funcIns, isScoped = handleScope(instructions)
    lib[name] = (funcIns, isScoped)

def handleImpl(instructions):
    name = instructions.pop(0)
    v = instructions.pop(0)
    if v != "=":
        raise Exception("Should have '='")

    funcIns = []
    while True:
        ins = instructions[0]
        if ins == "{":
            scopeName = f"{name}#{randint(0, 15000)}"
            lib[scopeName] = handleScope(instructions)
            funcIns.append(scopeName)
        elif ins not in ["."]:
            instructions.pop(0)
            funcIns.append(castIfAvailable(ins))
        else:
            instructions.pop(0)
            break
    lib[name] = (funcIns, False)

def exec(instructions, stack):
    while instructions:
        e = instructions.pop(0)
        if e in arithmetic_ops:
            binaryArith(stack, arithmetic_ops[e])
        elif str(e).startswith("@"):
            varName = e[1:]
            if varName not in globalVariables:
                raise Exception(f"Variable '{varName}' is not defined")
            if globalVariables[varName] is None:
                raise Exception(f"Variable '{varName}' is uninitialized")
            stack.append(globalVariables[varName])
        elif str(e).startswith("$"):
            varName = e[1:]
            if varName not in globalVariables:
                raise Exception(f"Variable '{varName}' is not defined")
            stack.append(varName)
        elif e in lib:
            if e == "show":
                std.show(stack)
            elif e == "dup":
                std.dup(stack)
            elif e == "swap":
                std.swap(stack)
            elif e == "drop":
                std.drop(stack)
            elif e == "ifelse":
                std.ifelse(stack, exec, lib)
            elif e == "save":
                std.save(stack, globalVariables)
            else:
                newInstructions, scoped = lib[e]
                if scoped:
                    stack.append(e)
                else:
                    exec(newInstructions.copy(), stack)
        elif e == "def":
            handleDef(instructions)
        elif e == "impl":
            handleImpl(instructions)
        elif e == "var":
            handleVar(instructions)
        elif e == ".":
            std.stop(stack)
        else:
            e = castIfAvailable(e)
            stack.append(e)

if __name__ == "__main__":
    instructions = []
    filename = sys.argv[1]

    with open(filename) as file:
        for line in file:
            if not line.startswith("#"):
                instructions.extend(shlex.split(line))

    stack = []

    exec(instructions, stack)
