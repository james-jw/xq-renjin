# xq-renjin

R integration for XQuery 3.1 and BaseX leveraging the [Renjin][3] JVM R interpreter.

## Why?

Use R in XQuery scripts and services or test new R code with XQuery! Additionally, with the 
prevlelance of R in data science and academia. Algorithms and libraries exist in the R space that would be a
shame to not use in your next XQuery project.

## Installation
Copy the ``xq-renjin-x.jar`` into your ``basex\lib`` directory 

Or use [xqpm][1] to do it for you:
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
init(exp xs:string?) as function(xs:string)
```

The `init` method constructs a new R engine object for use in executing future R code.

```xquery
import module namespace r = 'http://github.com/james-jw/xq-renjin';
let $r := r:init()
return
   $r
```

Additionally, an R expression can be passed in during the initialization
```xquery
let $r := r:init("a <- (1:20)")
return
  $r
```

In the above example, not only was en engine object initalized and returned, but the variable a was added to the engine context.

The returned engine object is a function which accepts a single argument. 
```xquery
function rEngine#1
```

By passing in an R expression, further `R` code can be executed, or objects retrieved. For example:

```xquery
let $r := r:init()
return
  $r('(1:20)')
```

The result of the above expression should be: `1 2 3 4 5 ...`

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

In order to provide a seamless scripting experience, all R objects are often automatically
mapped to XQuery maps or arrays if applicable. This allows for the use of the `?` operator when querying objects or arrays. 

If you expand on the above example, you could access the `a` column with the following:

```xquery
...
let $data-frame := $r('df')
return
  $data-frame?a
```

The output from the above query should be a sequence from 1 to 20. 

Additionlly, script objects and XQuery objects are interchangable, including
function items. For example, its possible to pass XQuery functions into R functions and vice versa:

```xquery
...
let $data-frame := $r('df')
let $model := $r('function (exp, data) { lm(exp, data)')
let $summary := $r('summary')
let $m := 
  $model('a ~ log(b)', $data-frame)
    => $summary() 
    => as-map()
return map {
    "intercept": $m?coefficients("(Intercept)")("Estimate")
    "slope": $m?coefficients("(Intercept)")("Std. Error")
}
```

Note: R's data model does not perfectly map too XQuery's. In some cases R objects will need to be manually unwrapped using the `as-map` function described next, or by simply by caling the R object as a function with the name of the attribute, column, or row to retrieve. This can be seen in the above example.

#### as-map
```xquery
as-map(rObject) as item()*
```

The function as-map will convert any R object and convert it to a map for further inspection. This is useful for function items
and other complex data types which cannot be easily mapped to a single XQuery data type.

If the evaluation of an expression results in `function rObject#1` the use of this function may be beneficial to better understand the resulting object.

#### names
```xquery
names(rObject) as xs:string*
```
Returns the column names of an R Object

#### rownames
```xquery
names(rObject) as xs:string*
```
Returns the row names of an R Object

#### is
```xquery
is(rObject) as xs:boolean
```
Denotes if an object is an R object

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
[3]: http://www.renjin.org
