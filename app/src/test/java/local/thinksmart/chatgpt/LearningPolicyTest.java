package local.thinksmart.chatgpt;
import org.junit.Test;
import static org.junit.Assert.*;
public class LearningPolicyTest {
 private static final String HOME="https://chatgpt.com/g/g-p-123abc-learning/project";
 @Test public void acceptsOnlyExactPrivateProjectHome(){assertTrue(LearningPolicy.validHome(HOME));for(String s:new String[]{"https://evil.example/g/g-p-123/project","http://chatgpt.com/g/g-p-123/project","https://evil@chatgpt.com/g/g-p-123/project","https://chatgpt.com:8443/g/g-p-123/project","https://chatgpt.com/g/g-p-123/project?token=abc","https://chatgpt.com/g/g-p-123/project#x","https://chatgpt.com/g/g-p-123/project/other"})assertFalse(s,LearningPolicy.validHome(s));}
 @Test public void newChatsAndOtherAccountAreasReturnToLearning(){for(String s:new String[]{"https://chatgpt.com/","https://chatgpt.com/?temporary-chat=true","https://chatgpt.com/c/parents-chat","https://chatgpt.com/g/g-p-other/project","https://chatgpt.com/gpts","https://chatgpt.com/#settings",HOME+"#settings",HOME+"#settings/General","https://chatgpt.com/g/g-p-123abc-learning/c/../../../../c/parent"})assertTrue(s,LearningPolicy.redirects(s,HOME));}
 @Test public void keepsProjectChatsAndLoginUsable(){for(String s:new String[]{HOME,"https://chatgpt.com/g/g-p-123abc-learning/c/new-chat","https://chatgpt.com/auth/login","https://chatgpt.com/api/auth/callback/google","https://auth.openai.com/log-in","https://accounts.google.com/v3/signin/identifier"})assertFalse(s,LearningPolicy.redirects(s,HOME));assertFalse(LearningPolicy.redirects("https://chatgpt.com/",""));}
 @Test public void keepsCanonicalAndNamedRoutesForSameProject(){
  String id="0123456789abcdef0123456789abcdef";
  String home="https://chatgpt.com/g/g-p-"+id+"-xue-xi-fu-dao/project";
  for(String route:new String[]{"/g/g-p-"+id+"/project","/g/g-p-"+id+"/c/local-chatgpt%3Aexample","/g/g-p-"+id+"-renamed/c/conversation"})
   assertFalse(route,LearningPolicy.redirects("https://chatgpt.com"+route,home));
 }
 @Test public void canonicalAliasesDoNotAdmitOtherProjectsOrAccountAreas(){
  String id="0123456789abcdef0123456789abcdef";
  String home="https://chatgpt.com/g/g-p-"+id+"-learning/project";
  for(String route:new String[]{"/g/g-p-1123456789abcdef0123456789abcdef/c/x","/g/g-p-"+id+"extra/c/x","/g/g-p-"+id+"/c/../../../../c/parent","/g/g-p-"+id+"/project#settings","/g/g-p-"+id+"/other","/g/g-p-"+id+"/c/","/g/g-p-"+id+"/c/%2e%2e","/g/g-p-"+id+"/c/%2E","/g/g-p-"+id+"/c/%5cparent","/g/g-p-"+id+"/c/x/extra"})
   assertTrue(route,LearningPolicy.redirects("https://chatgpt.com"+route,home));
 }
}
