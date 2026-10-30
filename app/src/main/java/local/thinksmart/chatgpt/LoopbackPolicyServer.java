package local.thinksmart.chatgpt;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Minimal loopback bridge. It has no mutation endpoint for setup policy. */
final class LoopbackPolicyServer implements AutoCloseable {
 interface Backend { long setupUntil(); String signPolicy(String nonce,boolean locked,long until) throws Exception; void blocked(String kind,String scheme,String host); }
 private final Backend backend; private final int requestedPort;
 private final Set<Socket> clients=Collections.newSetFromMap(new ConcurrentHashMap<Socket,Boolean>());
 private final ThreadPoolExecutor workers=new ThreadPoolExecutor(2,2,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(4),r->{Thread t=new Thread(r,"browser-bridge-client");t.setDaemon(true);return t;});
 private final ScheduledExecutorService deadlines=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"browser-bridge-deadline");t.setDaemon(true);return t;});
 private volatile boolean closed,ready; private ServerSocket listener; private long lastEventNanos;
 LoopbackPolicyServer(Backend backend){this(backend,8768);}
 LoopbackPolicyServer(Backend backend,int port){this.backend=backend;this.requestedPort=port;}
 synchronized void start() throws IOException {
  if(closed)throw new IOException("Closed"); if(listener!=null)return;
  ServerSocket s=new ServerSocket(); try{s.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),requestedPort),8);}catch(IOException e){s.close();throw e;}
  listener=s;ready=true;Thread t=new Thread(()->accept(s),"browser-bridge-accept");t.setDaemon(true);t.start();
 }
 boolean isReady(){return ready && !closed;}
 int port(){return listener==null ? requestedPort : listener.getLocalPort();}
 private void accept(ServerSocket s){while(!closed){try{Socket c=s.accept();c.setSoTimeout(1500);clients.add(c);try{ScheduledFuture<?> d=deadlines.schedule(()->closeSocket(c),3,TimeUnit.SECONDS);try{workers.execute(()->{try{serve(c);}finally{d.cancel(false);clients.remove(c);closeSocket(c);}});}catch(RejectedExecutionException e){d.cancel(false);clients.remove(c);closeSocket(c);}}catch(RejectedExecutionException e){clients.remove(c);closeSocket(c);}}catch(IOException e){if(!closed)close();}}}
 private void serve(Socket c){try{
  ParentWebSecurity.Request r;try{r=ParentWebSecurity.read(c.getInputStream());}catch(IOException e){send(c,400,"{}");return;}
  String site=r.headers.get("sec-fetch-site");
  if(!("127.0.0.1:"+port()).equals(r.headers.get("host")) || !"1".equals(r.headers.get("x-thinksmart-bridge")) || r.headers.containsKey("origin") || (site!=null && !site.equals("none"))){send(c,403,"{}");return;}
  if(r.query!=null){send(c,400,"{}");return;}
  if(r.path.equals("/policy") && r.method.equals("GET")){String nonce=r.headers.get("x-thinksmart-nonce");if(nonce==null || !nonce.matches("[0-9a-f]{32}")){send(c,400,"{}");return;}long until=backend.setupUntil();boolean locked=until==0;String signature;try{signature=backend.signPolicy(nonce,locked,until);if(signature==null || !signature.matches("[A-Za-z0-9+/]+={0,2}"))throw new IllegalStateException();}catch(Exception e){send(c,503,"{}");close();return;}send(c,200,"{\"nonce\":\""+nonce+"\",\"childLocked\":"+locked+",\"setupUntil\":"+until+",\"signature\":\""+signature+"\"}");return;}
  if(r.path.equals("/event") && r.method.equals("POST")){
   String type=r.headers.get("content-type");if(type==null || !type.split(";",2)[0].trim().equalsIgnoreCase("application/x-www-form-urlencoded")){send(c,415,"{}");return;}
   if(r.body.length>1024){send(c,400,"{}");return;}
   Map<String,String> fields=parse(r.body);if(fields==null){send(c,400,"{}");return;}
   synchronized(this){long n=System.nanoTime();if(lastEventNanos!=0 && n-lastEventNanos<1000000000L){send(c,429,"{}");return;}lastEventNanos=n;}
   backend.blocked(fields.get("kind"),fields.get("scheme"),fields.get("host"));send(c,200,"{\"acknowledged\":true}");return;
  }
  send(c,(r.path.equals("/policy")||r.path.equals("/event"))?405:404,"{}");
 }catch(Exception ignored){/* No request details or exception text are logged. */}}
 private static Map<String,String> parse(byte[] body){
  for(byte b:body)if((b&255)>127)return null;
  Map<String,String> fields=new HashMap<>();try{for(String pair:new String(body,StandardCharsets.US_ASCII).split("&",-1)){int i=pair.indexOf('=');if(i<1)return null;String k=URLDecoder.decode(pair.substring(0,i),"UTF-8"),v=URLDecoder.decode(pair.substring(i+1),"UTF-8");if(fields.put(k,v)!=null)return null;}}catch(Exception e){return null;}
  if(fields.size()!=3 || !fields.keySet().equals(new HashSet<>(Arrays.asList("kind","scheme","host"))))return null;
  String k=fields.get("kind"),s=fields.get("scheme"),h=fields.get("host");
  if(!Arrays.asList("navigation","new_window","location_guard","download","subframe").contains(k) || !s.matches("[A-Za-z][A-Za-z0-9+.-]{0,31}") || h.length()>253)return null;
  boolean network=Arrays.asList("http","https","ws","wss","ftp").contains(s.toLowerCase(Locale.ROOT));
  if(network ? !validHost(h) : !h.isEmpty())return null;
  return fields;
 }
 private static boolean validHost(String host){
  if(host.startsWith("[")){try{return host.matches("\\[[0-9A-Fa-f:.]+\\]") && new URI("http://"+host).getHost()!=null;}catch(URISyntaxException e){return false;}}
  if(!host.matches("[A-Za-z0-9.-]+"))return false;
  String[] labels=host.split("\\.",-1);for(String label:labels)if(label.length()==0 || label.length()>63 || !label.matches("[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"))return false;
  if(host.matches("[0-9.]+")){if(labels.length!=4)return false;for(String label:labels)try{if(label.length()>1 && label.startsWith("0") || Integer.parseInt(label)>255)return false;}catch(NumberFormatException e){return false;}}
  return true;
 }
 private static void send(Socket c,int code,String json)throws IOException{byte[] b=json.getBytes(StandardCharsets.US_ASCII);String h="HTTP/1.1 "+code+" Response\r\nContent-Type: application/json\r\nContent-Length: "+b.length+"\r\nConnection: close\r\nCache-Control: no-store\r\nX-Content-Type-Options: nosniff\r\n\r\n";c.getOutputStream().write(h.getBytes(StandardCharsets.US_ASCII));c.getOutputStream().write(b);c.getOutputStream().flush();}
 private static void closeSocket(Socket c){try{c.close();}catch(IOException ignored){}}
 public synchronized void close(){closed=true;ready=false;if(listener!=null)try{listener.close();}catch(IOException ignored){}for(Socket c:clients)closeSocket(c);clients.clear();workers.shutdownNow();deadlines.shutdownNow();}
}
