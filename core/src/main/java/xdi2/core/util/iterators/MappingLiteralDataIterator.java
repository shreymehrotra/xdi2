package xdi2.core.util.iterators;

import java.util.Iterator;

import xdi2.core.Literal;

/**
 * A MappingIterator that maps XDI literals to their literal datas.
 * 
 * @author markus
 */
public class MappingLiteralDataIterator extends MappingIterator<Literal, Object> {

	public MappingLiteralDataIterator(Iterator<Literal> literals) {

		super(literals);
	}

	@Override
	public Object map(Literal literal) {

		return literal.getLiteralData();
	}
}
