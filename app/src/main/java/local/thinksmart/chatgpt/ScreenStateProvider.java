package local.thinksmart.chatgpt;
import android.content.*;import android.database.Cursor;import android.net.Uri;import android.os.Bundle;
/** Only same-signature dashboard components can read this one boolean. */
public final class ScreenStateProvider extends ContentProvider {
 public boolean onCreate(){return true;}
 public Bundle call(String method,String arg,Bundle extras){getContext().enforceCallingPermission("local.thinksmart.chatgpt.permission.SCREEN_TOGGLE","Trusted dashboard required");if(!"status".equals(method)||arg!=null)throw new IllegalArgumentException();Bundle b=new Bundle();b.putBoolean("keep_screen_on",new MonitorSettings(getContext()).prefs.getBoolean("keep_screen_on",false));return b;}
 public Cursor query(Uri u,String[]p,String s,String[]a,String o){throw new UnsupportedOperationException();}public String getType(Uri u){return null;}public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[]a){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[]a){throw new UnsupportedOperationException();}
}
