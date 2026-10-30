package local.thinksmart.chatgpt;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import org.json.*;
import java.time.*;
final class UsageJournal extends SQLiteOpenHelper {
 UsageJournal(Context c){super(c,"parent-usage.db",null,1);}
 public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE spans (id INTEGER PRIMARY KEY, pkg TEXT NOT NULL, start INTEGER NOT NULL, end INTEGER NOT NULL)");}
 public void onUpgrade(SQLiteDatabase db,int a,int b){throw new IllegalStateException();}
 synchronized void prune(){SQLiteDatabase db=getWritableDatabase();long cutoff=System.currentTimeMillis()-MonitorPolicy.RETENTION_MS;db.delete("spans","end < ?",new String[]{""+cutoff});db.execSQL("UPDATE spans SET start=? WHERE start<?",new Object[]{cutoff,cutoff});}
 synchronized void add(String pkg,long start,long end){if(end<=start)return;SQLiteDatabase db=getWritableDatabase();
  try(Cursor c=db.rawQuery("SELECT id,end FROM spans WHERE pkg=? ORDER BY id DESC LIMIT 1",new String[]{pkg})){
   if(c.moveToFirst() && c.getLong(1)==start){ContentValues v=new ContentValues();v.put("end",end);db.update("spans",v,"id=?",new String[]{c.getString(0)});return;}}
  ContentValues v=new ContentValues();v.put("pkg",pkg);v.put("start",start);v.put("end",end);db.insertOrThrow("spans",null,v);
 }
 static long[] day(String date){LocalDate d=LocalDate.parse(date);ZoneId z=ZoneId.of("Asia/Shanghai");return new long[]{d.atStartOfDay(z).toInstant().toEpochMilli(),d.plusDays(1).atStartOfDay(z).toInstant().toEpochMilli()};}
 synchronized JSONObject report(String date,long since,boolean access){try{
  long[] day=day(date);SQLiteDatabase db=getWritableDatabase();long cutoff=System.currentTimeMillis()-MonitorPolicy.RETENTION_MS;
  db.delete("spans","end < ?",new String[]{""+cutoff});
  JSONObject out=new JSONObject().put("date",date).put("timezone","Asia/Shanghai").put("since",Math.max(since,day[0]));JSONArray apps=new JSONArray();long observed=0;
  try(Cursor c=db.rawQuery("SELECT pkg,SUM(MIN(end,CAST(? AS INTEGER))-MAX(start,CAST(? AS INTEGER))) FROM spans WHERE end>? AND start<? GROUP BY pkg",new String[]{""+day[1],""+Math.max(day[0],cutoff),""+Math.max(day[0],cutoff),""+day[1]})){
   while(c.moveToNext()){String pkg=c.getString(0);long seconds=c.getLong(1)/1000;if(pkg.equals("__observed__"))observed=seconds;else apps.put(new JSONObject().put("package_name",pkg).put("label",MonitorPolicy.label(pkg)).put("seconds",seconds));}}
  return out.put("apps",apps).put("observed_seconds",access?observed:JSONObject.NULL);
 }catch(Exception e){throw new IllegalArgumentException("Usage unavailable");}}
}
