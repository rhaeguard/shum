# shum

It's called шум because it is noisy. I guess.

This is my attempt at creating a simple stack-oriented language. It's implemented in Python. There are a couple of built-in functions and operators supported at the moment.

Supported functions:
- `dup`: duplicates the element at the top of the stack
- `swap`: swaps two elements at the top of the stack
- `show`: prints the element at the top of the stack without popping it
- `drop`: drops the topmost element of the stack
- `ifelse`: given a condition, executes the block corresponding to the True condition
- `+-/*pow//`: arithmetic operators
- `> >= < <= ==`: comparison operators
- `.` exit operator; prints the topmost element of the stack and stops the execution

To create custom functions, you can use `impl` keyword. Functions do not take parameters, they just work with the stack.

```
impl funcName = ...

# passing arguments: reverse polish notation
a b c funcName
```

Anonymous blocks which are not invoked immediately:

```
# sum the number with itself
{ dup + }
```

Anonymous blocks are useful in if/else blocks, or if/else if/else blocks.

```
condition { 1 * } { dup 1 - } ifelse .

condition1 { condition2 { ... } { ... } ifelse } { ... } ifelse .
```

To create named blocks, use `def`

```
def nested = { condition2 { ... } { ... } ifelse }

condition1 nested { ... } ifelse .
```

`def` can also be used for creating variables

```
def name = "Marie"
def age = 23
def height = 1.75
```

# Examples

Hello, world program

```
"Hello, " "World" + show
```

See [examples](./examples/) directory for examples. Please note that in order for the tokens to be recognized there must be at least 1 whitespace between two tokens. For example, this is bad `{drop}`, this is good: `{ drop }`.

# How to run

```
python main.py "example_file_name.uk"
```