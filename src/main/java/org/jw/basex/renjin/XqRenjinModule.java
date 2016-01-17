package org.jw.basex.renjin;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.script.ScriptException;

import org.basex.query.QueryException;
import org.basex.query.QueryModule;
import org.basex.query.value.Value;
import org.basex.query.value.ValueBuilder;
import org.basex.query.value.array.Array;
import org.basex.query.value.array.ArrayBuilder;
import org.basex.query.value.item.Bln;
import org.basex.query.value.item.Dbl;
import org.basex.query.value.item.FItem;
import org.basex.query.value.item.Int;
import org.basex.query.value.item.Item;
import org.basex.query.value.item.Str;
import org.basex.query.value.map.Map;
import org.basex.query.value.seq.Seq;
import org.renjin.invoke.reflection.converters.RuntimeConverter;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.ListVector.NamedBuilder;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;
import org.renjin.sexp.Vector.Builder;

/**
 * @author James Wright
 * A lightweight integration between renjin, a JVM R language interpreter
 * with XQuery.
 * 
 */
public class XqRenjinModule extends QueryModule  {
   public static final ValueBuilder empty = new ValueBuilder();

   public FItem init() {
      return new XqRenjin();
   }

   /**
    * Adds the specified named variable to the provided R engine.
    * 
    * @param renjin - R Engine to interact with
    * @param name - Name of the variable to add to the engine
    * @param val - Value or Expression to set the value to
    * @return The R Engine provided. Useful in chaining.
    * @throws QueryException
 * @throws ScriptException 
    */
   public FItem put(FItem renjin, Str name, Value val) throws QueryException, ScriptException {
     ((XqRenjin)renjin).put(name.toJava(), val);
     return renjin;
   }
   
   /**
    * Runs the provided expression in the R engine.
    * 
    * @param renjin - The engine to evaluate the expression in.
    * @param expression - The expression to evaluate
    * @return The R Engine provided. Useful in chaining.
    * @throws QueryException
    */
   public FItem run(FItem renjin, Str expression) throws QueryException {
     ((XqRenjin) renjin).eval(expression.toJava());
     return renjin;
   }
   
   /**
    * Denotes if the provided object is an R engine object.
    * 
    * @param value The value to test for R'ness
    * @return True or false
    */
   public Bln isR(Value value) {
	   return value instanceof XqRenjinObject ? Bln.TRUE : Bln.FALSE;
   }
   
   public Value names(Item value) throws QueryException {
	   if(isR(value) == Bln.FALSE) {
		   throw new QueryException("Cannot provide keys for non 'R' object.");
	   }
	   
	   return ((XqRenjinObject)value).names();
   }
   
   public Value rownames(Item value) throws QueryException {
	   if(isR(value) == Bln.FALSE) {
		   throw new QueryException("Cannot provide keys for non 'R' object.");
	   }
	   
	   return ((XqRenjinObject)value).rownames();
   }
   
   public Value attributes(Item value) throws QueryException {
	   if(isR(value) == Bln.FALSE) {
		   throw new QueryException("Cannot provide attributes for non 'R' object.");
	   }
	   
	   return ((XqRenjinObject)value).attributes();
   }

   /**
    * Runs the provided expression in the R engine returning its result.
    * Same as 'run' except it returns the result of the expression
    * instead of the engine the expression was run in.
    * 
    * @param renjin - The engine to evaluate the expression in.
    * @param expression - The expression to evaluate
    * @return The result of the expression invocation.
    * @throws QueryException
    */
   public Value eval(FItem renjin, Str expression) throws QueryException {
       return evaluate((XqRenjin) renjin, expression.toJava());
   }
   
   public static Value evaluate(XqRenjin renjin, String expression) throws QueryException {
	   Value out = ((XqRenjin) renjin).eval(expression);
	   return out;
   }

   /**
    * Converts an R value (SEXP) into a XQuery value (Value)
    * 
    * @param renjin Engine the value exists in.
    * @param in The value to convert
    * @return The converted value for use in XQuery
    */
	public static Value rValueToXQueryValue(XqRenjin renjin, SEXP in) {
		Value out = empty.value();
		
		if(in != null) {
			if (in instanceof ListVector) {
				out = processListVectorValue(renjin, (ListVector) in);
			} else if (in instanceof AtomicVector) {
				out = processAtomicVectorValue(renjin, (AtomicVector) in);
			} else if (in instanceof Function) {
				out = processFunctionValue(renjin, (Function) in);
			} else if (in instanceof FunctionCall) {
				out = processFunctionCallValue(renjin, (FunctionCall) in);
			}
		}
		
		return out;
	}

	private static Value processFunctionCallValue(XqRenjin renjin, FunctionCall in) {
		return new XqRenjinFunctionCall(renjin, in);
	}

	private static Value processFunctionValue(XqRenjin renjin, Function in) {
		return new XqRenjinFunction(renjin, in);
	}

	private static Value processAtomicVectorValue(XqRenjin renjin, Vector in) {
		ValueBuilder vb = new ValueBuilder();
		boolean completed = false;
		
		if(in.hasAttributes()) {
			AttributeMap attributes = in.getAttributes();
		    Vector dim = attributes.getDim();
		    if (dim != null && dim.length() == 2) {
		    	String clazz = attributes.getString(Symbols.CLASS);
		    	if(clazz != null && clazz.equals("table")) {
		    		vb.add(new XqRenjinObject(renjin, (Vector) in));
		    	} else {
		    		vb.add(new XqRenjinMatrix(renjin, (Vector) in));
		    	}
		    	
		    	completed = true;
		    } else if(attributes.has(Symbols.LEVELS) && in instanceof IntVector) {
		    	vb.add(processFactorVector(in, (StringVector) attributes.get(Symbols.LEVELS)));
		    	completed = true;
		    } else if(attributes.has(Symbols.NAMES)) {
		    	vb.add(new XqRenjinObject(renjin, (Vector) in));
		    	completed = true;
		    }
		}
		
		if(!completed){
			for(int i = 0; i < in.length(); i++) {
				
				if(in.isElementNA(i)) {
					vb.add(empty.value());
				} else if(in instanceof IntVector) {
					vb.add(Int.get(in.getElementAsInt(i)));
				} else if(in instanceof DoubleVector) {
					vb.add(Dbl.get(in.getElementAsDouble(i)));
				} else if(in instanceof StringVector) {
					vb.add(Str.get(in.getElementAsString(i)));
				} else if(in instanceof ComplexVector) {
					vb.add(Str.get(in.getElementAsComplex(i).toString()));
				} else if(in instanceof LogicalVector) {
					Logical v = in.getElementAsLogical(i);
					if(v != null) {
					  vb.add(Bln.get(v.getInternalValue() == 1 ? true : false));
					} else {
					  vb.add(empty.value());
					}
				}
			}
		}
		
		return vb.value();
	}

	private static Value processFactorVector(Vector in, StringVector levels) {
		ValueBuilder vb = new ValueBuilder();
		
		for(int i = 0; i < in.length(); i++) {
			int v = in.getElementAsInt(i);
			vb.add(Str.get(levels.getElementAsString(v - 1)));
		}
		
		return vb.value();
	}

	private static Value processMatrixValue(Matrix matrix) {
		ArrayBuilder ab = new ArrayBuilder();
		ArrayBuilder innerBuilder = null;
		Vector rawVector = matrix.getVector();
		
		int rowIndex = 0;
		for(int i = 0; i < rawVector.length(); i++, rowIndex++) {
			if(innerBuilder == null) {
				innerBuilder = new ArrayBuilder();
			}
			
			if((rowIndex / matrix.getNumRows()) < matrix.getNumRows()) {
				if(rawVector instanceof DoubleVector) {
					innerBuilder.append(Dbl.get(rawVector.getElementAsDouble(i)));
				} else {
					innerBuilder.append(Dbl.get(rawVector.getElementAsInt(i)));
				}
			} else {
				ab.append(innerBuilder.freeze());
				innerBuilder = null;
				rowIndex = 0;
			}
		}
		
		return ab.freeze();
	}

	private static Value processListVectorValue(XqRenjin renjin, ListVector in) {
		return new XqRenjinObject(renjin, in);
	}

    private static Value processDataFrame(XqRenjin renjin, ListVector in) {
    	ValueBuilder vb = new ValueBuilder();
	    StringArrayVector rowNames = (StringArrayVector) in.getNames();
	    vb.add(rValueToXQueryValue(renjin, rowNames));
	    for(int i = 0; i < rowNames.length(); i++) {
	    	String row = rowNames.getElementAsString(i);
	    	vb.add(rValueToXQueryValue(renjin, in.getElementAsVector(row)));
	    }
	    
	    return vb.value();
    }

    public Value get(FItem renjin, Str name) {
      SEXP out = (SEXP)((XqRenjin) renjin).get(name.toJava());
      return rValueToXQueryValue((XqRenjin) renjin, out);
    }
   
    /**
     * Converts an XQuery value (Value) into an R value (SEXP)
     * 
     * @param renjin Engine the value exists in.
     * @param in The value to convert
     * @return The converted value for use in R
     */
    public static SEXP xQueryValueToRValue(XqRenjin renjin, Value val) throws QueryException {
		// TODO Auto-generated method stub
    	SEXP out = null;
    	
    	if(val instanceof XqRenjinObject) {
    		out = ((XqRenjinObject)val).getVector();
    	} else if(val instanceof Map) {
    		out = processXQueryMap(renjin, (Map)val);
    	} else if(val.size() > 1 && val instanceof Seq && val.itemAt(0) instanceof Map) {
    		out = processXQueryAsDataFrame(renjin, val);
    	} 
    	
    	if(out == null && (val instanceof Array || val instanceof Seq)) {
    		out = processXQueryArray(renjin, val);
    	} else if(out == null && val instanceof FItem) {
    		out = processXQueryFunction(renjin, (FItem)val);
    	}
    	
    	if(out == null) {    		
    		try {
    			Object jValue = val.toJava();
    			if(jValue instanceof BigInteger) {
    				jValue = ((BigInteger)jValue).intValue();
    			}
    			
				out = RuntimeConverter.INSTANCE.convertToR(jValue);
			} catch (QueryException e) {
				throw new QueryException("Failed to convert value to R value: " + val.toString());
			}
    	}
    	
		return out;
	}

	private static SEXP processXqSequence(XqRenjin renjin, Value val) {
		
		return null;
	}

	private static SEXP processXQueryFunction(XqRenjin renjin, FItem val) {
		return null;
	}

	private static SEXP processXQueryArray(XqRenjin renjin, Value val) throws QueryException {
		SEXP out = null;
		boolean isArray = false;
		if(val instanceof Array) {
			isArray = true;
			if(((Array)val).get(0) instanceof Map) {
				out = processXQueryAsDataFrame(renjin, val);
			}
		}
	
		Builder builder = xqValueToBuilder(val);
		if(out == null) {
			long size = isArray ? ((Array)val).arraySize() : val.size();
			for(long i = 0; i < size; i++) {
			    Item item = (Item) (isArray ? ((Array)val).get(i) : val.itemAt(i));
				try {
					builder.add(XqRenjinModule.xQueryValueToRValue(renjin, item));
				} catch (QueryException e) {
					throw new QueryException("Failed to process array value: " + item.toString() + ". " + e.getMessage());
				}
			}
			
			out = builder.build();
		}
		
		return out;
	}
	
	private static Builder xqValueToBuilder(Value val) throws QueryException {
		Builder out = null;
			for(Value v : val) {
				if(v != null) {
					Object javaV = v.toJava();
					if(javaV instanceof String) {
						out = new XqRenjinFactorBuilder();
					} else if(javaV instanceof Integer || javaV instanceof BigInteger) {
						out = new IntArrayVector.Builder();
					} else if(javaV instanceof Double) {
						out = new DoubleArrayVector.Builder();
					} else if(javaV instanceof Boolean) {
						out = new LogicalArrayVector.Builder();
					} else if(val instanceof Map) {
						out = new ListVector.NamedBuilder();
					} else if(val instanceof Seq) {
						out = new ListVector.Builder();
					}
					break;
				}
			}
	
		
		return out;
	}

	/**
	 * Provided a sequence of maps, returns a HashMap of column-wise R Vector Builders
	 * for use in constructing an R 'data.frame'.
	 * @param val Value to analyze
	 * @return HashMap of Vector builders by key name.
	 * @throws QueryException
	 */
	private static java.util.Map<String, Builder> constructDataFrameBuilders(Value val) throws QueryException {
        java.util.Map<String, Builder> builders = new HashMap<String, Builder>();
		
		// Ascertain the full set of keys, and determine the Vector builders
		// for each key type.
		for(Item item : val) {
            if(!(item instanceof Map)) {
            	// This function only works with sequences of Maps
            	return null;
            }
            
			Map obj = (Map)item;
			Value keys = obj.keys();
			for(Item key : keys) {
				String k = (String)key.toJava();
				if(!builders.containsKey(k)) {
					Value v = obj.get(key, null);
					
					// We can only ascertain the type if there is a value
					if(v != null) {
						Builder builder = xqValueToBuilder(v);
						builders.put(k, builder);
					}
				}
			}
		}
		
		return builders;
	}
	
	/**
	 * Processes the sequence of Maps as a data.frame
	 * 
	 * @param renjin Engine to create the data frame in
	 * @param val Sequence of Map objects to populate the data frame from.
	 * @return ListVector of column wise R Vectors, representing an R 'data.frame'
	 * @throws QueryException
	 */
	private static SEXP processXQueryAsDataFrame(XqRenjin renjin, Value val) throws QueryException {
		ListVector.NamedBuilder lb = new ListVector.NamedBuilder();
		lb.setAttribute(Symbols.CLASS, new StringArrayVector("data.frame"));	
		lb.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(new Long(val.size()).intValue(), AttributeMap.EMPTY)); 
		java.util.Map<String, Builder> builders = constructDataFrameBuilders(val);
		lb.setAttribute(Symbols.NAMES, new StringArrayVector(builders.keySet().toArray(new String[0])));
		if(builders == null) {
			return null;
		}
		
		// Populate the column-wise vectors with the
		// provided map values
		for(Item v : val) {
			Map obj = (Map)v;
			for(Entry<String, Builder> entry : builders.entrySet()) {
				String key = entry.getKey();
				Builder builder = entry.getValue();
				Value value = obj.get(Str.get(key), null);				
				if(value == null || value.size() == 0) {
					builder.addNA();
				} else {
					builder.add(XqRenjinModule.xQueryValueToRValue(renjin, value));
				}
			}
		}
		
		// Build the data frame
		for(Entry<String, Builder> entry : builders.entrySet()) {
			lb.add(entry.getKey(), entry.getValue().build());
		}
		
		return lb.build();
	}

	private static SEXP processXQueryMap(XqRenjin renjin, Map val) throws QueryException {
		Value keys = val.keys();
		NamedBuilder out = ListVector.newNamedBuilder();
		for(Item key : keys) {
			out.add((String)key.toJava(), xQueryValueToRValue(renjin, val.get(key, null)));
		}
		
		return out.build();
	}

	public static Builder vectorToBuilder(Vector vector, int size) {
		Builder out = null;
		if(vector instanceof StringVector) {
			out = new StringArrayVector.Builder(size);
		} else if(vector instanceof LogicalVector) {
			out = new LogicalArrayVector.Builder(size);
		} else if(vector instanceof IntVector) {
			out = new IntArrayVector.Builder(size);
		} else if(vector instanceof ComplexVector) {
			out = new ComplexVector.Builder(size);
		} else if(vector instanceof DoubleVector) {
			out = new DoubleArrayVector.Builder(size);
		}
		
		return out;
	}
}
