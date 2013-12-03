package xdi2.core.impl.json;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import xdi2.core.ContextNode;
import xdi2.core.Literal;
import xdi2.core.Relation;
import xdi2.core.constants.XDIConstants;
import xdi2.core.impl.AbstractContextNode;
import xdi2.core.impl.AbstractLiteral;
import xdi2.core.util.XDI3Util;
import xdi2.core.util.iterators.DescendingIterator;
import xdi2.core.util.iterators.EmptyIterator;
import xdi2.core.util.iterators.IteratorContains;
import xdi2.core.util.iterators.IteratorListMaker;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3SubSegment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JSONContextNode extends AbstractContextNode implements ContextNode {

	private static final long serialVersionUID = 1222781682444161539L;

	private XDI3SubSegment arcXri;
	private XDI3Segment xri;

	JSONContextNode(JSONGraph graph, JSONContextNode contextNode, XDI3SubSegment arcXri, XDI3Segment xri) {

		super(graph, contextNode);

		this.arcXri = arcXri;
		this.xri = xri;
	}

	/*
	 * Methods related to context nodes of this context node
	 */

	@Override
	public XDI3SubSegment getArcXri() {

		return this.arcXri;
	}

	@Override
	public XDI3Segment getXri() {

		return this.xri;
	}

	@Override
	public ContextNode setContextNode(XDI3SubSegment arcXri) {

		// check validity

		this.setContextNodeCheckValid(arcXri);

		// set the context node

		((JSONGraph) this.getGraph()).jsonSaveToArray(this.getXri().toString(), XDIConstants.XRI_S_CONTEXT.toString(), new JsonPrimitive(arcXri.toString()));

		XDI3Segment xri = XDI3Util.concatXris(this.getXri(), arcXri);

		JSONContextNode contextNode = new JSONContextNode((JSONGraph) this.getGraph(), this, arcXri, xri);

		// set inner root

		this.setContextNodeSetInnerRoot(arcXri, contextNode);

		// done

		return contextNode;
	}

	@Override
	public ContextNode getContextNode(XDI3SubSegment arcXri) {

		JsonObject jsonObject = ((JSONGraph) this.getGraph()).jsonLoad(this.getXri().toString());

		final JsonArray jsonArrayContexts = jsonObject.getAsJsonArray(XDIConstants.XRI_S_CONTEXT.toString());
		if (jsonArrayContexts == null) return null;
		if (jsonArrayContexts.size() < 1) return null;

		if (! new IteratorContains<JsonElement> (jsonArrayContexts.iterator(), new JsonPrimitive(arcXri.toString())).contains()) return null;

		XDI3Segment xri = XDI3Util.concatXris(this.getXri(), arcXri);

		return new JSONContextNode((JSONGraph) JSONContextNode.this.getGraph(), JSONContextNode.this, arcXri, xri);
	}

	@Override
	public ContextNode getDeepContextNode(XDI3Segment contextNodeXri) {

		if (XDIConstants.XRI_S_ROOT.equals(contextNodeXri) && this.isRootContextNode()) return this;

		XDI3Segment parentContextNodeXri = XDI3Util.concatXris(this.getXri(), XDI3Util.parentXri(contextNodeXri, -1));
		XDI3SubSegment arcXri = contextNodeXri.getLastSubSegment();

		JsonObject jsonObject = ((JSONGraph) this.getGraph()).jsonLoad(parentContextNodeXri.toString());

		final JsonArray jsonArrayContexts = jsonObject.getAsJsonArray(XDIConstants.XRI_S_CONTEXT.toString());
		if (jsonArrayContexts == null) return null;
		if (jsonArrayContexts.size() < 1) return null;

		if (! new IteratorContains<JsonElement> (jsonArrayContexts.iterator(), new JsonPrimitive(arcXri.toString())).contains()) return null;

		JSONContextNode contextNode = this;

		for (XDI3SubSegment tempArcXri : contextNodeXri.getSubSegments()) {

			XDI3Segment tempXri = XDI3Util.concatXris(contextNode.getXri(), tempArcXri);

			contextNode = new JSONContextNode((JSONGraph) this.getGraph(), contextNode, tempArcXri, tempXri);
		}

		return contextNode;
	}

	@Override
	public ReadOnlyIterator<ContextNode> getContextNodes() {

		JsonObject jsonObject = ((JSONGraph) this.getGraph()).jsonLoad(this.getXri().toString());

		final JsonArray jsonArrayContexts = jsonObject.getAsJsonArray(XDIConstants.XRI_S_CONTEXT.toString());
		if (jsonArrayContexts == null) return new EmptyIterator<ContextNode> ();
		if (jsonArrayContexts.size() < 1) return new EmptyIterator<ContextNode> ();

		final List<JsonElement> entryList = new IteratorListMaker<JsonElement> (jsonArrayContexts.iterator()).list();

		return new ReadOnlyIterator<ContextNode> (new MappingIterator<JsonElement, ContextNode> (entryList.iterator()) {

			@Override
			public ContextNode map(JsonElement jsonElement) {

				XDI3SubSegment arcXri = XDI3SubSegment.create(((JsonPrimitive) jsonElement).getAsString());
				XDI3Segment xri = XDI3Util.concatXris(JSONContextNode.this.getXri(), arcXri);

				return new JSONContextNode((JSONGraph) JSONContextNode.this.getGraph(), JSONContextNode.this, arcXri, xri);
			}
		});
	}

	@Override
	public void delContextNode(XDI3SubSegment arcXri) {

		ContextNode contextNode = this.getContextNode(arcXri);
		if (contextNode == null) return;

		// delete all relations and incoming relations

		((JSONContextNode) contextNode).delContextNodeDelAllRelations();
		((JSONContextNode) contextNode).delContextNodeDelAllIncomingRelations();

		// delete this context node

		((JSONGraph) this.getGraph()).jsonDelete(contextNode.getXri().toString());
		((JSONGraph) this.getGraph()).jsonDeleteFromArray(this.getXri().toString(), XDIConstants.XRI_S_CONTEXT.toString(), new JsonPrimitive(arcXri.toString()));
	}

	@Override
	public synchronized Relation setRelation(XDI3Segment arcXri, ContextNode targetContextNode) {

		XDI3Segment targetContextNodeXri = targetContextNode.getXri();

		// check validity

		this.setRelationCheckValid(arcXri, targetContextNodeXri);

		// set the relation

		((JSONGraph) this.getGraph()).jsonSaveToArray(this.getXri().toString(), arcXri.toString(), new JsonPrimitive(targetContextNodeXri.toString()));
		((JSONGraph) this.getGraph()).jsonSaveToArray(targetContextNodeXri.toString(), "/" + arcXri.toString(), new JsonPrimitive(this.getXri().toString()));
		
		JSONRelation relation = new JSONRelation(this, arcXri, targetContextNodeXri);

		// done

		return relation;
	}

	@Override
	public ReadOnlyIterator<Relation> getRelations() {

		JsonObject jsonObject = ((JSONGraph) this.getGraph()).jsonLoad(this.getXri().toString());

		final Set<Entry<String, JsonElement>> entrySet = new HashSet<Entry<String, JsonElement>> (jsonObject.entrySet());

		return new DescendingIterator<Entry<String, JsonElement>, Relation> (entrySet.iterator()) {

			@Override
			public Iterator<Relation> descend(Entry<String, JsonElement> entry) {

				if (entry.getKey().startsWith("/")) return null;

				final XDI3Segment arcXri = XDI3Segment.create(entry.getKey());
				if (XDIConstants.XRI_S_CONTEXT.equals(arcXri)) return null;
				if (XDIConstants.XRI_S_LITERAL.equals(arcXri)) return null;

				JsonArray jsonArrayRelations = (JsonArray) entry.getValue();

				final List<JsonElement> entryList = new IteratorListMaker<JsonElement> (jsonArrayRelations.iterator()).list();

				return new ReadOnlyIterator<Relation> (new MappingIterator<JsonElement, Relation> (entryList.iterator()) {

					@Override
					public Relation map(JsonElement jsonElement) {

						XDI3Segment targetContextNodeXri = XDI3Segment.create(((JsonPrimitive) jsonElement).getAsString());

						return new JSONRelation(JSONContextNode.this, arcXri, targetContextNodeXri);
					}
				});
			}
		};
	}

	@Override
	public ReadOnlyIterator<Relation> getIncomingRelations() {

		JsonObject jsonObject = ((JSONGraph) this.getGraph()).jsonLoad(this.getXri().toString());

		final Set<Entry<String, JsonElement>> entrySet = new HashSet<Entry<String, JsonElement>> (jsonObject.entrySet());

		return new DescendingIterator<Entry<String, JsonElement>, Relation> (entrySet.iterator()) {

			@Override
			public Iterator<Relation> descend(Entry<String, JsonElement> entry) {

				if (! entry.getKey().startsWith("/")) return null;

				final XDI3Segment arcXri = XDI3Segment.create(entry.getKey().substring(1));

				JsonArray jsonArrayIncomingRelations = (JsonArray) entry.getValue();

				final List<JsonElement> entryList = new IteratorListMaker<JsonElement> (jsonArrayIncomingRelations.iterator()).list();

				return new ReadOnlyIterator<Relation> (new MappingIterator<JsonElement, Relation> (entryList.iterator()) {

					@Override
					public Relation map(JsonElement jsonElement) {

						XDI3Segment contextNodeXri = XDI3Segment.create(((JsonPrimitive) jsonElement).getAsString());

						ContextNode contextNode = JSONContextNode.this.getGraph().getDeepContextNode(contextNodeXri);

						return new JSONRelation(contextNode, arcXri, JSONContextNode.this.getXri());
					}
				});
			}
		};
	}

	@Override
	public void delRelation(XDI3Segment arcXri, XDI3Segment targetContextNodeXri) {

		// delete the relation

		((JSONGraph) this.getGraph()).jsonDeleteFromArray(this.getXri().toString(), arcXri.toString(), new JsonPrimitive(targetContextNodeXri.toString()));
		((JSONGraph) this.getGraph()).jsonDeleteFromArray(targetContextNodeXri.toString(), "/" + arcXri.toString(), new JsonPrimitive(this.getXri().toString()));

		// delete inner root

		this.delRelationDelInnerRoot(arcXri, targetContextNodeXri);
	}

	@Override
	public Literal setLiteral(Object literalData) {

		// check validity

		this.setLiteralCheckValid(literalData);

		// set the literal

		((JSONGraph) this.getGraph()).jsonSaveToObject(this.getXri().toString(), XDIConstants.XRI_SS_LITERAL.toString(), AbstractLiteral.literalDataToJsonElement(literalData));
		
		JSONLiteral literal = new JSONLiteral(this);

		// done

		return literal;
	}

	@Override
	public Literal getLiteral() {

		JsonObject jsonObject = ((JSONGraph) this.getGraph()).jsonLoad(this.getXri().toString());

		if (! jsonObject.has(XDIConstants.XRI_SS_LITERAL.toString())) return null;

		return new JSONLiteral(this);
	}

	@Override
	public void delLiteral() {

		((JSONGraph) this.getGraph()).jsonDeleteFromObject(this.getXri().toString(), XDIConstants.XRI_SS_LITERAL.toString());
	}
}
