package local.thinksmart.chatgpt;
import java.util.Locale;
public final class PhotoPolicy {
 private PhotoPolicy(){}
 public static boolean allowsImages(String[] types){if(types==null || types.length==0)return true;for(String type:types){if(type==null)continue;String s=type.trim().toLowerCase(Locale.ROOT);if(s.equals("*/*") || s.startsWith("image/") || s.equals(".jpg") || s.equals(".jpeg") || s.equals(".png"))return true;}return false;}
 public static boolean validName(String name){return name!=null && name.matches("question-(capture|upload)-[0-9]+\\.jpg");}
}
