# shum

It's called шум because it is noisy. I guess.

This is my attempt at creating a simple stack-oriented language. It's implemented in Java and translates the written Shum code to JVM bytecode. There are a couple of built-in functions and operators supported at the moment.

## Running

Run the [build.sh](./build.sh) script. It creates the docker image and runs it. You end up in the bash after a successful execution.

You can try running example `shum` files as follows:
```
bash>./shum ./examples/hello_world.shum
bash>./shum ./examples/fibonacci.shum
```

Obviously, you can create your own files and run them in the same way.

## Documentation

Shum is a stack-oriented language, meaning conceptually there exists a stack and we simply push and pop elements onto that stack. For example, to add two numbers `a` and `b`, we should push `a` first, then `b`, and finally execute `add` operation, The result will be pushed onto the stack as well. Shum uses the [Reverse Polish Notation](https://en.wikipedia.org/wiki/Reverse_Polish_notation). Shum is implemented in Java 17 and it produces JVM bytecode.

## Simple operations

Specifying a constant value simply pushes that value onto the stack.

```
        // stack: [] <- top of the stack
1       // stack: [1]
2       // stack: [1 2]
"Hello" // stack: [1 2 "Hello"]
len     // stack: [1 2 5] => because len("Hello") = 5 and it got pushed onto the stack
+       // stack: [1 7] => 2 + 5 = 7
-       // stack: [-6] => 1 - 7
```

Adding two numbers and printing the result:
```
1 2 + print
// the execution happens as follows:
// [1] <- top of the stack
// [1 2]
// [1 2] +
// [] (1 + 2)
// [3] print
// [] => "3"
```

There are quite a bit of built-in functions. 

| function  | signature  | what it does  |
|---|---|---|
| +  | int int -> int  | adds two numbers  |
| -  | int int -> int  | subtracts two numbers  |
| *  | int int -> int  | multiplies two numbers  |
| /  | int int -> int  | performs integer division  |
| %  | int int -> int  | performs mod operation  |
| pow  | int int -> int  | raises number to power  |
| >  | int int -> bool  | greater than  |
| >= | int int -> bool  | greater equal  |
| <  | int int -> bool  | less than  |
| <= | int int -> bool  | less equal  |
| ==  | int int -> bool  | equal  |
| !=  | int int -> bool  | not equal  |
| not  | bool -> bool  | negation  |
| abs  | int -> int  | returns absolute value  |
| neg  | int -> int  | negates an integer  |
| incr  | int -> int  | increments integer  |
| decr  | int -> int  | decrements integer  |
| print  | T -> ()  | prints the value at the top of the stack  |
| dup  | ...  | duplicates the element at the top of the stack  |
| swap  | ...  | swaps two elements at the top of the stack  |
| drop  | ...  | drops the element at the top of the stack  |
| upper  | string -> string  | to uppercase  |
| lower  | string -> string  | to lowercase   |
| trim  | string -> string  | trims  |
| startsWith  | string string -> bool  | if string starts with another string  |
| endsWith  | string string -> bool  | if string ends with another string  |
| isEmpty  | string -> bool  | is string empty?  |
| isBlank  | string -> bool  | is string blank?  |
| len  | string -> int  | length of the string  |
| contains  | string string -> bool  | if string contains another string  |
| substr  | string int int -> string  | substring of a string in range  |
| ++  | string string -> string  | concatenate two strings  |
| isUpper  | string -> bool  | is the string uppercase?  |
| isLower  | string -> bool  | is the string lowercase?  |


## Control-flow: if statements

Syntax:

```
[bool-producing-instructions] if [true branch] (else [false branch])? end 
```

```
1 10 <= if
    1 print
else
    2 print
end

// or with no else

1 10 <= if
    1 print
end
```

We currently do not support `else if` statements, but we can align `if` and `else`s in a way that it will resemble `else if`:
```
1 loop dup 20 <= do
    dup 15 % 0 == if
        "FizzBuzz"
    else dup 5 % 0 == if
        "Buzz"
    else dup 3 % 0 == if
        "Fizz"
    else
        dup
    end end end
    print
    1 +
end
```

## Control-flow: loops

Syntax:

```
[...some instructions] loop [bool-producing-instructions-for-condition] do [loop-body] end
```

```
1 loop dup 20 <= do
    dup print
    1 +
end
```

## Variables

Syntax to declare a variable:

```
let [varName]:[dataType]
```

When a variable is declared, the initial value is `null`. There are two operations we can do with a variable:

- `varName@` - loads the value of the variable onto the stack
- `varName!` - binds the value at the top of the stack to the variable with the given name

The syntax for variable operations is taken from Forth.

For example, to sum numbers from 1 till 15, we can do:

```
let sum:int                 // define a variable called `sum` which is an integer
0 sum!                      // initialize the variable to 0
1 loop dup 15 <= do         // loop till 15
     dup sum@ + sum!        // sum the loop increment with the sum value, and save it back to sum
     1 +                    // increment to advance the loop pointer
end
sum@ print                  // print the result which should be 120
```

Variables defined outside of any function are global, and the ones declared inside of a function are local. Global and local variables can have the same name, and in that case, the local variable will shadow the global one for that function. **Local variables can only be created inside of the main body of the function!** 

A little bit of an exaggerated example of a function using local variables.
```
func sumTill int -> int =
    let till:int
    till            // take the value of the first parameter since it is pushed onto the stack

    let counter:int
    0 counter!

    let sum:int
    0 sum!

    loop counter@ till@ <= do
        counter@ sum@ + sum!
        counter@ 1 + counter!
    end

    sum@
    return
end
```

## Custom functions

We can use the `func` keyword to create functions. Function declarations must end with `end` keyword.
```
func hello = ... end
```

If function expects a parameter, we need to mention the types, and also what it returns (if there's any):

```
func add5 int -> int = 5 + return end
```

The `return` keyword specifies that the function returns a value at the end of its execution. If the function is called somewhere else, and the value produced by the function is needed, then you need to use the `return` keyword.

Some type signatures:

```
func foo = ... // takes nothing returns nothing

func foo int -> int = ... // takes an int and returns an int, like abs() function

func foo int = ... // takes an int and returns nothing

func foo -> int = ... // takes nothing an returns an int

func foo int int int -> int = // takes three int and returns an int
```

## Macros

Macros are pretty much like functions, except we only execute their instructions as a part of the caller method without actually invoking another method. Macros do not take parameters.

```
macro foo = ... end
```