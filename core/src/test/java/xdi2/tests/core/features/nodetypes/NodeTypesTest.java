package xdi2.tests.core.features.nodetypes;

import junit.framework.TestCase;
import xdi2.core.features.nodetypes.XdiAbstractMemberOrdered;
import xdi2.core.features.nodetypes.XdiAbstractMemberUnordered;
import xdi2.core.features.nodetypes.XdiAbstractContext;
import xdi2.core.features.nodetypes.XdiAttributeCollection;
import xdi2.core.features.nodetypes.XdiAttributeSingleton;
import xdi2.core.features.nodetypes.XdiEntityCollection;
import xdi2.core.features.nodetypes.XdiEntitySingleton;
import xdi2.core.xri3.XDI3SubSegment;

public class NodeTypesTest extends TestCase {

	public void testArcXris() throws Exception {

		assertEquals(XdiEntitySingleton.createArcXri(XDI3SubSegment.create("+address")), XDI3SubSegment.create("+address"));
		assertEquals(XdiAttributeSingleton.createArcXri(XDI3SubSegment.create("+address")), XDI3SubSegment.create("<+address>"));
		assertEquals(XdiEntityCollection.createArcXri(XDI3SubSegment.create("+address")), XDI3SubSegment.create("[+address]"));
		assertEquals(XdiAttributeCollection.createArcXri(XDI3SubSegment.create("+address")), XDI3SubSegment.create("[<+address>]"));
		assertEquals(XdiAbstractMemberUnordered.createArcXri("1", true), XDI3SubSegment.create("<!1>"));
		assertEquals(XdiAbstractMemberOrdered.createArcXri("1", true), XDI3SubSegment.create("<#1>"));
		assertEquals(XdiAbstractMemberUnordered.createArcXri("1", false), XDI3SubSegment.create("!1"));
		assertEquals(XdiAbstractMemberOrdered.createArcXri("1", false), XDI3SubSegment.create("#1"));

		assertTrue(XdiEntitySingleton.isValidArcXri(XdiEntitySingleton.createArcXri(XDI3SubSegment.create("+address"))));
		assertFalse(XdiAttributeSingleton.isValidArcXri(XdiEntitySingleton.createArcXri(XDI3SubSegment.create("+address"))));
		assertFalse(XdiEntityCollection.isValidArcXri(XdiEntitySingleton.createArcXri(XDI3SubSegment.create("+address"))));
		assertFalse(XdiAttributeCollection.isValidArcXri(XdiEntitySingleton.createArcXri(XDI3SubSegment.create("+address"))));

		assertFalse(XdiEntitySingleton.isValidArcXri(XdiAttributeSingleton.createArcXri(XDI3SubSegment.create("+address"))));
		assertTrue(XdiAttributeSingleton.isValidArcXri(XdiAttributeSingleton.createArcXri(XDI3SubSegment.create("+address"))));
		assertFalse(XdiEntityCollection.isValidArcXri(XdiAttributeSingleton.createArcXri(XDI3SubSegment.create("+address"))));
		assertFalse(XdiAttributeCollection.isValidArcXri(XdiAttributeSingleton.createArcXri(XDI3SubSegment.create("+address"))));

		assertFalse(XdiEntitySingleton.isValidArcXri(XdiEntityCollection.createArcXri(XDI3SubSegment.create("+address"))));
		assertFalse(XdiAttributeSingleton.isValidArcXri(XdiEntityCollection.createArcXri(XDI3SubSegment.create("+address"))));
		assertTrue(XdiEntityCollection.isValidArcXri(XdiEntityCollection.createArcXri(XDI3SubSegment.create("+address"))));
		assertFalse(XdiAttributeCollection.isValidArcXri(XdiEntityCollection.createArcXri(XDI3SubSegment.create("+address"))));

		assertFalse(XdiEntitySingleton.isValidArcXri(XdiAttributeCollection.createArcXri(XDI3SubSegment.create("+address"))));
		assertFalse(XdiAttributeSingleton.isValidArcXri(XdiAttributeCollection.createArcXri(XDI3SubSegment.create("+address"))));
		assertFalse(XdiEntityCollection.isValidArcXri(XdiAttributeCollection.createArcXri(XDI3SubSegment.create("+address"))));
		assertTrue(XdiAttributeCollection.isValidArcXri(XdiAttributeCollection.createArcXri(XDI3SubSegment.create("+address"))));

		assertEquals(XdiAbstractContext.getBaseArcXri(XdiEntitySingleton.createArcXri(XDI3SubSegment.create("+address"))), XDI3SubSegment.create("+address"));
		assertEquals(XdiAbstractContext.getBaseArcXri(XdiAttributeSingleton.createArcXri(XDI3SubSegment.create("+address"))), XDI3SubSegment.create("+address"));
		assertEquals(XdiAbstractContext.getBaseArcXri(XdiEntityCollection.createArcXri(XDI3SubSegment.create("+address"))), XDI3SubSegment.create("+address"));
		assertEquals(XdiAbstractContext.getBaseArcXri(XdiAttributeCollection.createArcXri(XDI3SubSegment.create("+address"))), XDI3SubSegment.create("+address"));
	}

/*	public void testContextNodes() throws Exception {	

		Graph graph = MemoryGraphFactory.getInstance().openGraph();
		ContextNode contextNode = graph.getRootContextNode().createContextNode(XDI3SubSegment.create("=markus"));

		assertTrue(XdiSubGraph.fromContextNode(contextNode) instanceof XdiCollection);

		XdiCollection printerEntitySingleton = XdiSubGraph.fromContextNode(contextNode).getEntitySingleton(XDI3SubSegment.create("+printer"), true);
		XdiValue telAttributeSingleton = XdiSubGraph.fromContextNode(contextNode).getAttributeSingleton(XDI3SubSegment.create("+tel"), true);
		XdiEntityCollection printerCollection = XdiSubGraph.fromContextNode(contextNode).getEntityCollection(XDI3SubSegment.create("+printer"), true);
		XdiAttributeCollection telCollection = XdiSubGraph.fromContextNode(contextNode).getAttributeCollection(XDI3SubSegment.create("+tel"), true);

		assertTrue(ContextFunctions.isMemberArcXri(printerEntitySingleton.getContextNode().getArcXri()));
		assertTrue(ContextFunctions.isAttributeSingletonArcXri(telAttributeSingleton.getContextNode().getArcXri()));
		assertTrue(ContextFunctions.isEntityCollectionArcXri(printerCollection.getContextNode().getArcXri()));
		assertTrue(ContextFunctions.isAttributeCollectionArcXri(printerCollection.getContextNode().getArcXri()));

		ContextNode printer1ContextNode = printerCollection.createMember().getContextNode();
		ContextNode printer2ContextNode = printerCollection.createMember().getContextNode();
		ContextNode tel1ContextNode = telCollection.createMember().getContextNode();
		ContextNode tel2ContextNode = telCollection.createMember().getContextNode();

		assertFalse(Multiplicity.isEntityMemberArcXri(tel1ContextNode.getArcXri()));
		assertFalse(Multiplicity.isEntityMemberArcXri(tel2ContextNode.getArcXri()));
		assertTrue(ContextFunctions.isMemberArcXri(tel1ContextNode.getArcXri()));
		assertTrue(ContextFunctions.isMemberArcXri(tel2ContextNode.getArcXri()));

		assertTrue(ContextFunctions.isMemberArcXri(printer1ContextNode.getArcXri()));
		assertTrue(ContextFunctions.isMemberArcXri(printer2ContextNode.getArcXri()));
		assertFalse(Multiplicity.isAttributeMemberArcXri(passport1ContextNode.getArcXri()));
		assertFalse(Multiplicity.isAttributeMemberArcXri(passport2ContextNode.getArcXri()));

		assertEquals(printerCollection.membersSize(), 2);
		assertTrue(new IteratorContains<XdiElement>(printerCollection.members(), XdiElement.fromContextNode(printer1ContextNode)).contains());
		assertTrue(new IteratorContains<XdiElement>(printerCollection.members(), XdiElement.fromContextNode(printer2ContextNode)).contains());
		assertEquals(telCollection.membersSize(), 2);
		assertTrue(new IteratorContains<XdiAttributeMember>(telCollection.members(), XdiAttributeMember.fromContextNode(tel1ContextNode)).contains());
		assertTrue(new IteratorContains<XdiAttributeMember>(telCollection.members(), XdiAttributeMember.fromContextNode(tel2ContextNode)).contains());
	}*/
}
