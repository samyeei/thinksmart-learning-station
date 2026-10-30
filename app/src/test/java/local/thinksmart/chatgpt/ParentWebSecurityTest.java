package local.thinksmart.chatgpt;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class ParentWebSecurityTest {
    @Test public void allowsOnlyPrivateIpv4OrLoopbackClients() {
        for (String ip : new String[]{"192.168.1.2", "10.2.3.4", "172.16.0.1", "172.31.255.254", "127.0.0.1"}) assertTrue(ip, ParentWebSecurity.allowedClient(ip));
        for (String ip : new String[]{"8.8.8.8", "172.32.0.1", "172.15.0.1", "169.254.1.1", "192.168.1.999", "::1", "192.168.1.2.evil", "192.168.01.2"}) assertFalse(ip, ParentWebSecurity.allowedClient(ip));
    }
    @Test public void pinsHostAndOriginToLiteralLocalEndpoint() {
        assertTrue(ParentWebSecurity.allowedHost("192.168.1.3:8766", "192.168.1.3"));
        assertTrue(ParentWebSecurity.allowedHost("localhost:8766", "192.168.1.3"));
        for (String host : new String[]{"evil.example:8766", "192.168.1.4:8766", "192.168.1.3:80", "192.168.1.3@evil.example:8766", "192.168.1.3:8766.evil", "192.168.1.3"}) assertFalse(host, ParentWebSecurity.allowedHost(host,"192.168.1.3"));
        assertTrue(ParentWebSecurity.sameOrigin(null,"192.168.1.3:8766"));
        assertTrue(ParentWebSecurity.sameOrigin("http://192.168.1.3:8766", "192.168.1.3:8766"));
        for (String origin : new String[]{"null", "https://192.168.1.3:8766", "http://evil.example:8766", "http://192.168.1.3:8766/path", "http://192.168.1.3:8766/"}) assertFalse(origin, ParentWebSecurity.sameOrigin(origin,"192.168.1.3:8766"));
    }
    @Test public void rejectsAmbiguousHttpAndBoundsBodies() throws Exception {
        ParentWebSecurity.Request request = parse("POST /login HTTP/1.1\r\nHost: 192.168.1.3:8766\r\nContent-Length: 10\r\n\r\npassword=x");
        assertEquals("/login", request.path); assertEquals("password=x", new String(request.body, StandardCharsets.UTF_8));
        for (String raw : new String[]{
                "GET / HTTP/1.1\r\nHost: x\r\nHost: y\r\n\r\n",
                "POST /login HTTP/1.1\r\nHost: x\r\nContent-Length: 4097\r\n\r\n",
                "POST /login HTTP/1.1\r\nHost: x\r\nTransfer-Encoding: chunked\r\n\r\n",
                "GET /%2e%2e/secret HTTP/1.1\r\nHost: x\r\n\r\n",
                "GET /../secret HTTP/1.1\r\nHost: x\r\n\r\n",
                "GET http://evil.example/ HTTP/1.1\r\nHost: x\r\n\r\n",
                "GET / HTTP/1.1\nHost: x\n\n",
                "POST /login HTTP/1.1\r\nHost: x\r\nContent-Length: +1\r\n\r\nx",
                "GET / HTTP/1.1\r\nHost: x\r\nX-Big: " + "x".repeat(16400) + "\r\n\r\n"}) {
            try { parse(raw); fail("Accepted ambiguous request"); } catch (java.io.IOException expected) {}
        }
    }
    @Test public void validatesDateAndRejectsDuplicateParameters() throws Exception {
        assertEquals("2026-10-28", ParentWebSecurity.date("date=2026-10-28"));
        for (String query : new String[]{"date=2026-02-30", "date=2026-10-28&date=2026-10-27", "date=../secret", "date=2026-10-28&other=1", "date=%32%30%32%36-10-28", ""}) {
            try { ParentWebSecurity.date(query); fail(query); } catch (IllegalArgumentException expected) {}
        }
    }
    @Test public void fifthBadPasswordLocksBothPeerAndGlobalWindow() {
        ParentWebSecurity.LoginLimiter limiter = new ParentWebSecurity.LoginLimiter();
        for (int i=0;i<5;i++) { assertTrue(limiter.allowed("192.168.1.2", 1000)); limiter.failed("192.168.1.2", 1000); }
        assertFalse(limiter.allowed("192.168.1.2", 1000));
        assertFalse(limiter.allowed("192.168.1.3", 1000));
        assertTrue(limiter.allowed("192.168.1.2", 61001));
    }
    private ParentWebSecurity.Request parse(String value) throws Exception {
        return ParentWebSecurity.read(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }
}
