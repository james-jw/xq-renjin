package org.jw.basex.renjin;

import java.util.ArrayList;
import java.util.List;

import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;
import org.renjin.sexp.Vector.Builder;

@SuppressWarnings("rawtypes")
public class XqRenjinFactorBuilder implements Builder {
	
	IntArrayVector.Builder delegate = new IntArrayVector.Builder();
	List<String> levels = new ArrayList<String>();

	@Override
	public Builder add(SEXP in) {
		if((in instanceof StringVector)) {
			StringVector value = (StringVector)in;			
			String str = value.getElementAsString(0);
			
			int index = levels.indexOf(str);
			if(index == -1) {
				index = levels.size();
				levels.add(str);				
			}
			
			// R indexes start at 1 not 0
			delegate.add(index + 1);			
			return this;
		} else {
			return null;
		}
	}

	@Override
	public Builder add(Number in) {
		return null;
	}

	@Override
	public Builder addFrom(SEXP in, int index) {
		return add(in.getElementAsSEXP(index));
	}

	@Override
	public Builder addNA() {
		delegate.addNA();
		return this;
	}

	@Override
	public Vector build() {
		delegate.setAttribute(Symbols.LEVELS, new StringArrayVector(levels.toArray(new String[0])));
		return delegate.build();
	}

	@Override
	public Builder copyAttributesFrom(SEXP arg0) {
		return null;
	}

	@Override
	public Builder copySomeAttributesFrom(SEXP arg0, Symbol... arg1) {
		return null;
	}

	@Override
	public SEXP getAttribute(Symbol sym) {
		return delegate.getAttribute(sym);
	}

	@Override
	public int length() {
		return delegate.length();
	}

	@Override
	public Builder set(int arg0, SEXP arg1) {
		return null;
	}

	@Override
	public Builder setAttribute(String arg0, SEXP arg1) {
		return null;
	}

	@Override
	public Builder setAttribute(Symbol arg0, SEXP arg1) {
		return null;
	}

	@Override
	public Builder setDim(int arg0, int arg1) {
		return null;
	}

	@Override
	public Builder setFrom(int arg0, SEXP arg1, int arg2) {
		return null;
	}

	@Override
	public Builder setNA(int index) {
		return null;
	}

}
