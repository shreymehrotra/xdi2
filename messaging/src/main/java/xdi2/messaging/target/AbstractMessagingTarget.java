package xdi2.messaging.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.util.CopyUtil;
import xdi2.core.util.iterators.IteratorListMaker;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.core.xri3.XDI3SubSegment;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.Operation;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.contributor.Contributor;
import xdi2.messaging.target.contributor.ContributorMap;
import xdi2.messaging.target.contributor.ContributorResult;
import xdi2.messaging.target.interceptor.Interceptor;
import xdi2.messaging.target.interceptor.InterceptorList;
import xdi2.messaging.target.interceptor.InterceptorResult;

/**
 * The AbstractMessagingTarget provides the following functionality:
 * - Implementation of execute() with a message envelope (all messages
 *   in the envelope and all operations in the messages will be executed).
 * - Implementation of execute() with a message (all operations
 *   in the messages will be executed).
 * - Support for interceptors and contributors.
 * - Maintaining an "execution context" object where state can be kept between
 *   individual phases.
 * 
 * Subclasses must do the following:
 * - Implement execute() with an operation.
 * 
 * @author markus
 */
public abstract class AbstractMessagingTarget implements MessagingTarget {

	private static final Logger log = LoggerFactory.getLogger(AbstractMessagingTarget.class);

	private XDI3SubSegment ownerPeerRootXri;
	private InterceptorList interceptors;
	private ContributorMap contributors;

	public AbstractMessagingTarget() {

		this.ownerPeerRootXri = null;
		this.interceptors = new InterceptorList();
		this.contributors = new ContributorMap();
	}

	@Override
	public void init() throws Exception {

		if (log.isDebugEnabled()) log.debug("Initializing " + this.getClass().getSimpleName() + " [" + this.getInterceptors().size() + " interceptors: " + this.getInterceptors().stringList() + "] [" + this.getContributors().size() + " contributors: " + this.getContributors().stringList() + "].");

		// init interceptors and contributors

		List<Extension> extensions = new ArrayList<Extension> ();
		extensions.addAll(new IteratorListMaker<Interceptor> (this.getInterceptors().iterator()).list());
		extensions.addAll(new IteratorListMaker<Contributor> (this.getContributors().iterator()).list());

		Collections.sort(extensions, new Extension.InitPriorityComparator());

		for (Extension extension : extensions) {

			if (log.isDebugEnabled()) log.debug("Initializing interceptor/contributor " + extension.getClass().getSimpleName() + ".");

			extension.init(this);
		}
	}

	@Override
	public void shutdown() throws Exception {

		if (log.isDebugEnabled()) log.debug("Shutting down " + this.getClass().getSimpleName() + ".");

		// shutdon interceptors and contributors

		List<Extension> extensions = new ArrayList<Extension> ();
		extensions.addAll(new IteratorListMaker<Interceptor> (this.getInterceptors().iterator()).list());
		extensions.addAll(new IteratorListMaker<Contributor> (this.getContributors().iterator()).list());

		Collections.sort(extensions, new Extension.ShutdownPriorityComparator());

		for (Extension extension : extensions) {

			if (log.isDebugEnabled()) log.debug("Shutting down interceptor/contributor " + extension.getClass().getSimpleName() + ".");

			extension.shutdown(this);
		}
	}

	/**
	 * Executes a message envelope by executing all its messages.
	 * @param messageEnvelope The XDI message envelope containing XDI messages to be executed.
	 * @param messageResult The message result.
	 * @param executionContext An "execution context" object that carries state between
	 * messaging targets, interceptors and contributors.
	 */
	@Override
	public void execute(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		if (messageEnvelope == null) throw new NullPointerException();
		if (messageResult == null) messageResult = new MessageResult();
		if (executionContext == null) executionContext = new ExecutionContext();

		if (log.isDebugEnabled()) log.debug(this.getClass().getSimpleName() + ": Executing message envelope (" + messageEnvelope.getMessageCount() + " messages).");

		try {

			executionContext.pushMessagingTarget(this, null);

			executionContext.pushMessageEnvelope(messageEnvelope, null);

			// reset execution context

			executionContext.resetMessageEnvelopeAttributes();

			// before message envelope

			this.before(messageEnvelope, messageResult, executionContext);

			// execute message envelope interceptors (before)

			InterceptorResult interceptorResultBefore = this.getInterceptors().executeMessageEnvelopeInterceptorsBefore(messageEnvelope, messageResult, executionContext);

			if (interceptorResultBefore.isSkipMessagingTarget()) {

				if (log.isDebugEnabled()) log.debug("Skipping messaging target according to message envelope interceptors (before).");
				return;
			}

			// execute the messages in the message envelope

			Iterator<Message> messages = messageEnvelope.getMessages();

			while (messages.hasNext()) {

				Message message = messages.next();

				this.execute(message, messageResult, executionContext);
			}

			// execute message envelope interceptors (after)

			InterceptorResult interceptorResultAfter = this.getInterceptors().executeMessageEnvelopeInterceptorsAfter(messageEnvelope, messageResult, executionContext);

			if (interceptorResultAfter.isSkipMessagingTarget()) {

				if (log.isDebugEnabled()) log.debug("Skipping messaging target according to message envelope interceptors (after).");
				return;
			}

			// after message envelope

			this.after(messageEnvelope, messageResult, executionContext);

			// execute result interceptors (finish)

			this.getInterceptors().executeResultInterceptorsFinish(messageResult, executionContext);
		} catch (Exception ex) {

			// process exception

			ex = executionContext.processException(ex);

			// execute message envelope interceptors (exception)

			try {

				this.getInterceptors().executeMessageEnvelopeInterceptorsException(messageEnvelope, messageResult, executionContext, (Xdi2MessagingException) ex);
			} catch (Exception ex2) {

				log.warn("Error while messaging envelope interceptor tried to handle exception: " + ex2.getMessage(), ex2);
			}

			// exception in message envelope

			try {

				this.exception(messageEnvelope, messageResult, executionContext, (Xdi2MessagingException) ex);
			} catch (Exception ex2) {

				log.warn("Error while messaging envelope target tried to handle exception: " + ex2.getMessage(), ex2);
			}

			// re-throw it

			throw (Xdi2MessagingException) ex;
		} finally {

			executionContext.popMessageEnvelope();

			executionContext.popMessagingTarget();

			if (log.isDebugEnabled()) log.debug("Trace: " + executionContext.getTraceBlock());
		}
	}

	/**
	 * Executes a message by executing all its operations.
	 * @param message The XDI message containing XDI operations to be executed.
	 * @param messageResult The message result.
	 * @param executionContext An "execution context" object that carries state between
	 * messaging targets, interceptors and contributors.
	 */
	public void execute(Message message, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		if (message == null) throw new NullPointerException();
		if (messageResult == null) throw new NullPointerException();
		if (executionContext == null) throw new NullPointerException();

		if (log.isDebugEnabled()) log.debug(this.getClass().getSimpleName() + ": Executing message (" + message.getOperationCount() + " operations).");

		try {

			executionContext.pushMessage(message, null);

			// reset execution context

			executionContext.resetMessageAttributes();

			// before message

			this.before(message, messageResult, executionContext);

			// execute message interceptors (before)

			InterceptorResult interceptorResultBefore = this.getInterceptors().executeMessageInterceptorsBefore(message, messageResult, executionContext);

			if (interceptorResultBefore.isSkipMessagingTarget()) {

				if (log.isDebugEnabled()) log.debug("Skipping messaging target according to message interceptors (before).");
				return;
			}

			// execute the operations in the message

			Iterator<Operation> operations = message.getOperations();

			while (operations.hasNext()) {

				Operation operation = operations.next();
				operation = Operation.castOperation(operation);

				MessageResult operationMessageResult = new MessageResult();

				this.execute(operation, operationMessageResult, executionContext);

				CopyUtil.copyGraph(operationMessageResult.getGraph(), messageResult.getGraph(), null);
			}

			// execute message interceptors (after)

			InterceptorResult interceptorResultAfter = this.getInterceptors().executeMessageInterceptorsAfter(message, messageResult, executionContext);

			if (interceptorResultAfter.isSkipMessagingTarget()) {

				if (log.isDebugEnabled()) log.debug("Skipping messaging target according to message interceptors (after).");
				return;
			}

			// after message

			this.after(message, messageResult, executionContext);
		} catch (Exception ex) {

			// process exception and re-throw it

			throw executionContext.processException(ex);
		} finally {

			executionContext.popMessage();
		}
	}

	/**
	 * Executes an operation.
	 * @param operation The XDI operation.
	 * @param operationMessageResult The operation's message result.
	 * @param executionContext An "execution context" object that carries state between
	 * messaging targets, interceptors and contributors.
	 */
	public void execute(Operation operation, MessageResult operationMessageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		if (operation == null) throw new NullPointerException();
		if (operationMessageResult == null) throw new NullPointerException();
		if (executionContext == null) throw new NullPointerException();

		if (log.isDebugEnabled()) log.debug(this.getClass().getSimpleName() + ": Executing operation (" + operation.getOperationXri() + ").");

		try {

			executionContext.pushOperation(operation, null);

			// reset execution context

			executionContext.resetOperationAttributes();

			// before operation

			this.before(operation, operationMessageResult, executionContext);

			// execute operation interceptors (before)

			InterceptorResult interceptorResultBefore = this.getInterceptors().executeOperationInterceptorsBefore(operation, operationMessageResult, executionContext);

			if (interceptorResultBefore.isSkipMessagingTarget()) {

				if (log.isDebugEnabled()) log.debug("Skipping messaging target according to operation interceptors (before).");
				return;
			}

			// execute the address or statements in the operation

			XDI3Segment targetAddress = operation.getTargetAddress();
			Iterator<XDI3Statement> targetStatements = operation.getTargetStatements();

			if (targetAddress != null) {

				this.execute(targetAddress, operation, operationMessageResult, executionContext);
			} else if (targetStatements != null) {

				while (targetStatements.hasNext()) {

					XDI3Statement targetStatement = targetStatements.next();

					this.execute(targetStatement, operation, operationMessageResult, executionContext);
				}
			}

			// execute operation interceptors (after)

			InterceptorResult interceptorResultAfter = this.getInterceptors().executeOperationInterceptorsAfter(operation, operationMessageResult, executionContext);

			if (interceptorResultAfter.isSkipMessagingTarget()) {

				if (log.isDebugEnabled()) log.debug("Skipping messaging target according to operation interceptors (after).");
				return;
			}

			// after operation

			this.after(operation, operationMessageResult, executionContext);
		} catch (Exception ex) {

			// process exception and re-throw it

			throw executionContext.processException(ex);
		} finally {

			executionContext.popOperation();
		}
	}

	/**
	 * Executes a target address.
	 * @param targetAddress The target address.
	 * @param operation The XDI operation.
	 * @param operationMessageResult The operation's message result.
	 * @param executionContext An "execution context" object that carries state between
	 * messaging targets, interceptors and contributors.
	 */
	public void execute(XDI3Segment targetAddress, Operation operation, MessageResult operationMessageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		try {

			executionContext.pushTargetAddress(targetAddress, targetAddress.toString());

			// execute target interceptors (address)

			if ((targetAddress = this.getInterceptors().executeTargetInterceptorsAddress(targetAddress, operation, operationMessageResult, executionContext)) == null) {

				return;
			}

			// execute contributors (address)

			ContributorResult contributorResultAddress = this.getContributors().executeContributorsAddress(new XDI3Segment[0], targetAddress, operation, operationMessageResult, executionContext);

			if (contributorResultAddress.isSkipMessagingTarget()) {

				if (log.isDebugEnabled()) log.debug("Skipping messaging target according to contributors (address).");
				return;
			}

			// get an address handler, and execute on the address

			AddressHandler addressHandler = this.getAddressHandler(targetAddress);

			if (addressHandler == null) {

				if (log.isDebugEnabled()) log.debug(this.getClass().getSimpleName() + ": No address handler for target address " + targetAddress + ".");
				return;
			}

			if (log.isDebugEnabled()) log.debug(this.getClass().getSimpleName() + ": Executing " + operation.getOperationXri() + " on target address " + targetAddress + " (" + addressHandler.getClass().getName() + ").");

			addressHandler.executeOnAddress(targetAddress, operation, operationMessageResult, executionContext);
		} catch (Exception ex) {

			// process exception and re-throw it

			throw executionContext.processException(ex);
		} finally {

			executionContext.popTargetAddress();
		}
	}

	/**
	 * Executes a target statement.
	 * @param targetStatement The target statement.
	 * @param operation The XDI operation.
	 * @param operationMessageResult The operation's message result.
	 * @param executionContext An "execution context" object that carries state between
	 * messaging targets, interceptors and contributors.
	 */
	public void execute(XDI3Statement targetStatement, Operation operation, MessageResult operationMessageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		try {

			executionContext.pushTargetStatement(targetStatement, targetStatement.toString());

			// execute target interceptors (statement)

			if ((targetStatement = this.getInterceptors().executeTargetInterceptorsStatement(targetStatement, operation, operationMessageResult, executionContext)) == null) {

				return;
			}

			// execute contributors (statement)

			ContributorResult contributorResultAddress = this.getContributors().executeContributorsStatement(new XDI3Segment[0], targetStatement, operation, operationMessageResult, executionContext);

			if (contributorResultAddress.isSkipMessagingTarget()) {

				if (log.isDebugEnabled()) log.debug("Skipping messaging target according to contributors (statement).");
				return;
			}

			// get a statement handler, and execute on the statement

			StatementHandler statementHandler = this.getStatementHandler(targetStatement);

			if (statementHandler == null) {

				if (log.isDebugEnabled()) log.debug(this.getClass().getSimpleName() + ": No statement handler for target statement " + targetStatement + ".");
				return;
			}

			if (log.isDebugEnabled()) log.debug(this.getClass().getSimpleName() + ": Executing " + operation.getOperationXri() + " on target statement " + targetStatement + " (" + statementHandler.getClass().getName() + ").");

			statementHandler.executeOnStatement(targetStatement, operation, operationMessageResult, executionContext);
		} catch (Exception ex) {

			// process exception and re-throw it

			throw executionContext.processException(ex);
		} finally {

			executionContext.popTargetStatement();
		}
	}

	/*
	 * These are for being overridden by subclasses
	 */

	public void before(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

	}

	public void before(Message message, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

	}

	public void before(Operation operation, MessageResult operationMessageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

	}

	public void after(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

	}

	public void after(Message message, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

	}

	public void after(Operation operation, MessageResult operationMessageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

	}

	public void exception(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext, Xdi2MessagingException ex) throws Xdi2MessagingException {

	}

	public AddressHandler getAddressHandler(XDI3Segment targetAddress) throws Xdi2MessagingException {

		return null;
	}

	public StatementHandler getStatementHandler(XDI3Statement targetStatement) throws Xdi2MessagingException {

		return null;
	}

	/*
	 * Getters and setters
	 */

	@Override
	public XDI3SubSegment getOwnerPeerRootXri() {

		return this.ownerPeerRootXri;
	}

	public void setOwnerPeerRootXri(XDI3SubSegment ownerPeerRootXri) {

		this.ownerPeerRootXri = ownerPeerRootXri;
	}

	public InterceptorList getInterceptors() {

		return this.interceptors;
	}

	public void setInterceptors(InterceptorList interceptors) {

		this.interceptors = interceptors;
	}

	public ContributorMap getContributors() {

		return this.contributors;
	}

	public void setContributors(ContributorMap contributors) {

		this.contributors = contributors;
	}
}
