package org.jw.basex.renjin;

import static org.basex.query.QueryError.castError;

import java.util.concurrent.Callable;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.XQFunction;
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
import org.renjin.eval.Context;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

public class XqRenjinFunction extends FItem implements XQFunction {
	
	Function func;
	XqRenjin renjin;

	protected XqRenjinFunction(FuncType type, AnnList anns) {
		super(type, anns);
	}

	public XqRenjinFunction(XqRenjin renjinIn, Function in) {
		super(SeqType.ANY_FUN, new AnnList());
		renjin = renjinIn;
		func = in;
	}

	@Override
	public QNm argName(int arg0) {
		return QNm.get("arguments");
	}

	@Override
	public int arity() {
		return !(func instanceof Closure) ? 0 : 
			((Closure)func).getFormals().length();
	}

	@Override
	public QNm funcName() {
		return QNm.get("renjin_function");
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
		return (Item) invValue(qc, ii, args);
	}

	@Override
	public Value invValue(QueryContext qc, InputInfo ii, Value... args) throws QueryException {
		RenjinScriptEngine  engine = (RenjinScriptEngine) renjin.getEngine();
		Context ctx = engine.getTopLevelContext();
		
		PairList.Builder arguments = new PairList.Builder();
		for(Value v : args) {
			if(v.size() > 0) {
				arguments.add(XqRenjinModule.xQueryValueToRValue(renjin, v));
			}
		}
		
		FunctionCall call = new FunctionCall(func, arguments.build());	
		SEXP result = ctx.evaluate(call);
		
		return XqRenjinModule.rValueToXQueryValue(renjin, result);
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
	public void plan(FElem arg0) {	}

	@Override
	public Object toJava() throws QueryException {
		return this;
	}

	@Override
	public String toString() {
		return func.toString();
	}
}
