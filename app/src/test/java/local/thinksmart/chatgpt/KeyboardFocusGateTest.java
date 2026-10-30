package local.thinksmart.chatgpt;
import org.junit.Test;
import static org.junit.Assert.*;
public class KeyboardFocusGateTest {
    @Test public void initialPageAutofocusCannotOpenKeyboard(){
        KeyboardFocusGate g=new KeyboardFocusGate();
        assertFalse(g.mayShow(5000,false));assertFalse(g.mayShow(5000,true));
    }
    @Test public void deliberateTapCanOpenKeyboardAndKeepEditing(){
        KeyboardFocusGate g=new KeyboardFocusGate();g.userTap(5000);
        assertTrue(g.mayShow(5050,false));assertTrue(g.mayShow(20000,true));
        assertFalse(g.mayShow(20000,false));
    }
    @Test public void newPageAndReentryRevokeOldTapPermission(){
        KeyboardFocusGate g=new KeyboardFocusGate();g.userTap(5000);g.reset();
        assertFalse(g.mayShow(5050,false));assertFalse(g.mayShow(5050,true));
        g.userTap(7000);assertTrue(g.mayShow(7050,false));
    }
    @Test public void staleOrFutureTouchCannotAuthorizeAutofocus(){
        KeyboardFocusGate g=new KeyboardFocusGate();g.userTap(5000);
        assertFalse(g.mayShow(4999,false));assertFalse(g.mayShow(8000,false));
    }
}
