package uk.gov.ida.eidas.metatdata;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;

import javax.xml.namespace.QName;
import java.security.PrivateKey;
import java.security.PublicKey;

public class SamlObjectSigner {
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";

    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final String certificate;
    private final SamlObjectMarshaller marshaller;
    private String signatureAlgorithm;

    public SamlObjectSigner(PublicKey publicKey, PrivateKey privateKey, String certificate, String signatureAlgorithm) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.signatureAlgorithm = signatureAlgorithm;
        this.marshaller = new SamlObjectMarshaller();
    }

    public void sign(SignableSAMLObject signableSAMLObject) {
        Signature signature = buildSignature();
        signableSAMLObject.setSignature(signature);
        try {
            marshaller.marshallToElement(signableSAMLObject);
            Signer.signObject(signature);
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    private Signature buildSignature() {

        Signature signature = build(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setSigningCredential(new BasicCredential(publicKey, privateKey));
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setKeyInfo(buildKeyInfo());
        return signature;
    }

    private KeyInfo buildKeyInfo() {
        KeyInfo keyInfo = build(KeyInfo.DEFAULT_ELEMENT_NAME);
        X509Data x509Data = build(X509Data.DEFAULT_ELEMENT_NAME);
        X509Certificate x509Certificate = build(X509Certificate.DEFAULT_ELEMENT_NAME);

        x509Certificate.setValue(certificate.replace(BEGIN_CERT, "").replace(END_CERT, ""));
        x509Data.getX509Certificates().add(x509Certificate);
        keyInfo.getX509Datas().add(x509Data);
        return keyInfo;
    }

    public static <T extends XMLObject> T build(QName elementName) {
        return (T) XMLObjectSupport.buildXMLObject(elementName);
    }
}
