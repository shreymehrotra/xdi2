package xdi2.messaging.target.interceptor;

import xdi2.messaging.MessageResult;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.ExecutionContext;

public abstract class AbstractMessageResultInterceptor extends AbstractInterceptor implements MessageResultInterceptor {

	@Override
	public void finish(MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

	}
}
