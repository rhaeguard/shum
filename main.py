import sys

def is_float(n):
    try:
        float(n)
        return True
    except ValueError:
        return False

def binaryArith(stack, op):
    b = stack.pop()
    a = stack.pop()
    stack.append(op(a, b))

arithmetic_ops = {
    "+": lambda a, b: a + b,
    "-": lambda a, b: a - b,
    "*": lambda a, b: a * b,
    "//": lambda a, b: a // b,
    "/": lambda a, b: a / b,
    "pow": lambda a, b: a ** b,
    "<": lambda a, b: a < b,
    "<=": lambda a, b: a <= b
}

lib = {
    # name => (instructions, isScoped)
    "*": ([], False),
    "dup": ([], False),
    "show": ([], False),
    "ifelse": ([], False)
}

def handleFunctionCreation(instructions, stack):
    name = instructions.pop(0)
    v = instructions.pop(0)
    if v != "=":
        raise Exception(f"Should be '=', but was '{v}'")

    funcIns = []
    isScoped = False
    while True:
        ins = instructions.pop(0)
        if ins == "scope" and funcIns == []:
            isScoped = True
            continue
        
        if ins not in ["end"]:
            funcIns.append(ins)
        else:
            break
    lib[name] = (funcIns, isScoped)

def exec(instructions, stack):
    while instructions:
        e = instructions.pop(0)
        print(stack, f'Exec: {e}')
        if e in arithmetic_ops:
            binaryArith(stack, arithmetic_ops[e])
        elif e in lib:
            if e == "show":
                print(stack[-1])
            elif e == "dup":
                stack.append(stack[-1])
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
            handleFunctionCreation(instructions, stack)
        else:
            if e.isnumeric():
                e = int(e)
            elif is_float(e):
                e = float(e)
            stack.append(e)


if __name__ == "__main__":
    instructions = []
    filename = sys.argv[1]

    with open(filename) as file:
        for line in file:
            if not line.startswith("#"):
                instructions.extend(line.split())

    stack = []

    exec(instructions, stack)
