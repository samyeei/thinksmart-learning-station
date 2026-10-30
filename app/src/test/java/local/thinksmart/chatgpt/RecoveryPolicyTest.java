package local.thinksmart.chatgpt;
import org.junit.Test;import static org.junit.Assert.*;
public class RecoveryPolicyTest {
 @Test public void allowsRecoveryButStopsRapidCrashLoops(){RecoveryPolicy p=new RecoveryPolicy();assertTrue(p.allowAutomatic(1000));assertTrue(p.allowAutomatic(2000));assertFalse(p.allowAutomatic(3000));}
 @Test public void aLaterIndependentCrashCanRecover(){RecoveryPolicy p=new RecoveryPolicy();p.allowAutomatic(1000);p.allowAutomatic(2000);assertTrue(p.allowAutomatic(62000));}
}
