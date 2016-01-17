# xq-renjin

R integration for XQuery 3.1 and BaseX leveraging the [renjin][3] JVM R interpreter.

## Why?

Use R in XQuery scripts and services or test new R code with XQuery! Additionally, with the 
prevlelance of R in data science and academia. Algorithms and libraries exist in the R space that would be a
shame to not use in your next XQuery project.

## Installation
Copy the ``xq-renjin-x.jar`` into your ``basex\lib`` directory 

Or use [xqpm][3] to do it for you:
```
xqpm xq-renjin
```

### Declaration
To use the module in your scripts, import it:

```xquery
import module namespace r = 'http://github.com/james-jw/xq-renjin';
```

### Version 0.1-BETA
This module is currently in Beta and should be used with caution. Especially in scenarios involving the
writing of sensitive data. 

### Dependencies
This module currently require's [BaseX][0]

## Usage 
This module is not intended to help write better 'R', but leverage it.

Let me know if any other module works for you!

### Interop

The module, `xq-renjin` provides a simple mapping between the Renjin Java R engine and BaseX. 
It leverages the new `map` and `array` data types introduced in XQuery 3.1.

### Methods
The module provides a few helper methods for interacting with R objects. 

#### Init

```xquery
init() as function(xs:string)
```

The `init` method constructs a new R engine object for use in executing future R code.

```xquery
import module namespace r = 'http://github.com/james-jw/xq-renjin';
let $r := r:init()
return
   $r
```

If you examine the result of the above expression. You should see that it is a function which accepts a single argument.
```xquery
function rObject#1
```

An R expression can be passed in to evalute further `R` code or retrieve an engine value, for example:

```xquery
let $r := r:init()
return
  $r('(1:20)')
```

The result of the above expression should an XQuery sequence from 1 to 20

#### Run
```xquery
run(engine, exp as xs:string) as function(xs:string)
```

The run command takes an `R` engine object and an expression and evalutes it within the `R` engine. The return value from the `run` command is the engine passed in and NOT the result of the expression executed. This is in contrast to executing an expression using the engine itself as seen in the example above for `init`. 

The benefit however is it allows for easy chaining!

```xquery
import module namespace js = 'http://github.com/james-jw/xq-renjin';
let $r := r:init()
    => r:run('a = (1:20)')
    => r:run('b = (20:1)+rnorm(n=10)')
    => r:run('df = data.frame(a = a, b = b)')
return
  $r('df')
```

In order to provide a seamless scripting experience, all R objects are automatically
mapped to XQuery objects. This allows for the use of the `?` operator when querying objects or arrays:

So in the above example, you could access the a column like so:

```xquery
...
let $data-frame := $r('df')
return
  $data-frame?a
```

Which should print out the sequence from 1 to 20

Additionlly, script objects and XQuery objects are interchangable, including
function items. For example, its possible to pass XQuery functions into R functions and vice versa:

```xquery
...
let $data-frame := $r('df')
let $model := $r('function (exp, data) { lm(exp, data)')
let $summary := $r('summary')
return
  $model('a ~ log(b)', $data-frame)
    => $summary() 
```

#### eval
```xquery
eval(engine, exp as xs:string) as item()*
```

Eval operates exactly like `run` except it returns the result of the expression instead of the engine object. Thus it cannot be chained.

#### put
```xquery
put(engine, name as xs:string, exp as xs:string) as function(xs:string)
```

Put, executes an expression and assigns the value to a named variable in the engine provided. The return value is the engine itself, allowing chaining.

```xquery
let $r := r:init()
   => r:put('a', '(1:20')
   => r:put('b', '(1:20')
return
  r:eval($r, 'summary(lm(a ~ b, data.frame(a = a, b = b)))')
```

### Contribute
If you like what you see, or have any ideas to make it better, feel free to leave feedback, make a pull request, log an issue or simply ask a question.

## Unit Tests
Clone the repo and run ``basex -t`` within the repo's directory to run the unit tests.

## Shout Out!
If you like what you see here please star the repo and follow me on [github][1] or [linkedIn][2]

[0]: http://www.basex.org
[1]: https://github.com/james-jw/xqpm
[2]: https://www.linkedin.com/pub/james-wright/61/25a/101
[3]: http://www.renjin.org/
