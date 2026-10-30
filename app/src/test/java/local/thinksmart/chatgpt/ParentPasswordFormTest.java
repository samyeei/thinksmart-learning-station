package local.thinksmart.chatgpt;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import org.junit.Test;
import static org.junit.Assert.*;
public class ParentPasswordFormTest {
 private PasswordChangeForm parse(String s){return PasswordChangeForm.parse(s.getBytes(StandardCharsets.UTF_8),"application/x-www-form-urlencoded; charset=UTF-8");}
 @Test public void preservesUnicodeSpacesAndReservedCharacters()throws Exception{
  String next="  测试-only +&=123456  ";
  PasswordChangeForm f=parse("current_password=old-fixture-123&new_password="+URLEncoder.encode(next,"UTF-8")+"&confirm_password="+URLEncoder.encode(next,"UTF-8"));
  assertEquals(next,f.next);assertEquals("old-fixture-123",f.current);
 }
 @Test public void rejectsMissingDuplicateUnknownAndMalformedFields(){
  for(String s:new String[]{"", "current_password=x", "current_password=x&new_password=fixture-new-123&confirm_password=fixture-new-123&current_password=x", "current_password=x&new_password=fixture-new-123&confirm_password=fixture-new-123&extra=x", "current_password=%ZZ&new_password=fixture-new-123&confirm_password=fixture-new-123"}){
   try{parse(s);fail("Malformed form accepted");}catch(IllegalArgumentException expected){}
  }
 }
 @Test public void rejectsMismatchShortLongAndUnchangedPassword(){
  for(String[] p:new String[][]{{"short","short"},{"fixture-new-123","different-123"},{"x".repeat(129),"x".repeat(129)},{"old-fixture-123","old-fixture-123"}}){
   try{parse("current_password=old-fixture-123&new_password="+p[0]+"&confirm_password="+p[1]);fail("Invalid password accepted");}catch(IllegalArgumentException expected){}
  }
 }
}
