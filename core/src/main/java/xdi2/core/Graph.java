package xdi2.core;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Properties;

import xdi2.core.io.MimeType;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;

/**
 * This interface represents a whole XDI graph.
 * XDI graphs consist of context nodes, relations, and literals.
 * Also, an XDI graph can be expressed as a set of XDI statements.
 * 
 * @author markus
 */
public interface Graph extends Serializable, Comparable<Graph>, Closeable {

	/*
	 * General methods
	 */

	/**
	 * Gets the graph factory that created this graph.
	 * @return The graph factory.
	 */
	public GraphFactory getGraphFactory();

	/**
	 * Returns an optional identifier to distinguish graphs from one another.
	 * @return The graph identifier.
	 */
	public String getIdentifier();

	/**
	 * Gets the local root context node of this graph.
	 * @return The graph's local root context node.
	 */
	public ContextNode getRootContextNode();

	/**
	 * Closes the graph. This should be called when work on the graph is done.
	 */
	@Override
	public void close();

	/**
	 * Clears all data from the graph.
	 * This is equivalent to calling getRootContextNode().clear();
	 */
	public void clear();

	/**
	 * Checks if the graph is empty.
	 * This is equivalent to calling getRootContextNode().isEmpty();
	 */
	public boolean isEmpty();

	/**
	 * Converts the graph to a string in the given serialization format.
	 * @param format The serialization format.
	 * @param parameters Parameters for the serialization.
	 */
	public String toString(String format, Properties parameters);

	/**
	 * Converts the graph to a string in the given MIME type.
	 * @param mimeType The MIME type.
	 */
	public String toString(MimeType mimeType);

	/*
	 * Deep methods
	 */

	/**
	 * Deep version of ContextNode.setContextNode(XDI3SubSegment), operates at a context node further down in the graph.
	 */
	public ContextNode setDeepContextNode(XDI3Segment contextNodeXri);

	/**
	 * Deep version of ContextNode.getContextNode(XDI3SubSegment), operates at a context node further down in the graph.
	 */
	public ContextNode getDeepContextNode(XDI3Segment contextNodeXri);

	/**
	 * Deep version of ContextNode.getContextNodes(), operates at a context node further down in the graph.
	 */
	public ReadOnlyIterator<ContextNode> getDeepContextNodes(XDI3Segment contextNodeXri);

	/**
	 * Deep version of ContextNode.setRelation(XDI3Segment, XDI3Segment), operates at a context node further down in the graph.
	 */
	public Relation setDeepRelation(XDI3Segment contextNodeXri, XDI3Segment arcXri, XDI3Segment targetContextNodeXri);

	/**
	 * Deep version of ContextNode.setRelation(XDI3Segment, ContextNode), operates at a context node further down in the graph.
	 */
	public Relation setDeepRelation(XDI3Segment contextNodeXri, XDI3Segment arcXri, ContextNode targetContextNode);

	/**
	 * Deep version of ContextNode.getRelation(XDI3Segment, XDI3Segment), operates at a context node further down in the graph.
	 */
	public Relation getDeepRelation(XDI3Segment contextNodeXri, XDI3Segment arcXri, XDI3Segment targetContextNodeXri);

	/**
	 * Deep version of ContextNode.getRelation(XDI3Segment), operates at a context node further down in the graph.
	 */
	public Relation getDeepRelation(XDI3Segment contextNodeXri, XDI3Segment arcXri);

	/**
	 * Deep version of ContextNode.getRelations(XDI3Segment), operates at a context node further down in the graph.
	 */
	public ReadOnlyIterator<Relation> getDeepRelations(XDI3Segment contextNodeXri, XDI3Segment arcXri);

	/**
	 * Deep version of ContextNode.getRelations(), operates at a context node further down in the graph.
	 */
	public ReadOnlyIterator<Relation> getDeepRelations(XDI3Segment contextNodeXri);

	/**
	 * Deep version of ContextNode.setLiteral(Object), operates at a context node further down in the graph.
	 */
	public Literal setDeepLiteral(XDI3Segment contextNodeXri, Object literalData);

	/**
	 * Deep version of ContextNode.setLiteralString(String), operates at a context node further down in the graph.
	 */
	public Literal setDeepLiteralString(XDI3Segment contextNodeXri, String literalData);

	/**
	 * Deep version of ContextNode.setLiteralNumber(Double), operates at a context node further down in the graph.
	 */
	public Literal setDeepLiteralNumber(XDI3Segment contextNodeXri, Double literalData);

	/**
	 * Deep version of ContextNode.setLiteralBoolean(Boolean), operates at a context node further down in the graph.
	 */
	public Literal setDeepLiteralBoolean(XDI3Segment contextNodeXri, Boolean literalData);

	/**
	 * Deep version of ContextNode.getLiteral(Object), operates at a context node further down in the graph.
	 */
	public Literal getDeepLiteral(XDI3Segment contextNodeXri, Object literalData);

	/**
	 * Deep version of ContextNode.getLiteralString(String), operates at a context node further down in the graph.
	 */
	public Literal getDeepLiteralString(XDI3Segment contextNodeXri, String literalData);

	/**
	 * Deep version of ContextNode.getLiteralNumber(Double), operates at a context node further down in the graph.
	 */
	public Literal getDeepLiteralNumber(XDI3Segment contextNodeXri, Double literalData);

	/**
	 * Deep version of ContextNode.getLiteralBoolean(Boolean), operates at a context node further down in the graph.
	 */
	public Literal getDeepLiteralBoolean(XDI3Segment contextNodeXri, Boolean literalData);

	/**
	 * Deep version of ContextNode.getLiteral(), operates at a context node further down in the graph.
	 */
	public Literal getDeepLiteral(XDI3Segment contextNodeXri);

	/*
	 * Methods related to statements
	 */

	/**
	 * A simple way to set a statement in this graph.
	 */
	public Statement setStatement(XDI3Statement statementXri);

	/**
	 * A simple way to get a statement in this graph.
	 */
	public Statement getStatement(XDI3Statement statementXri);

	/**
	 * A simple way to check if a statement exists in this graph.
	 */
	public boolean containsStatement(XDI3Statement statementXri);

	/*
	 * Methods related to transactions
	 */

	/**
	 * Check if this graph supports transactions.
	 */
	public boolean supportsTransactions();

	/**
	 * Starts a new transaction.
	 */
	public void beginTransaction();

	/**
	 * Commits the changes made by the transaction.
	 */
	public void commitTransaction();

	/**
	 * Rolls back the changes made by the transaction.
	 */
	public void rollbackTransaction();
}
