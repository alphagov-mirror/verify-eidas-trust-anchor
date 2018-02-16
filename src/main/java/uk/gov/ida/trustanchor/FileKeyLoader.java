package uk.gov.ida.trustanchor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class FileKeyLoader {

  private static final String ALGORITHM = "RSA";
  private static final String CERTIFICATE_TYPE = "X.509";

  public static RSAPrivateKey load(final File keyFile) throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {
    final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
    return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(keyFile.toPath())));
  }

  public static X509Certificate loadCert(final File certFile) throws CertificateException, FileNotFoundException {
    final CertificateFactory certificateFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE);
    return (X509Certificate) certificateFactory.generateCertificate(new FileInputStream(certFile));
  }
}
