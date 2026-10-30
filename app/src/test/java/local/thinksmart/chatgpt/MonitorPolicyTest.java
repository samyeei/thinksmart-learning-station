package local.thinksmart.chatgpt;

import org.junit.Test;
import static org.junit.Assert.*;

public class MonitorPolicyTest {
 @Test public void officialAppIsTracked(){
  assertTrue(MonitorPolicy.tracked("com.openai.chatgpt"));
  assertEquals("ChatGPT",MonitorPolicy.label("com.openai.chatgpt"));
  assertEquals("MusicFree",MonitorPolicy.label("fun.upup.musicfree"));
  assertTrue(MonitorPolicy.tracked("org.mozilla.firefov"));
  assertFalse(MonitorPolicy.tracked("com.android.settings"));
 }

    @Test public void stripsEverySensitiveUrlPartAndNormalizesHost() {
        MonitorPolicy.Target target = MonitorPolicy.sanitize("https://child:password@EXAMPLE.com:8443/private?q=secret#token");
        assertEquals("example.com", target.host);
        assertEquals("https", target.scheme);
    }
    @Test public void opaqueUrlsDoNotLeakEmbeddedContentOrNestedUrls() {
        for (String value : new String[]{"javascript:secret()", "data:text/plain,secret", "blob:https://chatgpt.com/private", "intent://example.com/private#Intent;secret", "file:///private/secret", "https://example.com/path with spaces", null, "secret"}) {
            assertEquals("", MonitorPolicy.sanitize(value).host);
        }
        assertEquals("data", MonitorPolicy.sanitize("data:text/plain,secret").scheme);
        assertEquals("", MonitorPolicy.sanitize("not a url").scheme);
    }
    @Test public void deceptiveUrlsOnlyReportActualHost() {
        assertEquals("evil.example", MonitorPolicy.sanitize("https://chatgpt.com@evil.example/private").host);
        assertEquals("", MonitorPolicy.sanitize("https://evil.example%2fchatgpt.com/private").host);
        assertEquals("", MonitorPolicy.sanitize("https://中文.example/private").host);
    }
    @Test public void incrementalCursorAcceptsOnlyNonnegativeDecimalLongs() {
        assertEquals(0, MonitorPolicy.parseAfter(null));
        assertEquals(42, MonitorPolicy.parseAfter("42"));
        assertEquals(Long.MAX_VALUE, MonitorPolicy.parseAfter("9223372036854775807"));
        for (String value : new String[]{"-1", "1 OR 1=1", "1.0", "", "+2", "9223372036854775808"}) {
            try { MonitorPolicy.parseAfter(value); fail(value); } catch (IllegalArgumentException expected) {}
        }
    }
    @Test public void boundsExportBatchAndRetention() {
        assertEquals(500, MonitorPolicy.batchLimit(null));
        assertEquals(500, MonitorPolicy.batchLimit("5000"));
        assertEquals(2, MonitorPolicy.batchLimit("2"));
        for (String value : new String[]{"0", "-2", "oops"}) {
            try { MonitorPolicy.batchLimit(value); fail(value); } catch (IllegalArgumentException expected) {}
        }
        assertEquals(5000, MonitorPolicy.MAX_EVENTS);
        assertEquals(30L * 24 * 60 * 60 * 1000, MonitorPolicy.RETENTION_MS);
    }
    @org.junit.Test public void sanitizedLocalSchemeHasNoHost() {
        MonitorPolicy.Target target=MonitorPolicy.sanitize("about:blocked");
        org.junit.Assert.assertEquals("about",target.scheme);
        org.junit.Assert.assertEquals("",target.host);
    }
}
