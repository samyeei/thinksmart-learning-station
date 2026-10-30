package local.thinksmart.dashboard;
import org.junit.Test;import static org.junit.Assert.*;
public class DashboardLayoutTest {
 @Test public void allCardsFitBothOrientationsWithoutOverlap(){for(boolean portrait:new boolean[]{false,true}){DashboardLayout l=new DashboardLayout(portrait);assertEquals(4,l.cards.length);for(float[] r:l.cards){assertTrue(r[0]>=0&&r[1]>=0);assertTrue(r[0]+r[2]<=l.width);assertTrue(r[1]+r[3]<l.quoteY);assertTrue(r[2]>=200&&r[3]>=170);}for(int i=0;i<l.cards.length;i++)for(int j=i+1;j<l.cards.length;j++){float[]a=l.cards[i],b=l.cards[j];assertTrue(a[0]+a[2]<=b[0]||b[0]+b[2]<=a[0]||a[1]+a[3]<=b[1]||b[1]+b[3]<=a[1]);}}}
 @Test public void portraitUsesTheHeightRatherThanLetterboxingLandscape(){DashboardLayout l=new DashboardLayout(true);assertEquals(800,l.width);assertEquals(1280,l.height);assertTrue(l.cards[0][2]>700);assertTrue(l.cards[1][1]>=l.cards[0][1]+l.cards[0][3]);assertTrue(l.quoteY>1100);}
}
