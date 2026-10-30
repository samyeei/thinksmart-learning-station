package local.thinksmart.chatgpt;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Base64;
import java.security.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
final class MonitorSettings {
 final SharedPreferences prefs;
 MonitorSettings(Context c){this(c.getSharedPreferences("parent-monitor",0));}
 MonitorSettings(SharedPreferences prefs){this.prefs=prefs;}
 boolean paused(){return prefs.getBoolean("diagnostic_paused",false);}
 boolean enabled(){return prefs.contains("hash") && !paused();}
 void provision(String password) {
  synchronized(prefs){if(prefs.contains("hash"))throw new IllegalStateException("Already enrolled; change password from authenticated parent page");PasswordChangeForm.validateNew(password);savePassword(password,true);}
 }
 boolean changePassword(String current,String next,Runnable beforeCommit){
  synchronized(prefs){
   PasswordChangeForm.validateNew(next);
   if(!verify(current))return false;
   if(next.equals(current))throw new IllegalArgumentException("新密码不能与当前密码相同");
   beforeCommit.run();savePassword(next,false);return true;
  }
 }
 private void savePassword(String password,boolean resetSince){
  String oldSalt=prefs.getString("salt",null),oldHash=prefs.getString("hash",null);
  boolean hadSince=prefs.contains("since");long oldSince=prefs.getLong("since",0);
  byte[] salt=new byte[24];new SecureRandom().nextBytes(salt);
  SharedPreferences.Editor edit=prefs.edit().putString("salt",Base64.getEncoder().encodeToString(salt)).putString("hash",Base64.getEncoder().encodeToString(derive(password,salt)));
  if(resetSince)edit.putLong("since",System.currentTimeMillis());
  if(!edit.commit()){
   SharedPreferences.Editor restore=prefs.edit();
   if(oldSalt==null)restore.remove("salt");else restore.putString("salt",oldSalt);
   if(oldHash==null)restore.remove("hash");else restore.putString("hash",oldHash);
   if(hadSince)restore.putLong("since",oldSince);else restore.remove("since");
   restore.commit();throw new IllegalStateException("Credential persistence not confirmed");
  }
 }
 boolean verify(String value){synchronized(prefs){try{return value!=null && value.length()<=128 && MessageDigest.isEqual(Base64.getDecoder().decode(prefs.getString("hash","")),derive(value,Base64.getDecoder().decode(prefs.getString("salt",""))));}catch(RuntimeException e){return false;}}}
 private byte[] derive(String v,byte[] salt){PBEKeySpec spec=new PBEKeySpec(v.toCharArray(),salt,120000,256);try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}catch(Exception e){throw new IllegalStateException("Password hashing unavailable");}finally{spec.clearPassword();}}
}
