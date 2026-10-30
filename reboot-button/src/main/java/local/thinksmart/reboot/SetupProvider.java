package local.thinksmart.reboot;
import android.content.*;import android.database.Cursor;import android.net.Uri;import android.os.*;
public final class SetupProvider extends ContentProvider {
 public boolean onCreate(){return true;}
 public Bundle call(String method,String arg,Bundle extras){getContext().enforceCallingPermission("android.permission.DUMP","Installer access required");if(!"configure".equals(method)||arg==null||!arg.matches("[a-f0-9]{64}"))throw new IllegalArgumentException("Invalid setup");getContext().getSharedPreferences("reboot",0).edit().putString("api_key",arg).commit();Bundle b=new Bundle();b.putBoolean("configured",true);return b;}
 public Cursor query(Uri u,String[]p,String s,String[]a,String o){throw new UnsupportedOperationException();}public String getType(Uri u){return null;}public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[]a){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[]a){throw new UnsupportedOperationException();}
}