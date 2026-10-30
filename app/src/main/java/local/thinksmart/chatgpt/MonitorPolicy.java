package local.thinksmart.chatgpt;

import java.net.URI;
import java.util.Locale;

/** Privacy boundary shared by journaling and the USB export interface. */
public final class MonitorPolicy {
    public static final int MAX_EVENTS = 5000;
    public static final long RETENTION_MS = 30L * 24 * 60 * 60 * 1000;
    public static final class Target {
        public final String host, scheme;
        Target(String host, String scheme) { this.host = host; this.scheme = scheme; }
    }
    public static Target sanitize(String value) {
        if (value == null) return new Target("", "");
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme == null || scheme.length() > 32) return new Target("", "");
            scheme = scheme.toLowerCase(Locale.ROOT);
            String host = uri.getHost();
            if (!(scheme.equals("https") || scheme.equals("http") || scheme.equals("ftp")
                    || scheme.equals("ws") || scheme.equals("wss")) || host == null
                    || host.length() > 253 || !host.matches("[A-Za-z0-9.\\[\\]:-]+")) host = "";
            return new Target(host.toLowerCase(Locale.ROOT), scheme);
        } catch (Exception ignored) { return new Target("", ""); }
    }
    public static long parseAfter(String value) {
        if (value == null) return 0;
        if (!value.matches("[0-9]{1,19}")) throw new IllegalArgumentException("Invalid after cursor");
        try { return Long.parseLong(value); }
        catch (NumberFormatException ignored) { throw new IllegalArgumentException("Invalid after cursor"); }
    }
    public static int batchLimit(String value) {
        if (value == null) return 500;
        long limit = parseAfter(value);
        if (limit < 1) throw new IllegalArgumentException("Invalid batch limit");
        return (int) Math.min(limit, 500);
    }
    public static boolean tracked(String pkg) {return "local.thinksmart.chatgpt".equals(pkg)||"org.mozilla.firefov".equals(pkg)||"fun.upup.musicfree".equals(pkg)||"com.openai.chatgpt".equals(pkg);}
    public static String label(String pkg) {return "fun.upup.musicfree".equals(pkg)?"MusicFree":"ChatGPT";}
    private MonitorPolicy() {}
}
