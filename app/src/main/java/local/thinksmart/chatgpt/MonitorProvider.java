package local.thinksmart.chatgpt;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;

/** DUMP-protected installer diagnostics and one-time parent provisioning. */
public final class MonitorProvider extends ContentProvider {
    public static final String AUTHORITY = "local.thinksmart.chatgpt.monitor";
    @Override public boolean onCreate() { return true; }
    private void enforceAccess() {
        // ContentProvider.call does not automatically enforce the provider read permission.
        if (Binder.getCallingUid() != Process.myUid())
            getContext().enforceCallingPermission("android.permission.DUMP", "ADB monitor permission required");
    }
    private void checkUri(Uri uri) {
        if (!"content".equals(uri.getScheme()) || !AUTHORITY.equals(uri.getAuthority()) || !"/events".equals(uri.getPath())
                || uri.getFragment() != null || !Arrays.asList("after", "limit").containsAll(uri.getQueryParameterNames()))
            throw new IllegalArgumentException("Unsupported monitor URI");
        for (String key : uri.getQueryParameterNames()) if (uri.getQueryParameters(key).size() != 1)
            throw new IllegalArgumentException("Duplicate monitor parameter");
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        enforceAccess(); checkUri(uri);
        if ((projection != null && !Arrays.equals(projection, BlockedEventStore.COLUMNS)) || selection != null
                || selectionArgs != null || sortOrder != null) throw new IllegalArgumentException("Fixed monitor query only");
        return BlockedEventStore.get(getContext()).events(MonitorPolicy.parseAfter(uri.getQueryParameter("after")),
                MonitorPolicy.batchLimit(uri.getQueryParameter("limit")));
    }
    @Override public String getType(Uri uri) {
        enforceAccess(); checkUri(uri); return "vnd.android.cursor.dir/vnd.local.thinksmart.chatgpt.blocked-event";
    }
    @Override public Bundle call(String method, String arg, Bundle extras) {
        enforceAccess();
        if ("browser_bridge_public_key".equals(method)) {
            if(arg!=null || (extras!=null && !extras.isEmpty()))throw new IllegalArgumentException("No arguments accepted");
            try{Bundle result=new Bundle();result.putString("public_key",BrowserBridgeIdentity.publicKey());return result;}catch(Exception e){throw new IllegalStateException("Bridge identity unavailable");}
        }
        if ("close_browser_setup".equals(method)) {
            if(arg!=null || (extras!=null && !extras.isEmpty())) throw new IllegalArgumentException("No arguments accepted");
            ParentMonitorService.clearBrowserSetup(getContext());
            Bundle result=new Bundle();result.putBoolean("closed",true);return result;
        }
        if ("set_monitor_paused".equals(method)) {
            if(!"true".equals(arg) && !"false".equals(arg))throw new IllegalArgumentException("Boolean required");
            boolean paused=Boolean.parseBoolean(arg);
            if(!new MonitorSettings(getContext()).prefs.edit().putBoolean("diagnostic_paused",paused).commit())throw new IllegalStateException("Pause not saved");
            if(paused)getContext().stopService(new android.content.Intent(getContext(),ParentMonitorService.class));
            else ParentMonitorService.ensureStarted(getContext());
            Bundle result=new Bundle();result.putBoolean("monitor_paused",paused);return result;
        }
        if ("configure_learning".equals(method)) {
            LearningSettings.configure(getContext(),arg);
            Bundle result=new Bundle();result.putBoolean("configured",true);return result;
        }
        if ("provision_parent".equals(method)) {
            new MonitorSettings(getContext()).provision(arg);
            ParentMonitorService.ensureStarted(getContext());
            Bundle result = new Bundle(); result.putBoolean("configured",true); return result;
        }
        if ("request_screen_permission".equals(method)) {
            getContext().startActivity(new android.content.Intent(getContext(),ProjectionRequestActivity.class).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
            return new Bundle();
        }
        if ("events_json".equals(method)) {
            long after = MonitorPolicy.parseAfter(arg);
            int limit = MonitorPolicy.batchLimit(extras == null || !extras.containsKey("limit") ? null
                    : Integer.toString(extras.getInt("limit")));
            try (Cursor cursor = BlockedEventStore.get(getContext()).events(after, limit)) {
                JSONArray events = new JSONArray();
                long nextAfter = after;
                while (cursor.moveToNext()) {
                    JSONObject event = new JSONObject();
                    for (int i = 0; i < BlockedEventStore.COLUMNS.length; i++) {
                        Object value = cursor.isNull(i) ? JSONObject.NULL
                                : (i == 0 || i == 1 || i == 5 ? cursor.getLong(i) : cursor.getString(i));
                        event.put(BlockedEventStore.COLUMNS[i], value);
                    }
                    events.put(event); nextAfter = cursor.getLong(0);
                }
                Bundle result = new Bundle();
                result.putString("json", new JSONObject().put("events", events).put("next_after", nextAfter).toString());
                return result;
            } catch (JSONException error) { throw new IllegalStateException("Cannot serialize monitor events"); }
        }
        throw new IllegalArgumentException("Unknown monitor call");
    }
    @Override public Uri insert(Uri uri, ContentValues values) { enforceAccess(); throw new UnsupportedOperationException("Read only"); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) { enforceAccess(); throw new UnsupportedOperationException("Read only"); }
    @Override public int delete(Uri uri, String selection, String[] args) { enforceAccess(); throw new UnsupportedOperationException("Read only"); }
}
