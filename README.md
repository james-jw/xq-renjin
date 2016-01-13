# xq-renjin
R integration for BaseX leveraging renjin.

```xquery
import module namespace r = 'org.jw.renjin';
let $r := r:init() 
    => r:put("df", "data.frame(x=1:10, y=(1:10)+rnorm(n=10))")
    => r:run("print(df)")
let $makeModel := $r('function (exp, d) { lm(exp, d) }') 
let $model := $makeModel('x ~ y', $data)
return map {
   'intercept': $model?coefficients?1, 
   'slope': $model?coefficients?2
}  
```

// Initialize the engine
r:init() : renjin

// Run a command within the engine
r:run($engine, $expression as xs:string*) : engine

// Adds a variable to the engine from the supplied expression
r:put($engine, $name as xs:string*, $expression as xs:string?) : engine

// Run and return the last call's value
r:eval($engine, $expressions as xs:string*) : xdm:value

renjin(exp as xs:string*) : xdm:value
