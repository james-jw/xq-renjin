module namespace test = 'http://basex.org/modules/xrenjin-tests';
import module namespace r = 'http://xq-renjin';

declare variable $test:r := r:init();

declare %unit:test function test:vector-expression-test() {
  unit:assert-equals($test:r('c(1:10)'), (1 to 10))
};

declare %unit:test function test:string-vector-test() {
  unit:assert-equals($test:r('"renjin is amazing!"'), 'renjin is amazing!')
};

declare %unit:test function test:double-vector-test() {
  unit:assert-equals($test:r('10.234'), 10.234)
};

declare %unit:test function test:logical-vector-test() {
  unit:assert-equals($test:r('c(TRUE, FALSE)'), (true(), false()))
};


declare %unit:test function test:r-function-arity() {
  let $lm := $test:r('function (exp, df) { (lm(exp, df)) }') 
  let $simple-lm := $test:r('function (df) { lm("x ~ y", df) }')
  return (
    unit:assert-equals(2, function-arity($lm)),
    unit:assert-equals(1, function-arity($simple-lm))
  )
};

declare %unit:test function test:complex-number-test() {
  unit:assert-equals($test:r('-1+0i'), '-1+0i')
};

declare %unit:test function test:pi() {
  unit:assert-equals($test:r('pi'), 3.141592653589793)
};


(:
let $r := r:init()
let $names := $r('names <- sample(c("John", "Beth", "Amy", "Lawernce", "Sue"), 15, replace=TRUE)')
let $age := $r('ages <- sample(1:90, 15, replace=TRUE)')
let $data := r:run($r,'d1 <- data.frame(a = 1:15,b = 1:15)')
    => r:run('ages <- sample(1:90, 15000, replace=TRUE)')
    => r:run('height <- sample(1:269, 15000, replace=TRUE)')
    => r:run('d2 <- data.frame(a = letters[1:15],b = letters[1:15])')
    => r:eval('data <- data.frame(cbind(d1, d2, data.frame(ages = ages, height = height)))')
return 
      let $lm := $r('function (exp, df) { (lm(exp, df)) }')
      let $summary := $r('summary')(?, ())
      return
        let $df := csv:parse(file:read-text('C:\Users\SESA221445\Downloads\train.csv'), map { 'header': true(), 'format': 'map'} )
        return $lm('Fare ~ Age', $df?*) =>
      
          $summary((1 to 1000)),
          $lm('ages ~ height', $data) => $summary()
        ) ! local:as-map(.), 
        $r('print(ts(62:1, c(2009, 1), c(2014, 1), 12))')
      :)
         
         