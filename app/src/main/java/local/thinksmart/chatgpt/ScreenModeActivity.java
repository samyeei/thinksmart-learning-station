package local.thinksmart.chatgpt;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
/** A signature-gated action invoked by the dedicated launcher button. */
public final class ScreenModeActivity extends Activity {
 @Override protected void onCreate(Bundle state){super.onCreate(state);MonitorSettings settings=new MonitorSettings(this);boolean next=!settings.prefs.getBoolean("keep_screen_on",false);settings.prefs.edit().putBoolean("keep_screen_on",next).commit();ParentMonitorService.ensureStarted(this);
  Toast.makeText(this,next?"不息屏已开启；再点开关恢复30秒息屏":"已恢复30秒无操作自动息屏",Toast.LENGTH_LONG).show();finish();
 }
}
