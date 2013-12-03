package xdi2.messaging.target.interceptor.impl.authentication.signature;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.constants.XDIAuthenticationConstants;
import xdi2.core.features.nodetypes.XdiAttribute;
import xdi2.core.features.nodetypes.XdiAttributeSingleton;
import xdi2.core.features.nodetypes.XdiLocalRoot;
import xdi2.core.features.nodetypes.XdiRoot;
import xdi2.core.features.nodetypes.XdiValue;
import xdi2.core.xri3.XDI3Segment;
import xdi2.messaging.Message;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;

/**
 * A Signer that can authenticate an XDI message using a "public key graph",
 * which contains sender addresses and public keys.
 */
public class GraphSignatureAuthenticator extends PublicKeySignatureAuthenticator {

	private static Logger log = LoggerFactory.getLogger(GraphSignatureAuthenticator.class.getName());

	private Graph publicKeyGraph;

	public GraphSignatureAuthenticator(Graph publicKeyGraph) {

		super();

		this.publicKeyGraph = publicKeyGraph;
	}

	public GraphSignatureAuthenticator() {

		super();

		this.publicKeyGraph = null;
	}

	@Override
	public void init(MessagingTarget messagingTarget, AuthenticationSignatureInterceptor authenticationSignatureInterceptor) throws Exception {

		super.init(messagingTarget, authenticationSignatureInterceptor);

		if (this.getPublicKeyGraph() == null && messagingTarget instanceof GraphMessagingTarget) this.setPublicKeyGraph(((GraphMessagingTarget) messagingTarget).getGraph());
		if (this.getPublicKeyGraph() == null) throw new Xdi2MessagingException("No public key graph.", null, null);
	}

	@Override
	public PublicKey getPublicKey(Message message) {

		XDI3Segment senderXri = message.getSenderXri();
		if (senderXri == null) return null;

		// sender peer root

		XdiRoot senderXdiPeerRoot = XdiLocalRoot.findLocalRoot(this.getPublicKeyGraph()).findPeerRoot(senderXri, false);
		senderXdiPeerRoot = senderXdiPeerRoot == null ? null : senderXdiPeerRoot.dereference();

		if (log.isDebugEnabled()) log.debug("Sender peer root: " + senderXdiPeerRoot);

		// look for public key in the graph

		XdiAttribute signaturePublicKeyXdiAttribute = senderXdiPeerRoot == null ? null : XdiAttributeSingleton.fromContextNode(senderXdiPeerRoot.getContextNode().getDeepContextNode(XDIAuthenticationConstants.XRI_S_MSG_SIG_KEYPAIR_PUBLIC_KEY));
		signaturePublicKeyXdiAttribute = signaturePublicKeyXdiAttribute == null ? null : signaturePublicKeyXdiAttribute.dereference();

		XdiValue signaturePublicKeyXdiValue = signaturePublicKeyXdiAttribute == null ? null : signaturePublicKeyXdiAttribute.getXdiValue(false);
		signaturePublicKeyXdiValue = signaturePublicKeyXdiValue == null ? null : signaturePublicKeyXdiValue.dereference();
		
		Literal publicKeyLiteral = signaturePublicKeyXdiValue == null ? null : signaturePublicKeyXdiValue.getContextNode().getLiteral();

		String publicKeyString = publicKeyLiteral == null ? null : publicKeyLiteral.getLiteralDataString();
		if (publicKeyString == null) return null;

		PublicKey publicKey;

		try {

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyString));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			publicKey = keyFactory.generatePublic(keySpec);
		} catch (GeneralSecurityException ex) {

			if (log.isWarnEnabled()) log.warn("Invalid RSA public key " + publicKeyString + ": " + ex.getMessage(), ex);

			return null;
		}

		// done

		return publicKey;
	}

	/*
	 * Getters and setters
	 */

	public Graph getPublicKeyGraph() {
	
		return this.publicKeyGraph;
	}

	public void setPublicKeyGraph(Graph publicKeyGraph) {
	
		this.publicKeyGraph = publicKeyGraph;
	}
}
