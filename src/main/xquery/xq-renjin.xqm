module namespace renjin = 'http://github.com/james-jw/xq-renjin';
import module namespace r = 'org.jw.basex.renjin.xq-renjin-module'; 

(: Provided an R object, returns a map to traverse the object :)
declare function renjin:map($item) {(
    for $name in r:names($item)
    let $value := $item($name)
    return map {
      $name: 
        if(r:is-r($value)) then renjin:map($value) 
        else if(count($value) > 1) then
          let $rows := r:rownames($item) return
          if(count($rows) > 0) then map:merge(
            for $row at $i in $rows return
            map { $row : $value[$i] }
          )
          else array { $value }
        else $value 
    }
  ) => map:merge()
};

(: Initializes an R engine :)
declare function renjin:init() as function(*) { r:init() };

(: Returns whether an object represents an R object or not. :)
declare function renjin:is($item) as xs:boolean { r:is-r($item) };

(: Column names:)
declare function renjin:names($item) as xs:string* { r:names($item) };

(: Row names :)
declare function renjin:rownames($item) as xs:string* { r:rownames($item) };

(: Executes an R expression within the R engine provided 
 : @return The R engine provided. Useful in chaining.
 :)
declare function renjin:run($renjin, $expression as xs:string) as function(*) { r:run($renjin, $expression) };

(: Exeutes an R expression within the R engine provided, returning its value 
 : @return The result of the R execution as an R object
 :)
declare function renjin:eval($renjin, $expression as xs:string, $map as xs:boolean) as item()* { 
  let $out := r:eval($renjin, $expression)
  return if($map and r:is-r($out)) then renjin:map($out) else $out
};

(: Exeutes an R expression within the R engine provided, returning its value 
 : @return The result of the R execution as a map
 :)
declare function renjin:eval($renjin, $expression as xs:string) {
  renjin:eval($renjin, $expression, true())
};

(: Adds a variables with the provided name and value to the R engine :)
declare function renjin:put($renjin, $var-name as xs:string, $expression as xs:string) as function(*) {
  r:put($renjin, $var-name, $expression)
};
