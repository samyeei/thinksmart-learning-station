package local.thinksmart.chatgpt;

import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ParentPasswordHttpTest {
 static final String OLD="fixture-old-1234",NEXT="fixture-new-5678";
 Fake backend;ParentHttpServer server;int port;
 @Before public void start()throws Exception{
  backend=new Fake();server=new ParentHttpServer(backend,name->new ByteArrayInputStream("test asset".getBytes(StandardCharsets.UTF_8)));
  port=server.startAt(InetAddress.getByName("127.0.0.1"),0);
 }
 @After public void stop(){server.close();}
 @Test public void requiresLoginAndCsrfAndRejectsWrongCurrentPassword()throws Exception{
  assertEquals(401,request("POST","/api/password","","",form(OLD,NEXT,NEXT)).code);
  Login a=login(OLD);
  assertEquals(403,request("POST","/api/password",a.cookie,"bad",form(OLD,NEXT,NEXT)).code);
  assertEquals(403,request("POST","/api/password",a.cookie,a.csrf,form("wrong-fixture",NEXT,NEXT)).code);
  assertEquals(400,request("POST","/api/password",a.cookie,a.csrf,form(OLD,NEXT,"mismatch-fixture")).code);
  assertEquals(200,request("GET","/api/status",a.cookie,"","").code);assertEquals(OLD,backend.password);
 }
 @Test public void rotationClosesMaintenanceInvalidatesAllSessionsAndKeepsHistory()throws Exception{
  Login a=login(OLD),b=login(OLD);backend.maintenance=true;
  Response changed=request("POST","/api/password",a.cookie,a.csrf,form(OLD,NEXT,NEXT));
  assertEquals(200,changed.code);assertTrue(new JSONObject(changed.body).getBoolean("password_changed"));assertTrue(changed.headers.contains("Max-Age=0"));
  assertFalse(backend.maintenance);assertEquals(12345,backend.historySince);
  assertEquals(401,request("GET","/api/status",a.cookie,"","").code);
  assertEquals(401,request("GET","/api/status",b.cookie,"","").code);
  assertEquals(401,rawLogin(OLD).code);assertEquals(303,rawLogin(NEXT).code);
  assertFalse(changed.body.contains(OLD));assertFalse(changed.body.contains(NEXT));
 }
 @Test public void oldPasswordCannotMintALateSessionAfterRotation()throws Exception{
  Login admin=login(OLD);backend.blockVerification=true;
  ExecutorService pool=Executors.newFixedThreadPool(2);
  try{
   Future<Response> racingLogin=pool.submit(()->rawLogin(OLD));
   assertTrue(backend.entered.await(2,TimeUnit.SECONDS));
   Future<Response> change=pool.submit(()->request("POST","/api/password",admin.cookie,admin.csrf,form(OLD,NEXT,NEXT)));
   Thread.sleep(100);assertFalse("rotation must serialize with login issuance",change.isDone());
   backend.release.countDown();
   Response login=racingLogin.get(3,TimeUnit.SECONDS);assertEquals(303,login.code);
   assertEquals(200,change.get(3,TimeUnit.SECONDS).code);
   assertEquals(401,request("GET","/api/status",cookie(login),"","").code);
  }finally{backend.release.countDown();pool.shutdownNow();}
 }
 @Test public void failedPersistenceDoesNotReportSuccessAndRevokesSessions()throws Exception{
  Login a=login(OLD);backend.failPersistence=true;
  Response failed=request("POST","/api/password",a.cookie,a.csrf,form(OLD,NEXT,NEXT));
  assertEquals(503,failed.code);assertFalse(failed.body.contains("synthetic"));
  assertEquals(401,request("GET","/api/status",a.cookie,"","").code);
 }
 @Test public void wrongCurrentPasswordIsRateLimited()throws Exception{
  Login a=login(OLD);
  for(int i=0;i<5;i++)assertEquals(403,request("POST","/api/password",a.cookie,a.csrf,form("wrong-fixture",NEXT,NEXT)).code);
  assertEquals(429,request("POST","/api/password",a.cookie,a.csrf,form(OLD,NEXT,NEXT)).code);
  assertEquals(OLD,backend.password);
 }
 Login login(String password)throws Exception{
  Response r=rawLogin(password);assertEquals(303,r.code);String c=cookie(r);
  Response s=request("GET","/api/status",c,"","");assertEquals(200,s.code);
  return new Login(c,new JSONObject(s.body).getString("csrf_token"));
 }
 Response rawLogin(String password)throws Exception{return request("POST","/login","","","password="+enc(password));}
 static String form(String a,String b,String c)throws Exception{return "current_password="+enc(a)+"&new_password="+enc(b)+"&confirm_password="+enc(c);}
 static String enc(String value)throws Exception{return URLEncoder.encode(value,"UTF-8");}
 static String cookie(Response r){for(String l:r.headers.split("\r\n"))if(l.startsWith("Set-Cookie: "))return l.substring(12).split(";",2)[0];throw new AssertionError("missing cookie");}
 Response request(String method,String path,String cookie,String csrf,String body)throws Exception{
  byte[] payload=body.getBytes(StandardCharsets.UTF_8);
  try(Socket s=new Socket("127.0.0.1",port)){
   s.setSoTimeout(4000);
   String h=method+" "+path+" HTTP/1.1\r\nHost: 127.0.0.1:8766\r\nOrigin: http://127.0.0.1:8766\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: "+payload.length+"\r\n"+(cookie.isEmpty()?"":"Cookie: "+cookie+"\r\n")+(csrf.isEmpty()?"":"X-CSRF-Token: "+csrf+"\r\n")+"\r\n";
   s.getOutputStream().write(h.getBytes(StandardCharsets.US_ASCII));s.getOutputStream().write(payload);s.getOutputStream().flush();
   String raw=new String(s.getInputStream().readAllBytes(),StandardCharsets.UTF_8);String[] parts=raw.split("\r\n\r\n",2);
   return new Response(Integer.parseInt(parts[0].split(" ",3)[1]),parts[0],parts.length>1?parts[1]:"");
  }
 }
 record Login(String cookie,String csrf){}
 record Response(int code,String headers,String body){}
 static class Fake implements ParentHttpServer.Backend{
  volatile String password=OLD;volatile boolean maintenance,failPersistence,blockVerification;
  final long historySince=12345;final CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
  public JSONObject status(){try{return new JSONObject().put("local_ip","127.0.0.1");}catch(Exception e){throw new IllegalStateException(e);}}
  public JSONObject usage(String date){return new JSONObject();}public JSONObject events(String date){return new JSONObject();}
  public byte[] screenshot(){throw new AssertionError("No screenshot permitted in auth tests");}public void requestScreenPermission(){throw new AssertionError("No media operations permitted");}
  public boolean verifyPassword(String value){if(blockVerification){blockVerification=false;entered.countDown();try{if(!release.await(3,TimeUnit.SECONDS))throw new IllegalStateException();}catch(InterruptedException e){throw new IllegalStateException(e);}}return password.equals(value);}
  public boolean changePassword(String current,String next){if(!password.equals(current))return false;if(failPersistence)throw new IllegalStateException("synthetic storage failure");maintenance=false;password=next;return true;}
  public long browserSetupEpoch(){return 0;}public long startBrowserSetup(long expected){maintenance=true;return 1;}public void stopBrowserSetup(){maintenance=false;}
 }
}
