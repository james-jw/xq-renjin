package org.jw.basex.renjin;

import static org.basex.query.QueryError.castError;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.XQFunction;
import org.basex.query.iter.ValueIter;
import org.basex.query.util.list.AnnList;
import org.basex.query.value.Value;
import org.basex.query.value.item.FItem;
import org.basex.query.value.item.Item;
import org.basex.query.value.item.QNm;
import org.basex.query.value.node.FElem;
import org.basex.query.value.type.FuncType;
import org.basex.query.value.type.SeqType;
import org.basex.query.var.VarScope;
import org.basex.util.InputInfo;
import org.renjin.sexp.FunctionCall;

public class XqRenjinFunctionCall extends FItem implements XQFunction {
	
	private FunctionCall funcCall;
	private XqRenjin renjin;

	protected XqRenjinFunctionCall(FuncType type, AnnList anns) {
		super(type, anns);
	}

	public XqRenjinFunctionCall(XqRenjin renjinIn, FunctionCall in) {
		super(SeqType.ANY_FUN, new AnnList());
		renjin = renjinIn;
		funcCall = in;
	}

	@Override
	public QNm argName(int arg0) {
		return null;
	}

	@Override
	public int arity() {
		return 0;
	}

	@Override
	public QNm funcName() {
		return QNm.get("rFunction");
	}

	@Override
	public FuncType funcType() {
		return FuncType.get(SeqType.ITEM_ZM, SeqType.ITEM_ZM);
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
		return null;
	}

	@Override
	public FItem coerceTo(FuncType ft, QueryContext arg1, InputInfo ii, boolean arg3) throws QueryException {
		if(instanceOf(ft)) return this;
	    throw castError(ii, this, ft);
	}

	@Override
	public void plan(FElem arg0) {	}

	@Override
	public Object toJava() throws QueryException {
		return funcCall.asString();
	}

	@Override
	public String toString() {
		return funcCall.asString();
	}

	@Override
	public int stackFrameSize() {
		return 0;
	}	

}
