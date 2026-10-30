package local.thinksmart.chatgpt;
/** Serializes revocation with starts that captured a prior authority epoch. */
final class BrowserSetupGate {
 interface Start { long run(); }
 private long epoch;
 synchronized long epoch(){return epoch;}
 synchronized long start(long expected,Start operation){if(expected!=epoch)throw new IllegalStateException("Setup authorization expired");return operation.run();}
 synchronized void close(Runnable operation){epoch++;operation.run();}
}
