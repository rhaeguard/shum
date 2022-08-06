import sys
from random import randint
import shlex

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

lib = {
    # name => (instructions, isScoped)
    "dup": ([], False),
    "show": ([], False),
    "ifelse": ([], False),
    "swap": ([], False),
    "drop": ([], False)
}

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
        elif e in lib:
            if e == "show":
                print(stack[-1])
            elif e == "dup":
                stack.append(stack[-1])
            elif e == "swap":
                stack[-1], stack[-2] = stack[-2], stack[-1]
            elif e == "drop":
                stack.pop()
            elif e == "ifelse":
                elseClause = stack.pop()
                ifClause = stack.pop()
                ifCond = stack.pop()
                if ifCond:
                    exec(lib[ifClause][0].copy(), stack)
                else:
                    exec(lib[elseClause][0].copy(), stack)
            else:
                newInstructions, scoped = lib[e]
                if not scoped:
                    exec(newInstructions.copy(), stack)
                else:
                    stack.append(e)
        elif e == "def":
            handleDef(instructions)
        elif e == "impl":
            handleImpl(instructions)
        elif e == ".":
            if stack:
                print(stack[-1])
            exit(0)
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
