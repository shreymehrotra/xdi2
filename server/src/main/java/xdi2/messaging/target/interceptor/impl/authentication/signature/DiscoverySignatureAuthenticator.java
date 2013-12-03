package xdi2.messaging.target.interceptor.impl.authentication.signature;

import java.security.PublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.Message;

/**
 * A Signer that can authenticate an XDI message by obtaining
 * public keys using XDI discovery.
 */
public class DiscoverySignatureAuthenticator extends PublicKeySignatureAuthenticator {

	private static Logger log = LoggerFactory.getLogger(DiscoverySignatureAuthenticator.class.getName());

	public static final XDIDiscoveryClient DEFAULT_DISCOVERY_CLIENT = new XDIDiscoveryClient();

	private XDIDiscoveryClient xdiDiscoveryClient;

	public DiscoverySignatureAuthenticator(XDIDiscoveryClient xdiDiscoveryClient) {

		super();

		this.xdiDiscoveryClient = xdiDiscoveryClient;
	}

	public DiscoverySignatureAuthenticator() {

		this(DEFAULT_DISCOVERY_CLIENT);
	}

	@Override
	public PublicKey getPublicKey(Message message) {

		XDI3Segment senderXri = message.getSenderXri();
		if (senderXri == null) return null;

		// perform discovery

		PublicKey publicKey = null;

		try {

			XDIDiscoveryResult xdiDiscoveryResult = this.getXdiDiscoveryClient().discover(senderXri, null);

			if (xdiDiscoveryResult != null) publicKey = xdiDiscoveryResult.getSignaturePublicKey();
		} catch (Xdi2ClientException ex) {

			if (log.isWarnEnabled()) log.warn("Cannot discover public key for " + senderXri + ": " + ex.getMessage(), ex);

			return null;
		}

		// done

		return publicKey;
	}

	public XDIDiscoveryClient getXdiDiscoveryClient() {

		return this.xdiDiscoveryClient;
	}

	public void setXdiDiscoveryClient(XDIDiscoveryClient xdiDiscoveryClient) {

		this.xdiDiscoveryClient = xdiDiscoveryClient;
	}
}
