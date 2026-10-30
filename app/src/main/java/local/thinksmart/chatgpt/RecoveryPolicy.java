package local.thinksmart.chatgpt;
/** Bound automatic retries; user-initiated recovery is always available. */
final class RecoveryPolicy {
 private long window=-1;private int attempts;
 synchronized boolean allowAutomatic(long now){if(window<0||now<window||now-window>=60000){window=now;attempts=0;}return ++attempts<=2;}
}
