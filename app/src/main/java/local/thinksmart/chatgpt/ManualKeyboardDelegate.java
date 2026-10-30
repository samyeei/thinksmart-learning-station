package local.thinksmart.chatgpt;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import org.mozilla.geckoview.GeckoSession;

/** Preserve Gecko's complete IME behavior while suppressing page-load autofocus popups. */
final class ManualKeyboardDelegate implements GeckoSession.TextInputDelegate {
    private final GeckoSession.TextInputDelegate delegate;
    private final View view;
    private final KeyboardFocusGate gate;
    ManualKeyboardDelegate(GeckoSession.TextInputDelegate delegate,View view,KeyboardFocusGate gate){
        this.delegate=delegate;this.view=view;this.gate=gate;
    }
    @Override public void restartInput(GeckoSession s,int reason){delegate.restartInput(s,reason);}
    @Override public void showSoftInput(GeckoSession s){
        Rect visible=new Rect();view.getWindowVisibleDisplayFrame(visible);
        Point real=new Point();((WindowManager)view.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(real);
        boolean shown=view.getWindowToken()!=null&&real.y-visible.bottom>view.getResources().getDisplayMetrics().density*120;
        if(gate.mayShow(android.os.SystemClock.uptimeMillis(),shown))delegate.showSoftInput(s);
    }
    @Override public void hideSoftInput(GeckoSession s){delegate.hideSoftInput(s);}
    @Override public void updateSelection(GeckoSession s,int start,int end,int compositionStart,int compositionEnd){delegate.updateSelection(s,start,end,compositionStart,compositionEnd);}
    @Override public void updateExtractedText(GeckoSession s,ExtractedTextRequest request,ExtractedText text){delegate.updateExtractedText(s,request,text);}
    @Override public void updateCursorAnchorInfo(GeckoSession s,CursorAnchorInfo info){delegate.updateCursorAnchorInfo(s,info);}
}
