package local.thinksmart.chatgpt;
import android.content.SharedPreferences;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MonitorPasswordChangeTest {
 static final String OLD="storage-old-fixture",NEXT="storage-new-fixture";
 @Test public void rotatesSaltAndHashWithoutMovingHistoryStart(){
  Memory p=new Memory();MonitorSettings s=new MonitorSettings(p);s.provision(OLD);
  long since=p.getLong("since",0);String salt=p.getString("salt","");int[] close={0};
  assertTrue(s.changePassword(OLD,NEXT,()->close[0]++));
  assertEquals(1,close[0]);assertEquals(since,p.getLong("since",0));assertNotEquals(salt,p.getString("salt",""));
  assertFalse(s.verify(OLD));assertTrue(s.verify(NEXT));assertFalse(p.getAll().containsValue(NEXT));
 }
 @Test public void wrongCurrentDoesNotTouchMaintenanceOrCredentials(){
  Memory p=new Memory();MonitorSettings s=new MonitorSettings(p);s.provision(OLD);Map<String,?> before=p.getAll();
  assertFalse(s.changePassword("wrong",NEXT,()->fail("Must not close maintenance on failed verification")));
  assertEquals(before,p.getAll());assertTrue(s.verify(OLD));
 }
 @Test public void diskFailureRestoresOriginalCredentialsInMemoryAndOnDisk(){
  Memory p=new Memory();MonitorSettings s=new MonitorSettings(p);s.provision(OLD);Map<String,Object> disk=new HashMap<>(p.disk);p.failNext=true;
  try{s.changePassword(OLD,NEXT,()->{});fail("Persistence failure must not report success");}catch(IllegalStateException expected){}
  assertTrue(s.verify(OLD));assertFalse(s.verify(NEXT));assertEquals(disk,p.disk);
 }
 @Test public void maintenanceFailureStopsBeforeCredentialReplacement(){
  Memory p=new Memory();MonitorSettings s=new MonitorSettings(p);s.provision(OLD);Map<String,?> before=p.getAll();
  try{s.changePassword(OLD,NEXT,()->{throw new IllegalStateException("fixture failure");});fail();}catch(IllegalStateException expected){}
  assertEquals(before,p.getAll());assertTrue(s.verify(OLD));
 }
 @Test public void initialSetupCannotResetExistingParentPassword(){
  Memory p=new Memory();MonitorSettings s=new MonitorSettings(p);s.provision(OLD);Map<String,?> before=p.getAll();
  try{s.provision(NEXT);fail("Initial enrollment must not reset credentials");}catch(IllegalStateException expected){}
  assertEquals(before,p.getAll());assertTrue(s.verify(OLD));assertFalse(s.verify(NEXT));
 }
 static final class Memory implements SharedPreferences {
  final Map<String,Object> values=new HashMap<>(),disk=new HashMap<>();boolean failNext;
  public Map<String,?> getAll(){return new HashMap<>(values);}public boolean contains(String k){return values.containsKey(k);}
  public String getString(String k,String d){return (String)values.getOrDefault(k,d);}public int getInt(String k,int d){return (Integer)values.getOrDefault(k,d);}
  public long getLong(String k,long d){return (Long)values.getOrDefault(k,d);}public float getFloat(String k,float d){return (Float)values.getOrDefault(k,d);}
  public boolean getBoolean(String k,boolean d){return (Boolean)values.getOrDefault(k,d);}
  @SuppressWarnings("unchecked") public Set<String> getStringSet(String k,Set<String>d){return (Set<String>)values.getOrDefault(k,d);}
  public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l){}public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l){}
  public Editor edit(){return new Editor(){
   final Map<String,Object> pending=new HashMap<>();boolean clear;
   public Editor putString(String k,String v){pending.put(k,v);return this;}public Editor putStringSet(String k,Set<String>v){pending.put(k,v);return this;}
   public Editor putInt(String k,int v){pending.put(k,v);return this;}public Editor putLong(String k,long v){pending.put(k,v);return this;}
   public Editor putFloat(String k,float v){pending.put(k,v);return this;}public Editor putBoolean(String k,boolean v){pending.put(k,v);return this;}
   public Editor remove(String k){pending.put(k,null);return this;}public Editor clear(){clear=true;return this;}public void apply(){commit();}
   public boolean commit(){if(clear)values.clear();for(Map.Entry<String,Object> e:pending.entrySet()){if(e.getValue()==null)values.remove(e.getKey());else values.put(e.getKey(),e.getValue());}if(failNext){failNext=false;return false;}disk.clear();disk.putAll(values);return true;}
  };}
 }
}
