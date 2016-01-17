package org.jw.basex.renjin;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.value.Value;
import org.basex.query.value.ValueBuilder;
import org.basex.query.value.item.Dbl;
import org.basex.query.value.item.Int;
import org.basex.query.value.item.Str;
import org.basex.util.InputInfo;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;
import org.renjin.sexp.Vector.Builder;

public class XqRenjinMatrix extends XqRenjinObject {

	Matrix matrix;
	Vector rows, cols;
	boolean isDouble = false;
	
	public XqRenjinMatrix(XqRenjin renjinIn, Vector vectorIn) {
		super(renjinIn, vectorIn);
		
		matrix = new Matrix(vectorIn);
		rows = matrix.getRowNames();
		cols = matrix.getColNames();
		
		isDouble = vectorIn instanceof DoubleVector;
	}
	
	@Override
	public Value names() {
		return names(0);
	}
	
	@Override
	public Value rownames() {
		return names(1);
	}
	
	@Override
	public Value invValue(QueryContext qc, InputInfo ii, Value... args) throws QueryException {
        Value name = args[0];
        int row = -1, col = -1;
        Value out = null;
        
        if(name instanceof Str) {
        	// Retrieve a vector by name
        	StringVector n = new StringArrayVector(((Str)name).toJava());
        	row = rows.indexOf(n, 0, 0);
        	if(row == -1) {
        		col = cols.indexOf(n,  0, 0);
        	}         	
        } else {
        	col = (int) ((Int)name).toJava();
        }
        
        if(row >= 0) {
        	out = getRow(row);
        } else if(col >= 0) {
        	out = getColumn(col);
        } else {
    		throw new QueryException("Invalid index value for matrix: " + name.toString());
    	}
        
    	return out;
	}
	
	private Value getValue(int row, int col) {
		Value value = isDouble ? 
			Dbl.get(matrix.getElementAsDouble(row, col)) :
            Int.get(matrix.getElementAsInt(row, col));
		return value;
	}

	private Value getRow(int row) {
		int columns = matrix.getNumCols();
		ValueBuilder vb = new ValueBuilder();
		
		for(int col = 0; col < columns; col++) {
			vb.add(getValue(row, col));
		}
		
		return vb.value();
	}
	
	private Value getColumn(int col) {
		int rows = matrix.getNumRows();
		ValueBuilder vb = new ValueBuilder();
		
		for(int row = 0; row < rows; row++) {
			vb.add(getValue(row, col));
		}
		
		return vb.value();
	}

}
