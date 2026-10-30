package local.thinksmart.chatgpt;
import org.junit.Test;
import static org.junit.Assert.*;
public class BrowserSetupGateTest {
 @Test public void staleStartCannotUndoClose(){BrowserSetupGate gate=new BrowserSetupGate();long initial=gate.epoch();int[] state={0};gate.close(()->state[0]=0);try{gate.start(initial,()->{state[0]=1;return 1L;});fail();}catch(IllegalStateException expected){}assertEquals(0,state[0]);assertEquals(1,gate.start(gate.epoch(),()->{state[0]=1;return 1L;}));}
 @Test public void delayedConcurrentStartCannotUndoClose()throws Exception{
  BrowserSetupGate gate=new BrowserSetupGate();java.util.concurrent.CountDownLatch captured=new java.util.concurrent.CountDownLatch(1),resume=new java.util.concurrent.CountDownLatch(1);int[] state={0};boolean[] rejected={false};
  Thread start=new Thread(()->{long epoch=gate.epoch();captured.countDown();try{resume.await();gate.start(epoch,()->{state[0]=1;return 1;});}catch(IllegalStateException expected){rejected[0]=true;}catch(InterruptedException e){Thread.currentThread().interrupt();}});
  start.start();assertTrue(captured.await(2,java.util.concurrent.TimeUnit.SECONDS));gate.close(()->state[0]=0);resume.countDown();start.join(2000);assertFalse(start.isAlive());assertTrue(rejected[0]);assertEquals(0,state[0]);
 }
}
