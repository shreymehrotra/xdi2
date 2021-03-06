package xdi2.messaging.target.interceptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.util.iterators.SelectingClassIterator;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.Operation;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.Prototype;

public class InterceptorList implements Iterable<Interceptor>, Prototype<InterceptorList>, Serializable {

	private static final long serialVersionUID = -2532712738486475044L;

	private static final Logger log = LoggerFactory.getLogger(InterceptorList.class);

	private List<Interceptor> interceptors;

	public InterceptorList() {

		super();

		this.interceptors = new ArrayList<Interceptor> ();
	}

	public void addInterceptor(Interceptor interceptor) {

		this.interceptors.add(interceptor);
	}

	@SuppressWarnings("unchecked")
	public <T extends Interceptor> T getInterceptor(Class<T> clazz) {

		for (Interceptor interceptor : this.interceptors) {

			if (clazz.isAssignableFrom(interceptor.getClass())) return (T) interceptor;
		}

		return null;
	}

	public void removeInterceptor(Interceptor interceptor) {

		this.interceptors.remove(interceptor);
	}

	public boolean isEmpty() {

		return this.interceptors.isEmpty();
	}

	public int size() {

		return this.interceptors.size();
	}

	@Override
	public Iterator<Interceptor> iterator() {

		return this.interceptors.iterator();
	}

	public String stringList() {

		StringBuffer buffer = new StringBuffer();

		for (Interceptor interceptor : this.interceptors) {

			if (buffer.length() > 0) buffer.append(",");
			buffer.append(interceptor.getClass().getSimpleName());
		}

		return buffer.toString();
	}

	/*
	 * Methods for executing interceptors
	 */

	public InterceptorResult executeMessageEnvelopeInterceptorsBefore(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		InterceptorResult interceptorResultBefore = InterceptorResult.DEFAULT;

		for (Iterator<MessageEnvelopeInterceptor> messageEnvelopeInterceptors = this.findMessageEnvelopeInterceptors(); messageEnvelopeInterceptors.hasNext(); ) {

			MessageEnvelopeInterceptor messageEnvelopeInterceptor = messageEnvelopeInterceptors.next();

			if (! messageEnvelopeInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (before).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (before).");

			try {

				executionContext.pushInterceptor(messageEnvelopeInterceptor, "MessageEnvelopeInterceptor: before");

				InterceptorResult interceptorResult = messageEnvelopeInterceptor.before(messageEnvelope, messageResult, executionContext);
				interceptorResultBefore = interceptorResultBefore.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling message envelope interceptors (before) according to " + messageEnvelopeInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultBefore;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultBefore;
	}

	public InterceptorResult executeMessageEnvelopeInterceptorsAfter(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		InterceptorResult interceptorResultAfter = InterceptorResult.DEFAULT;

		for (Iterator<MessageEnvelopeInterceptor> messageEnvelopeInterceptors = this.findMessageEnvelopeInterceptors(); messageEnvelopeInterceptors.hasNext(); ) {

			MessageEnvelopeInterceptor messageEnvelopeInterceptor = messageEnvelopeInterceptors.next();

			if (! messageEnvelopeInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (after).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (after).");

			try {

				executionContext.pushInterceptor(messageEnvelopeInterceptor, "MessageEnvelopeInterceptor: after");

				InterceptorResult interceptorResult = messageEnvelopeInterceptor.after(messageEnvelope, messageResult, executionContext);
				interceptorResultAfter = interceptorResultAfter.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling message envelope interceptors (after) according to " + messageEnvelopeInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultAfter;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultAfter;
	}

	public void executeMessageEnvelopeInterceptorsException(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext, Xdi2MessagingException ex) throws Xdi2MessagingException {

		for (Iterator<MessageEnvelopeInterceptor> messageEnvelopeInterceptors = this.findMessageEnvelopeInterceptors(); messageEnvelopeInterceptors.hasNext(); ) {

			MessageEnvelopeInterceptor messageEnvelopeInterceptor = messageEnvelopeInterceptors.next();

			if (! messageEnvelopeInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (exception).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (exception).");

			try {

				executionContext.pushInterceptor(messageEnvelopeInterceptor, "MessageEnvelopeInterceptor: exception");

				messageEnvelopeInterceptor.exception(messageEnvelope, messageResult, executionContext, ex);
			} catch (Exception ex2) {

				throw executionContext.processException(ex2);
			} finally {

				executionContext.popInterceptor();
			}
		}
	}

	public InterceptorResult executeMessageInterceptorsBefore(Message message, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		InterceptorResult interceptorResultBefore = InterceptorResult.DEFAULT;

		for (Iterator<MessageInterceptor> messageInterceptors = this.findMessageInterceptors(); messageInterceptors.hasNext(); ) {

			MessageInterceptor messageInterceptor = messageInterceptors.next();

			if (! messageInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message interceptor " + messageInterceptor.getClass().getSimpleName() + " (before).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message interceptor " + messageInterceptor.getClass().getSimpleName() + " (before).");

			try {

				executionContext.pushInterceptor(messageInterceptor, "MessageInterceptor: before");

				InterceptorResult interceptorResult = messageInterceptor.before(message, messageResult, executionContext);
				interceptorResultBefore = interceptorResultBefore.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling message interceptors (before) according to " + messageInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultBefore;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultBefore;
	}

	public InterceptorResult executeMessageInterceptorsAfter(Message message, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		InterceptorResult interceptorResultAfter = InterceptorResult.DEFAULT;

		for (Iterator<MessageInterceptor> messageInterceptors = this.findMessageInterceptors(); messageInterceptors.hasNext(); ) {

			MessageInterceptor messageInterceptor = messageInterceptors.next();

			if (! messageInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message interceptor " + messageInterceptor.getClass().getSimpleName() + " (after).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message interceptor " + messageInterceptor.getClass().getSimpleName() + " (after).");

			try {

				executionContext.pushInterceptor(messageInterceptor, "MessageInterceptor: after");

				InterceptorResult interceptorResult = messageInterceptor.after(message, messageResult, executionContext);
				interceptorResultAfter = interceptorResultAfter.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling message interceptors (after) according to " + messageInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultAfter;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultAfter;
	}

	public InterceptorResult executeOperationInterceptorsBefore(Operation operation, MessageResult operationMessageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		InterceptorResult interceptorResultBefore = InterceptorResult.DEFAULT;

		for (Iterator<OperationInterceptor> operationInterceptors = this.findOperationInterceptors(); operationInterceptors.hasNext(); ) {

			OperationInterceptor operationInterceptor = operationInterceptors.next();

			if (! operationInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled operation interceptor " + operationInterceptor.getClass().getSimpleName() + " (before).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing operation interceptor " + operationInterceptor.getClass().getSimpleName() + " (before).");

			try {

				executionContext.pushInterceptor(operationInterceptor, "OperationInterceptor: before");

				InterceptorResult interceptorResult = operationInterceptor.before(operation, operationMessageResult, executionContext);
				interceptorResultBefore = interceptorResultBefore.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling operation interceptors (before) according to " + operationInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultBefore;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultBefore;
	}

	public InterceptorResult executeOperationInterceptorsAfter(Operation operation, MessageResult operationMessageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		InterceptorResult interceptorResultAfter = InterceptorResult.DEFAULT;

		for (Iterator<OperationInterceptor> operationInterceptors = this.findOperationInterceptors(); operationInterceptors.hasNext(); ) {

			OperationInterceptor operationInterceptor = operationInterceptors.next();

			if (! operationInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled operation interceptor " + operationInterceptor.getClass().getSimpleName() + " (after).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing operation interceptor " + operationInterceptor.getClass().getSimpleName() + " (after).");

			try {

				executionContext.pushInterceptor(operationInterceptor, "OperationInterceptor: after");

				InterceptorResult interceptorResult = operationInterceptor.after(operation, operationMessageResult, executionContext);
				interceptorResultAfter = interceptorResultAfter.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling operation interceptors (after) according to " + operationInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultAfter;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultAfter;
	}

	public XDI3Segment executeTargetInterceptorsAddress(XDI3Segment targetAddress, Operation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		for (Iterator<TargetInterceptor> targetInterceptors = this.findTargetInterceptors(); targetInterceptors.hasNext(); ) {

			TargetInterceptor targetInterceptor = targetInterceptors.next();

			if (! targetInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled target interceptor " + targetInterceptor.getClass().getSimpleName() + " with operation " + operation.getOperationXri() + " on address " + targetAddress + ".");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing target interceptor " + targetInterceptor.getClass().getSimpleName() + " with operation " + operation.getOperationXri() + " on address " + targetAddress + ".");

			try {

				executionContext.pushInterceptor(targetInterceptor, "TargetInterceptor: address: " + targetAddress);

				targetAddress = targetInterceptor.targetAddress(targetAddress, operation, messageResult, executionContext);

				if (targetAddress == null) {

					if (log.isDebugEnabled()) log.debug("Address has been skipped by interceptor " + targetInterceptor.getClass().getSimpleName() + ".");
					return null;
				}

				if (log.isDebugEnabled()) log.debug("Interceptor " + targetInterceptor.getClass().getSimpleName() + " returned address: " + targetAddress + ".");
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return targetAddress;
	}

	public XDI3Statement executeTargetInterceptorsStatement(XDI3Statement targetStatement, Operation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		for (Iterator<TargetInterceptor> targetInterceptors = this.findTargetInterceptors(); targetInterceptors.hasNext(); ) {

			TargetInterceptor targetInterceptor = targetInterceptors.next();

			if (! targetInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled target interceptor " + targetInterceptor.getClass().getSimpleName() + " with operation " + operation.getOperationXri() + " on statement " + targetStatement + ".");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing target interceptor " + targetInterceptor.getClass().getSimpleName() + " with operation " + operation.getOperationXri() + " on statement " + targetStatement + ".");

			try {

				executionContext.pushInterceptor(targetInterceptor, "TargetInterceptor: statement: " + targetStatement);

				targetStatement = targetInterceptor.targetStatement(targetStatement, operation, messageResult, executionContext);

				if (targetStatement == null) {

					if (log.isDebugEnabled()) log.debug("Statement has been skipped by interceptor " + targetInterceptor.getClass().getSimpleName() + ".");
					return null;
				}

				if (log.isDebugEnabled()) log.debug("Interceptor " + targetInterceptor.getClass().getSimpleName() + " returned statement: " + targetStatement + ".");
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return targetStatement;
	}

	public void executeResultInterceptorsFinish(MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		for (Iterator<MessageResultInterceptor> resultInterceptors = this.findResultInterceptors(); resultInterceptors.hasNext(); ) {

			MessageResultInterceptor resultInterceptor = resultInterceptors.next();

			if (! resultInterceptor.isEnabled()) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled result interceptor " + resultInterceptor.getClass().getSimpleName() + " (finish).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing result interceptor " + resultInterceptor.getClass().getSimpleName() + " (finish).");

			try {

				executionContext.pushInterceptor(resultInterceptor, "MessageResultInterceptor: finish");

				resultInterceptor.finish(messageResult, executionContext);
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}
	}

	/*
	 * Methods for finding interceptors
	 */

	public Iterator<MessageEnvelopeInterceptor> findMessageEnvelopeInterceptors() {

		return new SelectingClassIterator<Interceptor, MessageEnvelopeInterceptor> (this.iterator(), MessageEnvelopeInterceptor.class);
	}

	public Iterator<MessageInterceptor> findMessageInterceptors() {

		return new SelectingClassIterator<Interceptor, MessageInterceptor> (this.iterator(), MessageInterceptor.class);
	}

	public Iterator<OperationInterceptor> findOperationInterceptors() {

		return new SelectingClassIterator<Interceptor, OperationInterceptor> (this.iterator(), OperationInterceptor.class);
	}

	public Iterator<TargetInterceptor> findTargetInterceptors() {

		return new SelectingClassIterator<Interceptor, TargetInterceptor> (this.iterator(), TargetInterceptor.class);
	}

	public Iterator<MessageResultInterceptor> findResultInterceptors() {

		return new SelectingClassIterator<Interceptor, MessageResultInterceptor> (this.iterator(), MessageResultInterceptor.class);
	}

	/*
	 * Prototype
	 */

	@SuppressWarnings("unchecked")
	@Override
	public InterceptorList instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// create new interceptor list

		InterceptorList interceptorList = new InterceptorList();

		// add interceptors

		for (Interceptor interceptor : this.interceptors) {

			if (! (interceptor instanceof Prototype<?>)) {

				throw new Xdi2MessagingException("Cannot use interceptor " + interceptor.getClass().getSimpleName() + " as prototype.", null, null);
			}

			try {

				Prototype<? extends Interceptor> interceptorPrototype = (Prototype<? extends Interceptor>) interceptor;
				Interceptor prototypedInterceptor = prototypingContext.instanceFor(interceptorPrototype);

				interceptorList.addInterceptor(prototypedInterceptor);
			} catch (Xdi2MessagingException ex) {

				throw new Xdi2MessagingException("Cannot instantiate interceptor for prototype " + interceptor.getClass().getSimpleName() + ": " + ex.getMessage(), ex, null);
			}
		}

		// done

		return interceptorList;
	}
}
