package local.thinksmart.chatgpt;

import org.junit.Test;
import static org.junit.Assert.*;

public class NavigationPolicyTest {
    @Test public void acceptsChatAndNecessaryAuthentication() {
        for (String url : new String[]{"https://chatgpt.com/", "https://chatgpt.com/c/123", "https://CHATGPT.com:443/", "https://auth.openai.com/log-in", "https://accounts.google.com/v3/signin/identifier", "https://accounts.google.com/o/oauth2/v2/auth", "https://appleid.apple.com/auth/authorize"})
            assertTrue(url, NavigationPolicy.allows(url));
    }
    @Test public void blocksExternalSitesAndDisguises() {
        for (String url : new String[]{"https://youtube.com/", "https://www.bilibili.com/", "https://www.google.com/search?q=games", "https://chatgpt.com.evil.example/", "https://evil.example/?chatgpt.com", "https://chatgpt.com@evil.example/", "https://evil.example@chatgpt.com/", "https://chatgpt.com:8443/", "https://chatgpt.com./", "https://help.openai.com/", "https://accounts.google.com/", "https://accounts.google.com/search", "https://appleid.apple.com/account", "https://challenges.cloudflare.com/"})
            assertFalse(url, NavigationPolicy.allows(url));
    }
    @Test public void blocksNonHttpsAndMalformedNavigation() {
        for (String url : new String[]{null,"", "not a URL", "//chatgpt.com/", "http://chatgpt.com/", "javascript:alert(1)", "data:text/html,game", "file:///sdcard/game.html", "content://downloads/1", "intent://youtube.com#Intent;end", "about:config", "blob:https://chatgpt.com/abc"})
            assertFalse(String.valueOf(url), NavigationPolicy.allows(url));
    }
    @Test public void microphoneBelongsOnlyToChatGPT() {
        assertTrue(NavigationPolicy.allowsMicrophone("https://chatgpt.com/"));
        assertFalse(NavigationPolicy.allowsMicrophone("https://auth.openai.com/"));
        assertFalse(NavigationPolicy.allowsMicrophone("https://accounts.google.com/v3/signin/"));
        assertFalse(NavigationPolicy.allowsMicrophone("https://chatgpt.com.evil.example/"));
    }
    @Test public void acceptsGoogleSingaporeAuthenticationRedirect() {
        assertTrue(NavigationPolicy.allows("https://accounts.google.com.sg/accounts/SetSID?continue=https%3A%2F%2Faccounts.google.com%2FCheckCookie"));
        assertTrue(NavigationPolicy.allowsSubframe("https://accounts.google.com.sg/accounts/SetSID"));
        assertTrue(NavigationPolicy.allows("https://accounts.google.com.sg/CheckCookie"));
    }
    @Test public void regionalAuthenticationDoesNotOpenGeneralBrowsing() {
        for(String url:new String[]{"https://accounts.google.com.sg/", "https://accounts.google.com.sg/search?q=games", "https://www.google.com.sg/search?q=games", "https://accounts.google.com.sg/accounts/Other", "https://accounts.google.com.sg/accounts/SetSID/other", "https://accounts.google.com.sg.evil.example/accounts/SetSID", "https://evil.example@accounts.google.com.sg/accounts/SetSID", "http://accounts.google.com.sg/accounts/SetSID", "https://accounts.google.com.sg:8443/accounts/SetSID"})
            assertFalse(url,NavigationPolicy.allows(url));
        assertFalse(NavigationPolicy.allowsMicrophone("https://accounts.google.com.sg/accounts/SetSID"));
    }
    @Test public void officialRunnerIsFrameOnlyAndBlankFramesCanInitialize(){
        String runner="https://cdn.platform.openai.com/deployments/dil/v15/runner.html";
        assertTrue(NavigationPolicy.allowsSubframe(runner));
        assertTrue(NavigationPolicy.allowsSubframe("about:blank"));
        assertFalse(NavigationPolicy.allows(runner));
        assertFalse(NavigationPolicy.allows("about:blank"));
    }
    @Test public void frameExceptionDoesNotOpenCdnBrowsingOrOtherFrames(){
        for(String url:new String[]{"https://cdn.platform.openai.com/other.html","https://cdn.platform.openai.com/deployments/dil/v15/other.html","https://cdn.platform.openai.com.evil.example/deployments/dil/v15/runner.html","http://cdn.platform.openai.com/deployments/dil/v15/runner.html","https://cdn.platform.openai.com:8443/deployments/dil/v15/runner.html","javascript:alert(1)","data:text/html,game","about:config"})assertFalse(url,NavigationPolicy.allowsSubframe(url));
    }
}
