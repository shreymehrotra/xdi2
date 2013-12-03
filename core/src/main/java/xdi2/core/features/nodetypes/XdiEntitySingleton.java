package xdi2.core.features.nodetypes;

import java.util.Iterator;

import xdi2.core.ContextNode;
import xdi2.core.constants.XDIConstants;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.core.util.iterators.NotNullIterator;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3SubSegment;

/**
 * An XDI entity singleton (context function), represented as a context node.
 * 
 * @author markus
 */
public final class XdiEntitySingleton extends XdiAbstractSingleton<XdiEntity> implements XdiEntity {

	private static final long serialVersionUID = 7600443284706530972L;

	protected XdiEntitySingleton(ContextNode contextNode) {

		super(contextNode);
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a context node is a valid XDI entity singleton.
	 * @param contextNode The context node to check.
	 * @return True if the context node is a valid XDI entity singleton.
	 */
	public static boolean isValid(ContextNode contextNode) {

		if (contextNode == null) return false;

		return 
				isValidArcXri(contextNode.getArcXri()) &&
				( ! XdiAttributeCollection.isValid(contextNode.getContextNode()) && ! XdiAbstractAttribute.isValid(contextNode.getContextNode()) );
	}

	/**
	 * Factory method that creates an XDI entity singleton bound to a given context node.
	 * @param contextNode The context node that is an XDI entity singleton.
	 * @return The XDI entity singleton.
	 */
	public static XdiEntitySingleton fromContextNode(ContextNode contextNode) {

		if (! isValid(contextNode)) return null;

		return new XdiEntitySingleton(contextNode);
	}

	/*
	 * Methods for XRIs
	 */

	public static XDI3SubSegment createArcXri(XDI3SubSegment arcXri) {

		return arcXri;
	}

	public static boolean isValidArcXri(XDI3SubSegment arcXri) {

		if (arcXri == null) return false;

		if (arcXri.isAttributeXs()) return false;
		if (arcXri.isClassXs()) return false;

		if (! arcXri.hasLiteral() && ! arcXri.hasXRef()) return false;

		if (XDIConstants.CS_PLUS.equals(arcXri.getCs()) || XDIConstants.CS_DOLLAR.equals(arcXri.getCs())) {

		} else if (XDIConstants.CS_EQUALS.equals(arcXri.getCs()) || XDIConstants.CS_AT.equals(arcXri.getCs()) || XDIConstants.CS_STAR.equals(arcXri.getCs())) {

		} else {

			return false;
		}

		return true;
	}

	/**
	 * Returns an XDI inner root based on this XDI entity.
	 * @return The XDI inner root.
	 */
	@Override
	public XdiInnerRoot getXdiInnerRoot(XDI3Segment innerRootPredicateXri, boolean create) {

		return XdiAbstractEntity.getXdiInnerRoot(this, innerRootPredicateXri, create);
	}

	/*
	 * Helper classes
	 */

	public static class MappingContextNodeXdiEntitySingletonIterator extends NotNullIterator<XdiEntitySingleton> {

		public MappingContextNodeXdiEntitySingletonIterator(Iterator<ContextNode> contextNodes) {

			super(new MappingIterator<ContextNode, XdiEntitySingleton> (contextNodes) {

				@Override
				public XdiEntitySingleton map(ContextNode contextNode) {

					return XdiEntitySingleton.fromContextNode(contextNode);
				}
			});
		}
	}
}
