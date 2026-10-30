package local.thinksmart.chatgpt;
import java.nio.charset.StandardCharsets;
final class PolicySignature {
 static byte[] canonical(String nonce,boolean locked,long until){return (nonce+"\n"+Boolean.toString(locked)+"\n"+Long.toString(until)).getBytes(StandardCharsets.US_ASCII);}
 private PolicySignature(){}
}
