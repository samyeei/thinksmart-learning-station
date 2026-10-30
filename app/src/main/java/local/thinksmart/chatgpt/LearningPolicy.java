package local.thinksmart.chatgpt;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LearningPolicy {
 private static final Pattern PROJECT_ROUTE=Pattern.compile("^/g/(g-p-[A-Za-z0-9_-]+)/(?:project|c/[^/]+)$");
 private static final Pattern PROJECT_ID=Pattern.compile("^g-p-([a-fA-F0-9]{32})(?:-[A-Za-z0-9_-]+)?$");
 private LearningPolicy(){}
 private static URI parse(String url){try{return new URI(url);}catch(Exception e){return null;}}
 public static boolean validHome(String url){URI u=parse(url);return u!=null && "https".equals(u.getScheme()) && "chatgpt.com".equals(u.getHost()) && u.getRawUserInfo()==null && (u.getPort()==-1 || u.getPort()==443) && u.getRawQuery()==null && u.getRawFragment()==null && u.getPath().matches("/g/g-p-[A-Za-z0-9_-]+/project");}
 // ChatGPT starts voice with the canonical project ID, then adds the optional title slug.
 // Compare the stable ID; a title or a missing title must not interrupt the same project.
 private static String projectKey(String path){
  // URI.normalize runs before percent decoding. Reject decoded dot segments explicitly.
  if(path.indexOf('\\')>=0)return null;
  String tail=path.substring(path.lastIndexOf('/')+1);
  if(".".equals(tail) || "..".equals(tail))return null;
  Matcher route=PROJECT_ROUTE.matcher(path);
  if(!route.matches())return null;
  String segment=route.group(1);Matcher id=PROJECT_ID.matcher(segment);
  return id.matches()?"id:"+id.group(1).toLowerCase(Locale.ROOT):"exact:"+segment;
 }
 public static boolean redirects(String url,String home){
  if(!validHome(home))return false;
  URI u=parse(url);if(u==null || !"chatgpt.com".equalsIgnoreCase(u.getHost()))return false;
  String p=u.normalize().getPath();
  if(p.startsWith("/auth/") || p.startsWith("/api/auth/"))return false;
  if(u.getRawFragment()!=null)return true;
  String expected=projectKey(parse(home).getPath());
  return !expected.equals(projectKey(p));
 }
}
