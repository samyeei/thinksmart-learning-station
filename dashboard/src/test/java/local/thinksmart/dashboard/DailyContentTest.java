package local.thinksmart.dashboard;
import org.junit.Test;import static org.junit.Assert.*;import java.time.Instant;
public class DailyContentTest {
 @Test public void clockAlwaysUsesShanghaiAnd24Hours(){long t=Instant.parse("2026-10-29T15:08:00Z").toEpochMilli();assertEquals("23:08",DailyContent.clock(t));}
 @Test public void midnightRotatesLocallyNotAtUtcMidnight(){long a=Instant.parse("2026-10-29T15:59:59Z").toEpochMilli(),b=a+1000;assertFalse(DailyContent.quote(a).equals(DailyContent.quote(b)));assertEquals(DailyContent.quote(b),DailyContent.quote(b+3600000));}
 @Test public void quoteAlwaysHasAttributionAndIsStableAcrossRestart(){long a=Instant.parse("2026-10-29T03:00:00Z").toEpochMilli();assertEquals("不积跬步，无以至千里。",DailyContent.quote(a));assertEquals("荀子《劝学》",DailyContent.source(a));assertEquals(DailyContent.quote(a),DailyContent.quote(a+3600000));}
}
