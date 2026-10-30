package local.thinksmart.chatgpt;
import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.database.Cursor;
import android.os.*;
import org.json.*;
import java.net.*;
import java.util.*;
public final class ParentMonitorService extends Service implements ParentHttpServer.Backend {
 private MonitorSettings settings;private UsageJournal journal;private ScreenCapture screen;private ParentHttpServer server;
 private LoopbackPolicyServer bridge; private volatile boolean stopping; private static final BrowserSetupGate setupGate=new BrowserSetupGate();
 private HandlerThread worker;private Handler handler;private PowerManager.WakeLock screenLock;
 private volatile long started,lastSample;private long lastElapsed,lastWall,eventCursor,lastPrune;private volatile String foreground="";private boolean previousInteractive;
 public static void ensureStarted(Context c){if(new MonitorSettings(c).enabled())c.startForegroundService(new Intent(c,ParentMonitorService.class));}
 public void onCreate(){super.onCreate();settings=new MonitorSettings(this);started=System.currentTimeMillis();boolean leaseCleared;try{clearBrowserSetup(this);leaseCleared=true;}catch(IllegalStateException e){leaseCleared=false;}
  NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);nm.createNotificationChannel(new NotificationChannel("parent-monitor","家长监督已开启",NotificationManager.IMPORTANCE_LOW));
  startForeground(8766,notification());
  screenLock=((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,"ThinkSmart:ScreenMode");screenLock.setReferenceCounted(false);applyScreenMode();
  journal=new UsageJournal(this);screen=new ScreenCapture(this);worker=new HandlerThread("parent-usage");worker.start();handler=new Handler(worker.getLooper());
  server=new ParentHttpServer(this,this);try{server.start();}catch(java.io.IOException e){stopSelf();return;}
  BrowserBridgeIdentity identity=null;if(leaseCleared)try{identity=BrowserBridgeIdentity.create();}catch(Exception ignored){}
  final BrowserBridgeIdentity bridgeIdentity=identity;
  if(bridgeIdentity!=null)bridge=new LoopbackPolicyServer(new LoopbackPolicyServer.Backend(){public long setupUntil(){return browser_setup_until();}public String signPolicy(String nonce,boolean locked,long until)throws Exception{return bridgeIdentity.sign(nonce,locked,until);}public void blocked(String kind,String scheme,String host){BlockedEventStore.get(ParentMonitorService.this).record(kind,host.isEmpty()?scheme+":blocked":scheme+"://"+host,null);}});
  if(bridge!=null)try{bridge.start();}catch(java.io.IOException e){bridge.close();}handler.post(tick);
 }
 private final Runnable tick=new Runnable(){public void run(){try{sample();}catch(RuntimeException ignored){}finally{if(handler!=null)handler.postDelayed(this,5000);}}};
 public int onStartCommand(Intent i,int flags,int id){if(!settings.enabled()){stopSelf();return START_NOT_STICKY;}if(i!=null && "projection".equals(i.getAction())){Intent data=i.getParcelableExtra("data");if(data!=null)screen.authorize(i.getIntExtra("result",0),data);}applyScreenMode();return START_STICKY;}
 private Notification notification(){return new Notification.Builder(this,"parent-monitor").setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("家长监督已开启").setContentText(settings.prefs.getBoolean("keep_screen_on",false)?"不息屏已开启 · 家长查看服务运行中":"30秒无操作息屏 · 家长查看服务运行中").setOngoing(true).build();}
 private void applyScreenMode(){boolean keep=settings.prefs.getBoolean("keep_screen_on",false);if(screenLock!=null){if(keep && !screenLock.isHeld())screenLock.acquire();else if(!keep && screenLock.isHeld())screenLock.release();}((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(8766,notification());}
 public IBinder onBind(Intent i){return null;}
 public void onDestroy(){stopping=true;try{clearBrowserSetup(this);}catch(IllegalStateException ignored){}if(screenLock!=null && screenLock.isHeld())screenLock.release();if(handler!=null){handler.removeCallbacksAndMessages(null);handler=null;}if(server!=null)server.close();if(bridge!=null)bridge.close();if(screen!=null)screen.close();if(worker!=null)worker.quitSafely();super.onDestroy();}
 private boolean usageAccess(){AppOpsManager ops=(AppOpsManager)getSystemService(APP_OPS_SERVICE);return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),getPackageName())==AppOpsManager.MODE_ALLOWED;}
 private void sample(){long now=System.currentTimeMillis(),elapsed=SystemClock.elapsedRealtime();boolean interactive=((PowerManager)getSystemService(POWER_SERVICE)).isInteractive();boolean changed=false;
  if(usageAccess()){
   UsageEvents events=((UsageStatsManager)getSystemService(USAGE_STATS_SERVICE)).queryEvents(eventCursor==0?now-86400000L:eventCursor,now);UsageEvents.Event event=new UsageEvents.Event();
   while(events.hasNextEvent()){events.getNextEvent(event);eventCursor=Math.max(eventCursor,event.getTimeStamp()+1);int type=event.getEventType();if(type==1){foreground=event.getPackageName();changed=true;}else if(type==2 && foreground.equals(event.getPackageName())){foreground="";changed=true;}}

   long delta=elapsed-lastElapsed;
   if(lastElapsed>0 && delta>0 && delta<=15000 && Math.abs(now-lastWall-delta)<2000){journal.add("__observed__",lastWall,now);if(interactive && previousInteractive && !changed && MonitorPolicy.tracked(foreground))journal.add(foreground,lastWall,now);}
  }else foreground="";
  if(now-lastPrune>3600000){journal.prune();BlockedEventStore.get(this).expire();QuestionPhotoProvider.prune(this);lastPrune=now;}
  previousInteractive=interactive;lastElapsed=elapsed;lastWall=now;lastSample=now;
 }
 static String localIp(){try{Enumeration<NetworkInterface> nets=NetworkInterface.getNetworkInterfaces();while(nets.hasMoreElements()){NetworkInterface n=nets.nextElement();if(!n.getName().equals("wlan0"))continue;Enumeration<InetAddress> ips=n.getInetAddresses();while(ips.hasMoreElements()){InetAddress a=ips.nextElement();if(a instanceof Inet4Address && a.isSiteLocalAddress())return a.getHostAddress();}}}catch(Exception ignored){}return "";}
 public JSONObject status(){try{return new JSONObject().put("device_name","ThinkSmart").put("local_ip",localIp()).put("current_app",MonitorPolicy.tracked(foreground)?MonitorPolicy.label(foreground):"桌面或其他系统界面").put("collector_started_at",started).put("last_sample_at",lastSample).put("capture_ready",screen.ready()).put("usage_access",usageAccess()).put("keep_screen_on",settings.prefs.getBoolean("keep_screen_on",false)).put("browser_setup_until",browser_setup_until()).put("browser_control_available",bridge!=null && bridge.isReady());}catch(JSONException e){throw new IllegalStateException();}}
 private long browser_setup_until(){synchronized(ParentMonitorService.class){return BrowserSetupPolicy.validUntil(settings.prefs.getLong("browser_setup_started",0),settings.prefs.getLong("browser_setup_until",0),System.currentTimeMillis());}}
 public long browserSetupEpoch(){return setupGate.epoch();}
 public long startBrowserSetup(long expected){return setupGate.start(expected,()->{if(stopping || bridge==null || !bridge.isReady())throw new IllegalStateException("Browser bridge unavailable");long now=System.currentTimeMillis(),until=now+BrowserSetupPolicy.MAX_SETUP_MS;if(!settings.prefs.edit().putLong("browser_setup_started",now).putLong("browser_setup_until",until).commit())throw new IllegalStateException("Could not save setup");return until;});}
 static void clearBrowserSetup(Context context){setupGate.close(()->{if(!new MonitorSettings(context).prefs.edit().putLong("browser_setup_started",0).putLong("browser_setup_until",0).commit())throw new IllegalStateException("Could not clear setup");});}
 public void stopBrowserSetup(){clearBrowserSetup(this);}
 public JSONObject usage(String date){return journal.report(date,settings.prefs.getLong("since",started),usageAccess());}
 public JSONObject events(String date){long[] day=UsageJournal.day(date);try(Cursor c=BlockedEventStore.get(this).events(0,5000)){JSONArray array=new JSONArray();while(c.moveToNext()){if(c.getLong(1)<day[0] || c.getLong(1)>=day[1])continue;JSONObject item=new JSONObject();for(int j=0;j<BlockedEventStore.COLUMNS.length;j++)item.put(BlockedEventStore.COLUMNS[j],c.isNull(j)?JSONObject.NULL:(j==0||j==1||j==5?c.getLong(j):c.getString(j)));array.put(item);}return new JSONObject().put("date",date).put("events",array);}catch(JSONException e){throw new IllegalStateException();}}
 public byte[] screenshot() throws Exception{return screen.capture();}
 public void requestScreenPermission(){startActivity(new Intent(this,ProjectionRequestActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
 public boolean verifyPassword(String value){return settings.verify(value);}
 public boolean changePassword(String current,String next){return settings.changePassword(current,next,this::stopBrowserSetup);}
}
