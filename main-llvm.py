import shlex
import sys
from llvmlite import ir


def generate_llvm(instructions, stack):
    module = ir.Module(name=__file__)
    main = ir.Function(module, ir.FunctionType(ir.IntType(32), []), name="main")

    block = main.append_basic_block(name="entry")
    builder = ir.IRBuilder(block)
    while instructions:
        e = instructions.pop(0)
        if e in ["+", "-"]:
            b = stack.pop()
            a = stack.pop()
            if e == "+":
                func = builder.add
            else:
                func = builder.sub
            stack.append(func(a, b, "result"))
        else:
            val = ir.Constant(ir.IntType(32), e)
            stack.append(val)
    builder.ret(stack.pop())

    return module

if __name__ == "__main__":
    # instructions = []
    # filename = sys.argv[1]

    # with open(filename) as file:
    #     for line in file:
    #         if not line.startswith("#"):
    #             instructions.extend(shlex.split(line))

    stack = []
    instructions = [
        10, 20, "+", 5, "-"
    ]

    mod = generate_llvm(instructions, stack)
    print(mod)