package local.thinksmart.chatgpt;
import android.content.*;
public final class MonitorBootReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()))ParentMonitorService.ensureStarted(c);}
}
