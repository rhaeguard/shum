def show(stack):
    print(stack[-1])

def dup(stack):
    stack.append(stack[-1])

def swap(stack):
    stack[-1], stack[-2] = stack[-2], stack[-1]

def drop(stack):
    stack.pop()

def ifelse(stack, exec, lib):
    elseClause = stack.pop()
    ifClause = stack.pop()
    ifCond = stack.pop()
    if ifCond:
        exec(lib[ifClause][0].copy(), stack)
    else:
        exec(lib[elseClause][0].copy(), stack)

def save(stack, globalVariables):
    varName = stack.pop()
    value = stack[-1]
    if varName not in globalVariables:
        raise Exception(f"Variable '{varName}' is not defined")
    globalVariables[varName] = value

def stop(stack):
    if stack:
        print(stack[-1])
    exit(0)