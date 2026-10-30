package local.thinksmart.manualkeyboard;

/** State only: never accepts or stores text, keycodes or view contents. */
final class KeyboardPolicy {
 private boolean active,expanded;
 private long openedAt;
 private long editor;
 void enter(){if(!active){active=true;expanded=false;}}
 void leave(){active=false;expanded=false;}
 void screenOff(){expanded=false;}
 void toggle(long now){if(!active)return;expanded=!expanded;if(expanded)openedAt=now;}
 void editorFocused(long now){if(expanded&&now-openedAt>=1000)expanded=false;}
 void trackEditor(long identity){editor=identity;}
 void observeEditor(long identity,long now){if(expanded&&now-openedAt>=1000&&identity!=editor)expanded=false;}
 boolean active(){return active;}
 boolean expanded(){return expanded;}
}
