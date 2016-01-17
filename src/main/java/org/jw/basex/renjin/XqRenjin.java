package org.jw.basex.renjin;

import static org.basex.query.QueryError.castError;

import java.text.ParseException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
import org.basex.query.value.item.Str;
import org.basex.query.value.node.FElem;
import org.basex.query.value.type.FuncType;
import org.basex.query.value.type.SeqType;
import org.basex.query.var.VarScope;
import org.basex.util.InputInfo;
import org.renjin.eval.EvalException;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * Represents a passable R engine object. 
 * 
 * @author James Wright
 *
 */
public class XqRenjin extends FItem implements XQFunction {

	protected XqRenjin(FuncType type, AnnList anns) {
		super(type, anns);
	}
	
	public XqRenjin() {
		super(SeqType.ANY_FUN, new AnnList());
		initialize();
	}

	public static final ValueBuilder empty = new ValueBuilder();
    private ScriptEngineManager man = new ScriptEngineManager();
	private ScriptEngine renjin;

	public void initialize() {
		renjin = man.getEngineByName("Renjin");

		if (renjin == null) {
			throw new RuntimeException("Failed to initialize renjin R engine.");
		}
	}
	
	public ScriptEngine getEngine() {
		return renjin;
	}

	@Override
	public QNm argName(int arg0) {
		return QNm.get("rExpression");
	}

	@Override
	public int arity() {
		return 1;
	}

	@Override
	public QNm funcName() {
		return QNm.get("renjin");
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
		ValueBuilder vb = new ValueBuilder();
		for(Value v : args) {
			vb.add(XqRenjinModule.evaluate(this, (String)v.toJava()));
		}
		
		return vb.value();
	}

	@Override
	public int stackFrameSize() {
		return 0;
	}

	@Override
	public FItem coerceTo(FuncType ft, QueryContext qc, InputInfo ii, boolean arg3) throws QueryException {
		if(instanceOf(ft)) return this;
	    throw castError(ii, this, ft);
	}

	@Override
	public void plan(FElem arg0) {	}

	@Override
	public Object toJava() throws QueryException {
		return renjin;
	}

	@Override
	public String toString() {
		return null;
	}

	public void put(String key, Value value) throws QueryException, ScriptException {
		if(value instanceof Str) {
			renjin.put(key, ((ScriptEngine)renjin).eval((String) ((Str)value).toJava()));
		} else {
			renjin.put(key, XqRenjinModule.xQueryValueToRValue(this, value));
		}
	}

	public Value eval(String expression) throws QueryException {
		try {
			Object result = ((ScriptEngine)renjin).eval(expression);
			return XqRenjinModule.rValueToXQueryValue(this, (SEXP) result);
		} catch (EvalException e) {
			throw new QueryException("Failed to evaluate R Expression: '" + expression.toString() + "'. Error: " +
						((Vector)e.getCondition()).getElementAsString(0));
		} catch (org.renjin.parser.ParseException e) {
			throw new QueryException("Failed to evaluate R expression: '" + expression.toString() + "'. Error: " + e.getMessage());
		} catch (ScriptException e) {
			throw new QueryException("Failed to process R expression: '" + expression + "'. Error: " + e.getMessage());
		}		
	}

	public Object get(String key) {
		return renjin.get(key);
	}
}
