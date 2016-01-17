package org.jw.basex.renjin;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.value.Value;
import org.basex.query.value.item.FItem;
import org.renjin.eval.Context;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;

/**
 * Implements the Function interface of Renjin allowing R  code to 
 * call supplied XQuery functions
 * 
 * @author James Wright
 *
 */
public class XqQueryFunctionProxy extends Closure {
	
	private FItem delegate;
	private QueryContext xqContext;
	private XqRenjin renjin;

	public XqQueryFunctionProxy(Environment enclosingEnvironment, PairList formals, SEXP body, AttributeMap attributes) {
		super(enclosingEnvironment, formals, body, attributes);
	}

	@Override
	public SEXP apply(Context ctx, Environment env, FunctionCall exp, PairList arguments) {
		Value res;
		SEXP out = null;
		try {
			res = delegate.invokeValue(xqContext, null, XqRenjinModule.rValueToXQueryValue(renjin, arguments));
			out = XqRenjinModule.xQueryValueToRValue(renjin, res);
		} catch (QueryException e) {
			out = new StringArrayVector(e.getMessage());
		}
		
		return out;
	}

	public FItem getDelegate() {
		return delegate;
	}

	public void setDelegate(FItem delegate) {
		this.delegate = delegate;
	}

	public QueryContext getXqContext() {
		return xqContext;
	}

	public void setXqContext(QueryContext xqContext) {
		this.xqContext = xqContext;
	}

	public XqRenjin getRenjin() {
		return renjin;
	}

	public void setRenjin(XqRenjin renjin) {
		this.renjin = renjin;
	}
}
