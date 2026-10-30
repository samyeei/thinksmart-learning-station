package local.thinksmart.chatgpt;
/** An entry starts in reading mode; only an actual touch may request a hidden keyboard. */
final class KeyboardFocusGate {
    private long lastTap=-1;
    void userTap(long now){lastTap=now;}
    void reset(){lastTap=-1;}
    boolean hasUserTap(){return lastTap>=0;}
    boolean mayShow(long now,boolean alreadyVisible){
        return lastTap>=0&&now>=lastTap&&(alreadyVisible||now-lastTap<=2500);
    }
}
