package local.thinksmart.chatgpt;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;

/** The private key is generated and retained inside this app's Android Keystore. */
final class BrowserBridgeIdentity {
 private static final String ALIAS="thinksmart-browser-bridge-v1";
 private final PrivateKey privateKey; private final String publicKey;
 private BrowserBridgeIdentity(KeyStore store)throws Exception{
  privateKey=(PrivateKey)store.getKey(ALIAS,null);java.security.cert.Certificate cert=store.getCertificate(ALIAS);
  if(privateKey==null || cert==null)throw new IllegalStateException("Bridge identity unavailable");
  publicKey=Base64.encodeToString(cert.getPublicKey().getEncoded(),Base64.NO_WRAP);
 }
 private static KeyStore store()throws Exception{KeyStore s=KeyStore.getInstance("AndroidKeyStore");s.load(null);return s;}
 static synchronized BrowserBridgeIdentity create()throws Exception{
  KeyStore s=store();if(!s.containsAlias(ALIAS)){KeyPairGenerator g=KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,"AndroidKeyStore");g.initialize(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_SIGN|KeyProperties.PURPOSE_VERIFY).setKeySize(2048).setDigests(KeyProperties.DIGEST_SHA256).setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1).build());g.generateKeyPair();s=store();}
  BrowserBridgeIdentity identity=new BrowserBridgeIdentity(s);identity.sign("00000000000000000000000000000000",true,0);return identity;
 }
 static synchronized String publicKey()throws Exception{KeyStore s=store();java.security.cert.Certificate cert=s.getCertificate(ALIAS);if(cert==null)throw new IllegalStateException("Bridge identity unavailable");return Base64.encodeToString(cert.getPublicKey().getEncoded(),Base64.NO_WRAP);}
 String sign(String nonce,boolean locked,long until)throws Exception{Signature s=Signature.getInstance("SHA256withRSA");s.initSign(privateKey);s.update(PolicySignature.canonical(nonce,locked,until));return Base64.encodeToString(s.sign(),Base64.NO_WRAP);}
}
