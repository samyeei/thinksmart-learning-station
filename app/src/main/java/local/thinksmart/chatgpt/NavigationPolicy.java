package local.thinksmart.chatgpt;

import java.net.URI;
import java.util.Locale;

public final class NavigationPolicy {
    private NavigationPolicy() {}
    private static URI secureUri(String url) {
        if (url == null) return null;
        try {
            URI uri = new URI(url);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || (uri.getPort() != -1 && uri.getPort() != 443)) return null;
            return uri;
        } catch (Exception ignored) { return null; }
    }
    public static boolean allows(String url) {
        URI uri = secureUri(url);
        if (uri == null) return false;
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        String path = uri.getPath();
        if (host.equals("chatgpt.com") || host.equals("auth.openai.com")) return true;
        // Google may set its regional sign-in cookie before returning to the main login host.
        // Keep this exact host and cookie-handshake endpoints; do not allow Google browsing.
        if (host.equals("accounts.google.com.sg")) {
            return path.equals("/accounts/SetSID") || path.equals("/CheckCookie");
        }
        if (host.equals("accounts.google.com")) {
            return path.startsWith("/v3/signin/") || path.startsWith("/signin/")
                    || path.startsWith("/o/oauth2/") || path.equals("/ServiceLogin")
                    || path.equals("/AccountChooser") || path.equals("/CheckCookie");
        }
        return host.equals("appleid.apple.com") && path.startsWith("/auth/");
    }
    public static boolean allowsSubframe(String url) {
        // Blank child frames inherit their already-allowed parent's origin.
        if ("about:blank".equals(url)) return true;
        URI uri = secureUri(url);
        return allows(url) || (uri != null && ("challenges.cloudflare.com".equalsIgnoreCase(uri.getHost())
                || ("cdn.platform.openai.com".equalsIgnoreCase(uri.getHost())
                    && uri.getPath().matches("/deployments/dil/v[0-9]+/runner\\.html"))));
    }
    public static boolean allowsMicrophone(String url) {
        URI uri = secureUri(url);
        return uri != null && "chatgpt.com".equalsIgnoreCase(uri.getHost());
    }
}
