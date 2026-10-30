package local.thinksmart.chatgpt;
import org.junit.Test;
import static org.junit.Assert.*;
public class BrowserSetupPolicyTest {
 @Test public void validatesBoundedLease(){assertEquals(1600,BrowserSetupPolicy.validUntil(1000,1600,1200));assertEquals(0,BrowserSetupPolicy.validUntil(1000,1600,1600));assertEquals(0,BrowserSetupPolicy.validUntil(1000,1600,999));assertEquals(0,BrowserSetupPolicy.validUntil(0,100,1));assertEquals(0,BrowserSetupPolicy.validUntil(1,600002,2));assertEquals(0,BrowserSetupPolicy.validUntil(100,99,100));}
}
