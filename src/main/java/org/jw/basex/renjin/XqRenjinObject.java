package org.jw.basex.renjin;

import static org.basex.query.QueryError.castError;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.XQFunction;
import org.basex.query.util.list.AnnList;
import org.basex.query.value.Value;
import org.basex.query.value.ValueBuilder;
import org.basex.query.value.item.FItem;
import org.basex.query.value.item.Item;
import org.basex.query.value.item.QNm;
import org.basex.query.value.node.FElem;
import org.basex.query.value.type.FuncType;
import org.basex.query.value.type.SeqType;
import org.basex.query.var.VarScope;
import org.basex.util.InputInfo;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;
import org.renjin.sexp.Vector.Builder;

public class XqRenjinObject extends FItem implements XQFunction {

	Vector vector; 
	XqRenjin renjin;
	
	public XqRenjinObject(XqRenjin renjinIn, Vector vectorIn) {
		super(SeqType.ANY_FUN, new AnnList());
		vector = vectorIn;
		renjin = renjinIn;
	}

	@Override
	public QNm argName(int arg0) {
		return QNm.get("name");
	}

	@Override
	public int arity() {
		return 1;
	}

	@Override
	public QNm funcName() {
		return QNm.get("rObject");
	}

	@Override
	public FuncType funcType() {
		return FuncType.get(SeqType.STR, SeqType.ITEM_ZM);
	}

	@Override
	public Expr inlineExpr(Expr[] arg0, QueryContext arg1, VarScope arg2, InputInfo arg3) throws QueryException {
		return null;
	}

	@Override
	public Item invItem(QueryContext qc, InputInfo ii, Value... args) throws QueryException {
		return invValue(qc, ii, args).item(qc, ii);
	}

	@Override
	public Value invValue(QueryContext qc, InputInfo ii, Value... args) throws QueryException {
        Value name = args[0];
        SEXP rValue = null;
        int index = vector.getIndexByName((String) name.toJava());
        if(index >= 0) {
        	rValue = vector.getElementAsSEXP(index);
        } else {
        	AttributeMap attrs = vector.getAttributes();
        	if(!(vector instanceof ListVector)) {
        		Vector rows = attrs.getDimNames().getElementAsSEXP(0);
        		Vector columns = attrs.getDimNames().getElementAsSEXP(1);
        		int binSize = vector.length() / columns.length();
        		int colIndex = columns.indexOf(new StringArrayVector(((String)name.toJava())), 0, 0);
        		if(colIndex >= 0) {
        			Builder builder = XqRenjinModule.vectorToBuilder(vector, binSize);
        			for(int i = colIndex * binSize, x = 0; x < binSize; x++, i++) {
        				builder.add(vector.getElementAsSEXP(i));
        			}
        			rValue = builder.build();
        		}
        	} else {
        		rValue = attrs.get((String) name.toJava());
        	}
        }

    	return rValue.length() == 0 ? (new ValueBuilder()).value() : XqRenjinModule.rValueToXQueryValue(renjin, rValue);
	}
	
	public Value names(int index) {
		Vector names = index == 0 ? vector.getNames() : null;
		
		if(names == null || names.length() == 0) {
			Vector dimNames = (Vector) vector.getAttribute(Symbols.DIMNAMES);
			if(dimNames.length() > 1) {
				names = dimNames.getElementAsSEXP(index);
			}
		}
		
		return XqRenjinModule.rValueToXQueryValue(renjin, names);
	}
	
	/**
	 * Returns the column names of the object
	 * @return A sequence of column names
	 */
	public Value names() {
		return names(0);
	}

	/**
	 * Returns the row names of the object
	 * @return A sequence of row names
	 */
	public Value rownames() {
		return names(1);
	}
	
	public Value attributes() {
		AttributeMap attrs = vector.getAttributes();
		return XqRenjinModule.rValueToXQueryValue(renjin, attrs.getNames());
	}

	@Override
	public int stackFrameSize() {
		return 0;
	}

	@Override
	public FItem coerceTo(FuncType ft, QueryContext arg1, InputInfo ii, boolean arg3) throws QueryException {
		if(instanceOf(ft)) return this;
	    throw castError(ii, this, ft);
	}

	@Override
	public void plan(FElem arg0) {
	}

	@Override
	public Object toJava() throws QueryException {
		return toString();
	}

	@Override
	public String toString() {
		return vector.toString();
	}

	public SEXP getVector() {
		return vector;
	}

}
