package xdi2.messaging.tests.basic;

import junit.framework.TestCase;
import xdi2.core.ContextNode;
import xdi2.core.util.iterators.SingleItemIterator;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.core.xri3.XDI3SubSegment;
import xdi2.messaging.DelOperation;
import xdi2.messaging.GetOperation;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.Operation;
import xdi2.messaging.SetOperation;
import xdi2.messaging.constants.XDIMessagingConstants;

public class BasicTest extends TestCase {

	private static final XDI3Segment SENDER = XDI3Segment.create("=sender");

	private static final XDI3Segment TARGET_ADDRESS = XDI3Segment.create("=markus");
	private static final XDI3Statement TARGET_STATEMENT = XDI3Statement.create("=markus/+friend/=giovanni");

	private static final XDI3Segment CONTEXTNODEXRIS[] = new XDI3Segment[] {
		XDI3Segment.create("=markus+email"),
		XDI3Segment.create("=markus"),
		XDI3Segment.create("=markus+friends"),
		XDI3Segment.create("=markus+name+last")
	};

	public void testMessagingOverview() throws Exception {

		// create a message envelope

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		assertTrue(MessageEnvelope.isValid(messageEnvelope.getGraph()));

		assertFalse(messageEnvelope.getMessageCollections().hasNext());
		assertNull(messageEnvelope.getMessageCollection(SENDER, false));
		assertEquals(messageEnvelope.getMessageCollectionCount(), 0);
		assertFalse(messageEnvelope.getMessages().hasNext());
		assertFalse(messageEnvelope.getMessages(SENDER).hasNext());
		assertEquals(messageEnvelope.getMessageCount(), 0);

		// create a message collection

		MessageCollection messageCollection = messageEnvelope.getMessageCollection(SENDER, true);

		assertTrue(MessageCollection.isValid(messageCollection.getXdiEntityCollection()));

		assertTrue(messageEnvelope.getMessageCollections().hasNext());
		assertNotNull(messageEnvelope.getMessageCollection(SENDER, false));
		assertEquals(messageEnvelope.getMessageCollectionCount(), 1);
		assertFalse(messageEnvelope.getMessages().hasNext());
		assertFalse(messageEnvelope.getMessages(SENDER).hasNext());
		assertEquals(messageEnvelope.getMessageCount(), 0);

		assertFalse(messageCollection.getMessages().hasNext());
		assertEquals(messageCollection.getMessageCount(), 0);

		// create a message

		Message message = messageCollection.createMessage();

		assertTrue(Message.isValid(message.getXdiEntity()));

		assertTrue(messageEnvelope.getMessageCollections().hasNext());
		assertNotNull(messageEnvelope.getMessageCollection(SENDER, false));
		assertEquals(messageEnvelope.getMessageCollectionCount(), 1);
		assertTrue(messageEnvelope.getMessages().hasNext());
		assertTrue(messageEnvelope.getMessages(SENDER).hasNext());
		assertEquals(messageEnvelope.getMessageCount(), 1);

		assertTrue(messageCollection.getMessages().hasNext());
		assertEquals(messageCollection.getMessageCount(), 1);

		assertFalse(message.getOperations().hasNext());
		assertEquals(message.getOperationCount(), 0);

		// create some operations

		ContextNode[] contextNodes = new ContextNode[CONTEXTNODEXRIS.length]; 
		for (int i=0; i<CONTEXTNODEXRIS.length; i++) contextNodes[i] = messageEnvelope.getGraph().setDeepContextNode(CONTEXTNODEXRIS[i]);

		Operation setOperation = message.createSetOperation(contextNodes[0].getXri());
		Operation getOperation = message.createGetOperation(contextNodes[1].getXri());
		Operation delOperation = message.createDelOperation(contextNodes[2].getXri());

		assertTrue(messageCollection.equals(messageEnvelope.getMessageCollection(SENDER, false)));
		assertTrue(message.equals(messageCollection.getMessages().next()));
		assertTrue(setOperation.equals(message.getSetOperations().next()));
		assertTrue(getOperation.equals(message.getGetOperations().next()));
		assertTrue(delOperation.equals(message.getDelOperations().next()));

		assertEquals(messageEnvelope.getMessageCount(), 1);
		assertEquals(messageEnvelope.getOperationCount(), 3);
		assertEquals(messageCollection.getMessageCount(), 1);
		assertEquals(messageCollection.getOperationCount(), 3);
		assertEquals(message.getOperationCount(), 3);
		assertEquals(messageCollection.getSenderXri(), SENDER);
		assertEquals(message.getSenderXri(), SENDER);
		assertEquals(setOperation.getSenderXri(), SENDER);
		assertEquals(getOperation.getSenderXri(), SENDER);
		assertEquals(delOperation.getSenderXri(), SENDER);
		assertTrue(setOperation instanceof SetOperation);
		assertTrue(getOperation instanceof GetOperation);
		assertTrue(delOperation instanceof DelOperation);
	}

	public void testMessagingFromOperationXriAndTargetAddress() throws Exception {

		MessageEnvelope messageEnvelope = MessageEnvelope.fromOperationXriAndTargetAddress(XDIMessagingConstants.XRI_S_SET, TARGET_ADDRESS);
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(XDIMessagingConstants.XRI_S_ANONYMOUS, false);
		Message message = messageCollection.getMessages().next();
		Operation operation = message.getSetOperations().next();

		assertEquals(messageEnvelope.getMessageCount(), 1);
		assertEquals(messageEnvelope.getOperationCount(), 1);
		assertEquals(messageCollection.getMessageCount(), 1);
		assertEquals(messageCollection.getOperationCount(), 1);
		assertEquals(message.getOperationCount(), 1);
		assertEquals(messageCollection.getSenderXri(), XDIMessagingConstants.XRI_S_ANONYMOUS);
		assertEquals(message.getSenderXri(), XDIMessagingConstants.XRI_S_ANONYMOUS);
		assertEquals(operation.getSenderXri(), XDIMessagingConstants.XRI_S_ANONYMOUS);
		assertEquals(operation.getOperationXri(), XDIMessagingConstants.XRI_S_SET);
		assertEquals(operation.getTargetAddress(), TARGET_ADDRESS);
		assertTrue(operation instanceof SetOperation);
	}

	public void testMessagingFromOperationXriAndTargetStatement() throws Exception {

		MessageEnvelope messageEnvelope = MessageEnvelope.fromOperationXriAndTargetStatements(XDIMessagingConstants.XRI_S_SET, new SingleItemIterator<XDI3Statement> (TARGET_STATEMENT));
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(XDIMessagingConstants.XRI_S_ANONYMOUS, false);
		Message message = messageCollection.getMessages().next();
		Operation operation = message.getSetOperations().next();

		assertEquals(messageEnvelope.getMessageCount(), 1);
		assertEquals(messageEnvelope.getOperationCount(), 1);
		assertEquals(messageCollection.getMessageCount(), 1);
		assertEquals(messageCollection.getOperationCount(), 1);
		assertEquals(message.getOperationCount(), 1);
		assertEquals(messageCollection.getSenderXri(), XDIMessagingConstants.XRI_S_ANONYMOUS);
		assertEquals(message.getSenderXri(), XDIMessagingConstants.XRI_S_ANONYMOUS);
		assertEquals(operation.getSenderXri(), XDIMessagingConstants.XRI_S_ANONYMOUS);
		assertEquals(operation.getOperationXri(), XDIMessagingConstants.XRI_S_SET);
		assertEquals(operation.getTargetStatements().next(), TARGET_STATEMENT);
		assertTrue(operation instanceof SetOperation);
	}

	public void testSenderAndRecipientAddress() throws Exception {

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		Message message = messageEnvelope.createMessage(XDI3Segment.create("=sender"));
		message.setFromPeerRootXri(XDI3SubSegment.create("([=]!1111)"));
		message.setToPeerRootXri(XDI3SubSegment.create("([=]!2222)"));
		assertEquals(message.getFromPeerRootXri(), XDI3Segment.create("([=]!1111)"));
		assertEquals(message.getToPeerRootXri(), XDI3Segment.create("([=]!2222)"));
	}
}
