package uk.gov.ida.trustanchor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyType;

public class CountryTrustAnchor {
  public static JWK parse(String json) throws ParseException {
    JWK key = JWK.parse(json);

    Collection<String> errors = findErrors(key);
    if (!errors.isEmpty()) {
      throw new RuntimeException(String.format("JWK was not a valid trust anchor: %s", String.join(", ", errors)));
    }

    return key;
  }

  public static Collection<String> findErrors(JWK anchor) {
    Collection<String> errors = new HashSet<String>();
    if (!anchor.getKeyType().equals(KeyType.RSA))
      errors.add(String.format("Expecting key type to be %s, was %s", KeyType.RSA, anchor.getKeyType()));
    if (!anchor.getAlgorithm().equals(JWSAlgorithm.RS256))
      errors.add(String.format("Expecting algorithm to be %s, was %s", JWSAlgorithm.RS256, anchor.getAlgorithm()));
    if (anchor.getKeyOperations().size() != 1 || !anchor.getKeyOperations().contains(KeyOperation.VERIFY))
      errors.add(String.format("Expecting key operations to only contain %s", KeyOperation.VERIFY));
    if (anchor.getX509CertChain().size() != 1)
      errors.add(String.format("Expecting exactly one X.509 certificate"));

    InputStream certStream = new ByteArrayInputStream(anchor.getX509CertChain().get(0).decode());
    try {
      CertificateFactory.getInstance("X.509").generateCertificate(certStream);
    } catch (CertificateException e) {
      errors.add(String.format("Expecting a valid X.509 certificate: %s", e.getMessage()));
    }

    return errors;
  }
}