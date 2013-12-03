package xdi2.core.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.GraphFactory;
import xdi2.core.Literal;
import xdi2.core.Relation;
import xdi2.core.Statement;
import xdi2.core.exceptions.Xdi2GraphException;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.io.MimeType;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;

public abstract class AbstractGraph implements Graph {

	private static final long serialVersionUID = -5285276230236236923L;

	private static final Logger log = LoggerFactory.getLogger(AbstractContextNode.class);

	private GraphFactory graphFactory;
	private String identifier;

	protected AbstractGraph(GraphFactory graphFactory, String identifier) {

		this.graphFactory = graphFactory;
		this.identifier = identifier;
	}

	/*
	 * General methods
	 */

	@Override
	public GraphFactory getGraphFactory() {

		return this.graphFactory;
	}

	@Override
	public String getIdentifier() {

		return this.identifier;
	}

	@Override
	public void clear() {

		this.getRootContextNode().clear();
	}

	@Override
	public boolean isEmpty() {

		return this.getRootContextNode().isEmpty();
	}

	@Override
	public String toString(String format, Properties parameters) {

		if (format == null) format = XDIWriterRegistry.getDefault().getFormat();

		XDIWriter writer = XDIWriterRegistry.forFormat(format, parameters);
		StringWriter buffer = new StringWriter();

		try {

			writer.write(this, buffer);
		} catch (IOException ex) {

			return "[Exception: " + ex.getMessage() + "]";
		}

		return buffer.toString();
	}

	@Override
	public String toString(MimeType mimeType) {

		if (mimeType == null) throw new NullPointerException();

		XDIWriter writer = XDIWriterRegistry.forMimeType(mimeType);
		if (writer == null) throw new Xdi2RuntimeException("Unknown MIME type for XDI serialization: " + mimeType);

		StringWriter buffer = new StringWriter();

		try {

			writer.write(this, buffer);
		} catch (IOException ex) {

			return "[Exception: " + ex.getMessage() + "]";
		}

		return buffer.toString();
	}

	/*
	 * Deep methods
	 */

	@Override
	public ContextNode setDeepContextNode(XDI3Segment contextNodeXri) {

		return this.getRootContextNode().setDeepContextNode(contextNodeXri);
	}

	@Override
	public ContextNode getDeepContextNode(XDI3Segment contextNodeXri) {

		return this.getRootContextNode().getDeepContextNode(contextNodeXri);
	}

	@Override
	public ReadOnlyIterator<ContextNode> getDeepContextNodes(XDI3Segment contextNodeXri) {

		return this.getRootContextNode().getDeepContextNodes(contextNodeXri);
	}

	@Override
	public Relation setDeepRelation(XDI3Segment contextNodeXri, XDI3Segment arcXri, XDI3Segment targetContextNodeXri) {

		return this.getRootContextNode().setDeepRelation(contextNodeXri, arcXri, targetContextNodeXri);
	}

	@Override
	public Relation setDeepRelation(XDI3Segment contextNodeXri, XDI3Segment arcXri, ContextNode targetContextNode) {

		return this.getRootContextNode().setDeepRelation(contextNodeXri, arcXri, targetContextNode);
	}

	@Override
	public Relation getDeepRelation(XDI3Segment contextNodeXri, XDI3Segment arcXri, XDI3Segment targetContextNodeXri) {

		return this.getRootContextNode().getDeepRelation(contextNodeXri, arcXri, targetContextNodeXri);
	}

	@Override
	public Relation getDeepRelation(XDI3Segment contextNodeXri, XDI3Segment arcXri) {

		return this.getRootContextNode().getDeepRelation(contextNodeXri, arcXri);
	}

	@Override
	public ReadOnlyIterator<Relation> getDeepRelations(XDI3Segment contextNodeXri, XDI3Segment arcXri) {

		return this.getRootContextNode().getDeepRelations(contextNodeXri, arcXri);
	}

	@Override
	public ReadOnlyIterator<Relation> getDeepRelations(XDI3Segment contextNodeXri) {

		return this.getRootContextNode().getDeepRelations(contextNodeXri);
	}

	@Override
	public Literal setDeepLiteral(XDI3Segment contextNodeXri, Object literalData) {

		return this.getRootContextNode().setDeepLiteral(contextNodeXri, literalData);
	}

	@Override
	public Literal setDeepLiteralString(XDI3Segment contextNodeXri, String literalData) {

		return this.getRootContextNode().setDeepLiteralString(contextNodeXri, literalData);
	}

	@Override
	public Literal setDeepLiteralNumber(XDI3Segment contextNodeXri, Double literalData) {

		return this.getRootContextNode().setDeepLiteralNumber(contextNodeXri, literalData);
	}

	@Override
	public Literal setDeepLiteralBoolean(XDI3Segment contextNodeXri, Boolean literalData) {

		return this.getRootContextNode().setDeepLiteralBoolean(contextNodeXri, literalData);
	}

	@Override
	public Literal getDeepLiteral(XDI3Segment contextNodeXri, Object literalData) {

		return this.getRootContextNode().getDeepLiteral(contextNodeXri, literalData);
	}

	@Override
	public Literal getDeepLiteralString(XDI3Segment contextNodeXri, String literalData) {

		return this.getRootContextNode().getDeepLiteralString(contextNodeXri, literalData);
	}

	@Override
	public Literal getDeepLiteralNumber(XDI3Segment contextNodeXri, Double literalData) {

		return this.getRootContextNode().getDeepLiteralNumber(contextNodeXri, literalData);
	}

	@Override
	public Literal getDeepLiteralBoolean(XDI3Segment contextNodeXri, Boolean literalData) {

		return this.getRootContextNode().getDeepLiteralBoolean(contextNodeXri, literalData);
	}

	@Override
	public Literal getDeepLiteral(XDI3Segment contextNodeXri) {

		return this.getRootContextNode().getDeepLiteral(contextNodeXri);
	}

	/*
	 * Methods related to statements
	 */

	@Override
	public Statement setStatement(XDI3Statement statementXri) {

		if (log.isTraceEnabled()) log.trace("setStatement(" + statementXri + ")");

		// inner root short notation?

		statementXri = statementXri.fromInnerRootNotation(true);

		// set the statement

		if (statementXri.isContextNodeStatement()) {

			ContextNode contextNode = this.setDeepContextNode(statementXri.getTargetContextNodeXri());

			return contextNode.getStatement();
		} else if (statementXri.isRelationStatement()) {

			Relation relation = this.setDeepRelation(statementXri.getContextNodeXri(), statementXri.getRelationArcXri(), statementXri.getTargetContextNodeXri());

			return relation.getStatement();
		} else if (statementXri.isLiteralStatement()) {

			Literal literal = this.setDeepLiteral(statementXri.getContextNodeXri(), statementXri.getLiteralData());

			return literal.getStatement();
		} else {

			throw new Xdi2GraphException("Invalid statement XRI: " + statementXri);
		}
	}

	@Override
	public Statement getStatement(XDI3Statement statementXri) {

		if (log.isTraceEnabled()) log.trace("getStatement(" + statementXri + ")");

		ContextNode baseContextNode = this.getDeepContextNode(statementXri.getSubject());
		if (baseContextNode == null) return null;

		if (statementXri.isContextNodeStatement()) {

			ContextNode contextNode = baseContextNode.getContextNode(statementXri.getContextNodeArcXri());

			return contextNode == null ? null : contextNode.getStatement();
		} else if (statementXri.isRelationStatement()) {

			Relation relation = baseContextNode.getRelation(statementXri.getRelationArcXri(), statementXri.getTargetContextNodeXri());

			return relation == null ? null : relation.getStatement();
		} else if (statementXri.isLiteralStatement()) {

			Literal literal = baseContextNode.getLiteral(statementXri.getLiteralData());

			return literal == null ? null : literal.getStatement();
		}

		return null;
	}

	@Override
	public boolean containsStatement(XDI3Statement statementXri) {

		if (log.isTraceEnabled()) log.trace("containsStatement(" + statementXri + ")");

		return this.getStatement(statementXri) != null;
	}

	/*
	 * Methods related to transactions
	 */

	@Override
	public boolean supportsTransactions() {

		return false;
	}

	@Override
	public void beginTransaction() {

	}

	@Override
	public void commitTransaction() {

	}

	@Override
	public void rollbackTransaction() {

	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		return this.toString(null, null);
	}

	@Override
	public boolean equals(Object object) {

		if (object == null || ! (object instanceof Graph)) return false;
		if (object == this) return true;

		Graph other = (Graph) object;

		// TODO: do this without serializing to string

		return this.toString(new MimeType("text/xdi;implied=1;ordered=1")).equals(other.toString(new MimeType("text/xdi;implied=1;ordered=1")));
	}

	@Override
	public int hashCode() {

		// TODO: do this without serializing to string

		return this.toString(new MimeType("text/xdi;implied=1;ordered=1")).hashCode();
	}

	@Override
	public int compareTo(Graph other) {

		if (other == null || other == this) return 0;

		// TODO: do this without serializing to string

		String string1 = this.toString(new MimeType("text/xdi;implied=1;ordered=1"));
		String string2 = other.toString(new MimeType("text/xdi;implied=1;ordered=1"));

		return string1.compareTo(string2);
	}
}
