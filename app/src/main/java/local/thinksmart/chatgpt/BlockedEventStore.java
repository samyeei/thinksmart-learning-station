package local.thinksmart.chatgpt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/** App-private journal: the original URI must never reach SQLite or a diagnostic log. */
final class BlockedEventStore extends SQLiteOpenHelper {
    static final String[] COLUMNS = {"_id", "timestamp_ms", "kind", "target_host", "target_scheme", "user_gesture"};
    private static BlockedEventStore instance;
    static synchronized BlockedEventStore get(Context context) {
        if (instance == null) instance = new BlockedEventStore(context.getApplicationContext());
        return instance;
    }
    private BlockedEventStore(Context context) { super(context, "blocked-events.db", null, 1); }
    @Override public void onConfigure(SQLiteDatabase db) { try(Cursor cursor=db.rawQuery("PRAGMA secure_delete=ON",null)){cursor.moveToFirst();} }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE events (_id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp_ms INTEGER NOT NULL, "
                + "kind TEXT NOT NULL, target_host TEXT NOT NULL, target_scheme TEXT NOT NULL, "
                + "user_gesture INTEGER CHECK(user_gesture IN (0,1) OR user_gesture IS NULL))");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Unsupported journal schema upgrade");
    }
    synchronized void record(String kind, String uri, Boolean gesture) {
        // The fixed event kinds cannot contain externally supplied text.
        if (!kind.equals("navigation") && !kind.equals("new_window") && !kind.equals("inbound_intent")
                && !kind.equals("location_guard") && !kind.equals("download") && !kind.equals("subframe"))
            throw new IllegalArgumentException("Unknown event kind");
        MonitorPolicy.Target target = MonitorPolicy.sanitize(uri);
        ContentValues values = new ContentValues();
        values.put("timestamp_ms", System.currentTimeMillis());
        values.put("kind", kind);
        values.put("target_host", target.host);
        values.put("target_scheme", target.scheme);
        if (gesture == null) values.putNull("user_gesture"); else values.put("user_gesture", gesture ? 1 : 0);
        // A full disk must never weaken the navigation guard or crash the child UI.
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            try {
                db.insertOrThrow("events", null, values);
                prune(db);
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        } catch (SQLiteException ignored) { /* No URI or exception text is logged. */ }
    }
    private void prune(SQLiteDatabase db) {
        db.delete("events", "timestamp_ms < ?", new String[]{Long.toString(System.currentTimeMillis() - MonitorPolicy.RETENTION_MS)});
        db.execSQL("DELETE FROM events WHERE _id IN (SELECT _id FROM events ORDER BY _id DESC LIMIT -1 OFFSET "
                + MonitorPolicy.MAX_EVENTS + ")");
    }
    synchronized Cursor events(long after, int limit) {
        SQLiteDatabase db = getWritableDatabase();
        prune(db);
        return db.query("events", COLUMNS, "_id > ?", new String[]{Long.toString(after)}, null, null, "_id ASC", Integer.toString(limit));
    }
    synchronized void expire() { prune(getWritableDatabase()); }
}
