package local.thinksmart.manualkeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputMethodManager;

/** Empty IME: never obtains InputConnection, reads text or emits input. */
public final class CollapsedIme extends InputMethodService {
 private static CollapsedIme instance;
 @Override public void onCreate(){super.onCreate();instance=this;}
 @Override public void onDestroy(){instance=null;super.onDestroy();}
 /** Arm the system's show request using this IME's own token; never touch editor contents. */
 static boolean prepareForSwitch(){
  CollapsedIme ime=instance;
  if(ime==null||!ime.getCurrentInputStarted()||ime.getWindow().getWindow()==null)return false;
  InputMethodManager manager=(InputMethodManager)ime.getSystemService(INPUT_METHOD_SERVICE);
  manager.showSoftInputFromInputMethod(ime.getWindow().getWindow().getAttributes().token,0);
  return true;
 }
 @Override public boolean onShowInputRequested(int flags,boolean configChange){return false;}
 @Override public boolean onEvaluateInputViewShown(){return false;}
 @Override public boolean onEvaluateFullscreenMode(){return false;}
}
