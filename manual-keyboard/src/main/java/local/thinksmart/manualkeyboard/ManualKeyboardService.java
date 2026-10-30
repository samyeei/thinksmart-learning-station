package local.thinksmart.manualkeyboard;

import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.os.Handler;
import android.provider.Settings;
import android.view.*;
import android.view.accessibility.*;
import android.widget.TextView;

/** Focus and package events only. No event text, screenshots, keys, network or storage. */
public final class ManualKeyboardService extends AccessibilityService {
 static final String TARGET="com.openai.chatgpt";
 static final String CHINESE="org.fcitx.fcitx5.android/.input.FcitxInputMethodService";
 static final String COLLAPSED="local.thinksmart.manualkeyboard/.CollapsedIme";
 private final KeyboardPolicy policy=new KeyboardPolicy();
 private WindowManager windows;
 private TextView button;
 private boolean attached;
 private final Handler handler=new Handler();
 private final Runnable editorCheck=new Runnable(){public void run(){
  if(!policy.expanded())return;
  policy.observeEditor(editorIdentity(),SystemClock.uptimeMillis());apply();
 }};
 private final BroadcastReceiver screen=new BroadcastReceiver(){
  @Override public void onReceive(Context c,Intent i){policy.screenOff();apply();}
 };

 @Override public void onServiceConnected(){
  windows=(WindowManager)getSystemService(WINDOW_SERVICE);
  if(android.os.Build.VERSION.SDK_INT>=33)registerReceiver(screen,new IntentFilter(Intent.ACTION_SCREEN_OFF),Context.RECEIVER_NOT_EXPORTED);
  else registerReceiver(screen,new IntentFilter(Intent.ACTION_SCREEN_OFF));
  // Read only the foreground package to recover after a service restart.
  AccessibilityNodeInfo root=getRootInActiveWindow();
  if(root!=null){CharSequence pkg=root.getPackageName();if(TARGET.contentEquals(pkg==null?"":pkg))policy.enter();root.recycle();}
  apply();
 }
 @Override public void onAccessibilityEvent(AccessibilityEvent e){
  CharSequence pkg=e.getPackageName();if(pkg==null)return;
  String p=pkg.toString();
  if(p.equals(getPackageName())||p.equals("org.fcitx.fcitx5.android")||p.equals("com.android.systemui"))return;
  if(e.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
   if(TARGET.equals(p))policy.enter();else policy.leave();
   apply();
  } else if(TARGET.equals(p)&&e.getEventType()==AccessibilityEvent.TYPE_VIEW_FOCUSED){
   policy.enter();
   apply();
  }
 }
 private void apply(){
  try {
   String ime=policy.active()&&!policy.expanded()?COLLAPSED:CHINESE;
   if(!ime.equals(Settings.Secure.getString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD))){
    if(!Settings.Secure.putString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD,ime))throw new SecurityException("IME selection failed");
   }
   if(policy.active())showButton();else removeButton();
   if(button!=null){button.setText(policy.expanded()?"收起键盘":"键盘");button.setContentDescription(policy.expanded()?"收起中文键盘":"展开中文键盘");}
   handler.removeCallbacks(editorCheck);
   if(policy.expanded())handler.postDelayed(editorCheck,400);
  }catch(RuntimeException failure){
   policy.leave();removeButton();
   try{Settings.Secure.putString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD,CHINESE);}catch(RuntimeException ignored){}
   android.util.Log.e("ThinkSmartManualKeyboard","Keyboard control unavailable",failure);
  }
 }
 private long editorIdentity(){
  AccessibilityNodeInfo root=getRootInActiveWindow();
  if(root==null)return 0;
  try {
   if(!TARGET.contentEquals(root.getPackageName()==null?"":root.getPackageName()))return 0;
   AccessibilityNodeInfo focus=root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
   if(focus==null)return 0;
   try{return ((long)focus.getWindowId()<<32)^(focus.hashCode()&0xffffffffL);}
   finally{focus.recycle();}
  }catch(RuntimeException stale){return 0;}finally{root.recycle();}
 }
 private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
 private void showButton(){
  if(attached)return;
  button=new TextView(this);button.setGravity(Gravity.CENTER);button.setTextColor(Color.WHITE);button.setTextSize(14);
  button.setPadding(dp(12),0,dp(12),0);
  GradientDrawable bg=new GradientDrawable();bg.setColor(0xee173e66);bg.setCornerRadius(dp(22));button.setBackground(bg);
  button.setOnClickListener(v->{
   if(!policy.expanded()&&!CollapsedIme.prepareForSwitch()){button.setText("先点输入框");return;}
   if(!policy.expanded())policy.trackEditor(editorIdentity());
   policy.toggle(SystemClock.uptimeMillis());apply();
  });
  WindowManager.LayoutParams lp=new WindowManager.LayoutParams(dp(88),dp(44),WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
   WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);
  lp.gravity=Gravity.RIGHT|Gravity.TOP;lp.x=dp(12);lp.y=dp(100);lp.setTitle("ThinkSmart manual keyboard");
  windows.addView(button,lp);attached=true;
 }
 private void removeButton(){if(attached){windows.removeViewImmediate(button);attached=false;}button=null;}
 @Override public void onInterrupt(){policy.leave();apply();}
 @Override public void onDestroy(){
  handler.removeCallbacksAndMessages(null);
  try{unregisterReceiver(screen);}catch(IllegalArgumentException ignored){}
  policy.leave();apply();super.onDestroy();
 }
}
