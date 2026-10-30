package local.thinksmart.chatgpt;
final class BrowserSetupPolicy {
 static final long MAX_SETUP_MS=600000L;
 static long validUntil(long startedAt,long until,long now) {
  return startedAt>0 && now>=startedAt && until>now && until>startedAt && until-startedAt<=MAX_SETUP_MS ? until : 0;
 }
 private BrowserSetupPolicy() {}
}
