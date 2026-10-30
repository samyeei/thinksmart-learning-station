package local.thinksmart.chatgpt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebResponse;

public class MainActivity extends Activity {
    private String homeUrl;
    private GeckoView browserView;
    private final KeyboardFocusGate keyboardFocus=new KeyboardFocusGate();
    private boolean learningUiReady;
    private String currentPage;
    private GeckoSession.PromptDelegate.FilePrompt pendingPhotoPrompt;
    private GeckoResult<GeckoSession.PromptDelegate.PromptResponse> pendingPhotoResult;
    private android.net.Uri cameraOutput;
    private static final int QUESTION_CAMERA = 61;
    private static GeckoRuntime runtime;
    private static final RecoveryPolicy RECOVERY=new RecoveryPolicy();
    private boolean recoveryScheduled;
    private boolean foreground;
    private final java.util.Set<String> frameAudit=new java.util.HashSet<>();
    private GeckoSession session;
    private TextView notice;
    private boolean canGoBack;
    private GeckoSession.PermissionDelegate.Callback pendingPermission;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        ParentMonitorService.ensureStarted(this);
        homeUrl = LearningSettings.home(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setPadding(12, 2, 12, 2);
        toolbar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText(LearningSettings.enabled(this) ? "学习辅导" : "ChatGPT"); title.setTextSize(19);
        toolbar.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button home = new Button(this); home.setText(LearningSettings.enabled(this) ? "新问题" : "回到首页");
        home.setOnClickListener(v -> {
            hideKeyboardUntilTap();
            notice.setVisibility(android.view.View.GONE);
            if (session==null || !session.isOpen()) recoverPage(false,"manual"); else session.loadUri(homeUrl);
        }); toolbar.addView(home);
        Button desktop = new Button(this); desktop.setText("返回桌面");
        desktop.setOnClickListener(v -> returnToDesktop()); toolbar.addView(desktop);
        root.addView(toolbar, new LinearLayout.LayoutParams(-1, -2));
        notice = new TextView(this); notice.setTextSize(14); notice.setPadding(16, 0, 16, 0);
        notice.setVisibility(android.view.View.GONE); root.addView(notice);
        try { BlockedEventStore.get(this).expire(); } catch (android.database.sqlite.SQLiteException ignored) {}
        GeckoView view = new GeckoView(this);browserView=view;
        view.setOnTouchListener((v,event)->{if(event.getActionMasked()==android.view.MotionEvent.ACTION_DOWN)keyboardFocus.userTap(event.getEventTime());return false;});
        root.addView(view, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        if (runtime == null) {
            GeckoRuntimeSettings settings = new GeckoRuntimeSettings.Builder()
                    .fissionEnabled(true).configFilePath(GeckoCompatibilityDefaults.restore(getApplicationContext())).remoteDebuggingEnabled(false).consoleOutput(false).build();
            runtime = GeckoRuntime.create(getApplicationContext(), settings);
        }
        session = new GeckoSession();
        session.getTextInput().setDelegate(new ManualKeyboardDelegate(session.getTextInput().getDelegate(),view,keyboardFocus));
        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override public GeckoResult<AllowOrDeny> onLoadRequest(GeckoSession s, LoadRequest request) {
                if (LearningPolicy.redirects(request.uri, homeUrl)) {
                    getWindow().getDecorView().post(() -> s.loadUri(homeUrl));
                    return GeckoResult.fromValue(AllowOrDeny.DENY);
                }
                if (!NavigationPolicy.allows(request.uri)) {
                    recordBlocked(request.target == TARGET_WINDOW_NEW ? "new_window" : "navigation", request.uri, request.hasUserGesture);
                    blocked(); return GeckoResult.fromValue(AllowOrDeny.DENY);
                }
                if (request.target == TARGET_WINDOW_NEW) {
                    getWindow().getDecorView().post(() -> s.loadUri(request.uri));
                    return GeckoResult.fromValue(AllowOrDeny.DENY);
                }
                return GeckoResult.fromValue(AllowOrDeny.ALLOW);
            }
            @Override public GeckoResult<AllowOrDeny> onSubframeLoadRequest(GeckoSession s, LoadRequest request) {
                boolean allowed=NavigationPolicy.allowsSubframe(request.uri);
                android.net.Uri frame=android.net.Uri.parse(request.uri==null?"":request.uri);
                String key=(allowed?"allowed:":"denied:")+frame.getScheme()+":"+frame.getHost();
                if(frameAudit.size()<16 && (!allowed || "cdn.platform.openai.com".equals(frame.getHost())) && frameAudit.add(key))android.util.Log.w("ThinkSmartFrame",key);
                return GeckoResult.fromValue(allowed ? AllowOrDeny.ALLOW : AllowOrDeny.DENY);
            }
            @Override public GeckoResult<GeckoSession> onNewSession(GeckoSession s, String uri) {
                if (!NavigationPolicy.allows(uri)) { recordBlocked("new_window", uri, null); blocked(); }
                return null;
            }
            @Override public void onCanGoBack(GeckoSession s, boolean back) { canGoBack = back; }
            @Override public void onLocationChange(GeckoSession s, String uri,
                    java.util.List<GeckoSession.PermissionDelegate.ContentPermission> permissions, Boolean gesture) {
                currentPage = uri;
                if (LearningPolicy.redirects(uri, homeUrl)) { s.stop(); s.loadUri(homeUrl); return; }
                // A second guard covers history changes and engine-internal redirects.
                if (uri != null && !NavigationPolicy.allows(uri)) {
                    recordBlocked("location_guard", uri, gesture);
                    s.stop(); blocked(); s.loadUri(homeUrl);
                }
            }
        });
        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override public void onCrash(GeckoSession s) { recoverPage(true,"content_crash"); }
            @Override public void onKill(GeckoSession s) { recoverPage(true,"content_killed"); }
            @Override public void onExternalResponse(GeckoSession s, WebResponse response) {
                recordBlocked("download", response.uri, null);
                if (response.body != null) try { response.body.close(); } catch (Exception ignored) {}
                notice.setVisibility(android.view.View.VISIBLE);
                notice.setText("此设备不允许下载文件或安装应用");
            }
            @Override public void onContextMenu(GeckoSession s, int x, int y, ContextElement element) {
                // No menu for opening a link in another app, browser or tab.
            }
        });
        session.setPromptDelegate(new GeckoSession.PromptDelegate() {
            @Override public GeckoResult<PromptResponse> onFilePrompt(GeckoSession s, FilePrompt prompt) {
                if (pendingPhotoPrompt != null || prompt.type == FilePrompt.Type.FOLDER || !PhotoPolicy.allowsImages(prompt.mimeTypes)
                        || !NavigationPolicy.allowsMicrophone(currentPage) || LearningPolicy.redirects(currentPage,homeUrl))
                    return GeckoResult.fromValue(prompt.dismiss());
                pendingPhotoPrompt=prompt;pendingPhotoResult=new GeckoResult<>();
                GeckoResult<PromptResponse> result=pendingPhotoResult;
                try {
                    cameraOutput=QuestionPhotoProvider.create(MainActivity.this);
                    Intent camera=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    camera.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,cameraOutput);
                    camera.setClipData(android.content.ClipData.newRawUri("Question photo",cameraOutput));
                    camera.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(camera,QUESTION_CAMERA);
                } catch(Exception error) { cancelPhoto(); Toast.makeText(MainActivity.this,"无法打开相机，请稍后重试",Toast.LENGTH_SHORT).show(); }
                return result;
            }
        });
        session.setPermissionDelegate(new GeckoSession.PermissionDelegate() {
            @Override public void onAndroidPermissionsRequest(GeckoSession s, String[] permissions, Callback callback) {
                if (permissions == null || permissions.length != 1 || !Manifest.permission.RECORD_AUDIO.equals(permissions[0])) {
                    callback.reject(); return;
                }
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) callback.grant();
                else if (pendingPermission != null) callback.reject();
                else { pendingPermission = callback; requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 41); }
            }
            @Override public GeckoResult<Integer> onContentPermissionRequest(GeckoSession s, ContentPermission permission) {
                boolean allowed = NavigationPolicy.allowsMicrophone(permission.uri) &&
                        (permission.permission == PERMISSION_AUTOPLAY_AUDIBLE || permission.permission == PERMISSION_AUTOPLAY_INAUDIBLE
                                || permission.permission == PERMISSION_PERSISTENT_STORAGE);
                return GeckoResult.fromValue(allowed ? ContentPermission.VALUE_ALLOW : ContentPermission.VALUE_DENY);
            }
            @Override public void onMediaPermissionRequest(GeckoSession s, String uri, MediaSource[] video,
                    MediaSource[] audio, MediaCallback callback) {
                if (!NavigationPolicy.allowsMicrophone(uri) || (video != null && video.length > 0)
                        || audio == null || checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    callback.reject(); return;
                }
                for (MediaSource source : audio) if (source.source == MediaSource.SOURCE_MICROPHONE) {
                    callback.grant((MediaSource) null, source); return;
                }
                callback.reject();
            }
        });
        session.open(runtime); view.setSession(session);
        if (LearningSettings.enabled(this)) {
            runtime.getWebExtensionController().ensureBuiltIn("resource://android/assets/learning/", "learning@thinksmart.local").accept(extension -> {
                learningUiReady = true; navigateIntent(getIntent());
            }, error -> {
                notice.setVisibility(android.view.View.VISIBLE);
                notice.setText("学习界面加载失败，请返回桌面后重试");
            });
        } else { learningUiReady = true; navigateIntent(getIntent()); }
    }
    private void navigateIntent(Intent intent) {
        String requested = intent.getDataString();
        if (requested == null || LearningPolicy.redirects(requested, homeUrl)) session.loadUri(homeUrl);
        else if (NavigationPolicy.allows(requested)) session.loadUri(requested);
        else { recordBlocked("inbound_intent", requested, null); blocked(); session.loadUri(homeUrl); }
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); hideKeyboardUntilTap(); setIntent(intent); refreshDiagnosticScreenHold(); if (!LearningSettings.home(this).equals(homeUrl)) { recreate(); return; } if (session==null || !session.isOpen()) { recoverPage(false,"reentry"); return; } if (learningUiReady && intent.getDataString() != null) navigateIntent(intent); }
    @Override protected void onResume() { super.onResume(); hideKeyboardUntilTap(); foreground=true; refreshDiagnosticScreenHold(); if(homeUrl!=null && !LearningSettings.home(this).equals(homeUrl))recreate(); else if(learningUiReady && session!=null && !session.isOpen())recoverPage(true,"resume"); }
    @Override protected void onPause(){foreground=false;super.onPause();}
    private void hideKeyboardUntilTap(){
        keyboardFocus.reset();
        if(browserView==null)return;
        android.view.inputmethod.InputMethodManager input=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if(input!=null)input.hideSoftInputFromWindow(browserView.getWindowToken(),0);
        browserView.post(()->{if(!keyboardFocus.hasUserTap()&&input!=null)input.hideSoftInputFromWindow(browserView.getWindowToken(),0);});
    }
    private void refreshDiagnosticScreenHold(){MonitorSettings m=new MonitorSettings(this);getWindow().getDecorView().setKeepScreenOn(m.paused() && m.prefs.getBoolean("keep_screen_on",false));}
    private void recoverPage(boolean automatic,String reason) {
        if(recoveryScheduled || isFinishing() || isDestroyed())return;
        if(automatic && !foreground){android.util.Log.w("ThinkSmartRecovery",reason+" deferred_in_background");return;}
        boolean retry=!automatic || RECOVERY.allowAutomatic(android.os.SystemClock.elapsedRealtime());
        android.util.Log.w("ThinkSmartRecovery",reason+" automatic="+automatic+" retry="+retry);
        notice.setVisibility(android.view.View.VISIBLE);
        notice.setText(retry?"页面异常，正在恢复；账号无需重新登录…":"语音页面连续异常，请点“新问题”恢复");
        if(!retry)return;
        recoveryScheduled=true;
        getWindow().getDecorView().postDelayed(() -> {if(isFinishing() || isDestroyed())return; if(!foreground){recoveryScheduled=false;android.util.Log.w("ThinkSmartRecovery","deferred_before_rebuild");return;}recreate();},250);
    }
    private void cancelPhoto() {
        if(cameraOutput!=null){revokeUriPermission(cameraOutput,Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);try{QuestionPhotoProvider.release(this,cameraOutput,true);}catch(RuntimeException ignored){}cameraOutput=null;}
        if(pendingPhotoPrompt!=null && pendingPhotoResult!=null && !pendingPhotoPrompt.isComplete())pendingPhotoResult.complete(pendingPhotoPrompt.dismiss());
        pendingPhotoPrompt=null;pendingPhotoResult=null;
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);if(request!=QUESTION_CAMERA)return;
        if(result!=RESULT_OK || cameraOutput==null || pendingPhotoPrompt==null){cancelPhoto();return;}
        final android.net.Uri captured=cameraOutput;revokeUriPermission(captured,Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        final GeckoSession.PromptDelegate.FilePrompt prompt=pendingPhotoPrompt;
        new Thread(() -> {
            android.net.Uri upload=null;try{upload=QuestionPhotoProvider.prepareUpload(getApplicationContext(),captured);}catch(Exception|OutOfMemoryError ignored){}
            final android.net.Uri ready=upload;runOnUiThread(() -> {
                if(pendingPhotoPrompt!=prompt || pendingPhotoResult==null || prompt.isComplete()){if(ready!=null)QuestionPhotoProvider.release(this,ready,true);return;}
                if(ready==null){cancelPhoto();Toast.makeText(this,"照片处理失败，请重拍",Toast.LENGTH_SHORT).show();return;}
                android.net.Uri internalFile=android.net.Uri.fromFile(QuestionPhotoProvider.file(this,ready));
                try { pendingPhotoResult.complete(prompt.type==GeckoSession.PromptDelegate.FilePrompt.Type.MULTIPLE ? prompt.confirm(this,new android.net.Uri[]{internalFile}) : prompt.confirm(this,internalFile)); }

                catch(RuntimeException error){if(!prompt.isComplete())pendingPhotoResult.complete(prompt.dismiss());}
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> QuestionPhotoProvider.release(getApplicationContext(),ready,false),600000);
                pendingPhotoPrompt=null;pendingPhotoResult=null;cameraOutput=null;
            });
        },"question-photo").start();
    }
    @Override public void onRequestPermissionsResult(int code, String[] names, int[] results) {
        super.onRequestPermissionsResult(code, names, results);
        if (code == 41 && pendingPermission != null) {
            if (results.length == 1 && results[0] == PackageManager.PERMISSION_GRANTED) pendingPermission.grant();
            else pendingPermission.reject();
            pendingPermission = null;
        }
    }
    private void recordBlocked(String kind, String uri, Boolean gesture) {
        if(!new MonitorSettings(this).paused())BlockedEventStore.get(this).record(kind, uri, gesture);
    }
    private void blocked() { notice.setVisibility(android.view.View.VISIBLE); notice.setText("已拦截：这个入口只允许访问 ChatGPT 和必要的登录页面"); }
    private void returnToDesktop() {
        // Android 8 lock task blocks the generic HOME action, including its own
        // allowlisted home. Return directly to the trusted kiosk controller.
        Intent home = new Intent().setClassName("local.thinksmart.dashboard", "local.thinksmart.dashboard.MainActivity");
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.putExtra("voluntaryReturn", true);
        try { startActivity(home); }
        catch (android.content.ActivityNotFoundException error) {
            try { startActivity(new Intent().setClassName("com.freekiosk","com.freekiosk.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch(android.content.ActivityNotFoundException missing){ notice.setVisibility(android.view.View.VISIBLE); notice.setText("桌面暂时不可用，请联系家长"); }
        }
    }
    @Override public void onBackPressed() { hideKeyboardUntilTap(); if (canGoBack && session!=null && session.isOpen()) session.goBack(); else returnToDesktop(); }
    @Override protected void onDestroy() {
        if (pendingPermission != null) { pendingPermission.reject(); pendingPermission = null; }
        if (session != null && session.isOpen()) session.close();
        cancelPhoto();
        super.onDestroy();
    }
}
