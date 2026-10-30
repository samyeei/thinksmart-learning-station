package local.thinksmart.dashboard;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.net.*;import android.net.wifi.*;import android.view.*;import android.widget.*;import android.util.TypedValue;

/** Offline dashboard. The four destinations are compiled in, never supplied by an Intent. */
public final class MainActivity extends Activity {
 private Board board;private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable tick=new Runnable(){public void run(){if(board!=null)board.refresh();handler.postDelayed(this,5000);}};
 protected void onCreate(Bundle state){super.onCreate(state);getWindow().setFlags(1024,1024);board=new Board();setContentView(board);immersive();}
 private void immersive(){getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);}
 protected void onResume(){super.onResume();immersive();handler.removeCallbacks(tick);handler.post(tick);handler.postDelayed(()->{if(board!=null)board.refresh();},300);}
 protected void onPause(){handler.removeCallbacks(tick);super.onPause();}
 public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)immersive();}
 protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);}
 public void onBackPressed(){}
 private void launch(int which){String[] pkgs={"com.openai.chatgpt","fun.upup.musicfree","local.thinksmart.screenmode","local.thinksmart.reboot"};String[] names={"com.openai.chatgpt.MainActivity","fun.upup.musicfree.MainActivity","local.thinksmart.screenmode.MainActivity","local.thinksmart.reboot.MainActivity"};try{startActivity(new Intent().setClassName(pkgs[which],names[which]).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));}catch(RuntimeException e){Toast.makeText(this,"入口暂不可用，请稍后重试",Toast.LENGTH_SHORT).show();}}
 private class Board extends FrameLayout {
  final TextView greeting=text("Hello.",Typeface.create("sans-serif-light",0),0xffe8f3ff),clock=text("",Typeface.create("sans-serif-thin",0),0xfff5f8ff),date=text("",Typeface.DEFAULT,0xffc1d1ee),quote=text("",Typeface.DEFAULT,0xffe4eeff),source=text("",Typeface.DEFAULT,0xff8caaca);
  final Card[] cards={new Card(0,650,417),new Card(1,326,417),new Card(2,210,211),new Card(3,210,194)};final Wifi wifi=new Wifi();Boolean keep=null;float scale=1,ox,oy;
  Board(){super(MainActivity.this);setWillNotDraw(false);setBackgroundColor(0xff03132b);for(TextView t:new TextView[]{greeting,clock,date,quote,source})addView(t);addView(wifi);for(int i=0;i<cards.length;i++){final int index=i;Card card=cards[i];card.setClickable(true);card.setFocusable(true);card.setOnClickListener(v->launch(index));addView(card);}clock.setContentDescription("北京时间，24小时制");refresh();}
  TextView text(String s,Typeface font,int color){TextView t=new TextView(MainActivity.this);t.setText(s);t.setTextColor(color);t.setTypeface(font);t.setIncludeFontPadding(false);t.setSingleLine(true);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
  protected void onMeasure(int ws,int hs){int w=MeasureSpec.getSize(ws),h=MeasureSpec.getSize(hs);boolean portrait=h>w;DashboardLayout layout=new DashboardLayout(portrait);scale=Math.min(w/(float)layout.width,h/(float)layout.height);ox=(w-layout.width*scale)/2;oy=(h-layout.height*scale)/2;
   place(greeting,84,42,500,32,22);place(clock,50,portrait?80:68,portrait?690:610,portrait?150:133,portrait?145:132);place(date,54,portrait?235:199,portrait?690:650,35,portrait?24:22);place(quote,54,layout.quoteY,layout.width-100,34,22);place(source,54,portrait?1200:742,layout.width-100,26,16);place(wifi,layout.width-82,40,48,38,0);
   for(int i=0;i<cards.length;i++){float[]r=layout.cards[i];cards[i].dw=r[2];cards[i].dh=r[3];place(cards[i],r[0],r[1],r[2],r[3],0);}super.onMeasure(ws,hs);}

  void place(View v,float x,float y,float w,float h,float font){FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(Math.round(w*scale),Math.round(h*scale));lp.leftMargin=Math.round(ox+x*scale);lp.topMargin=Math.round(oy+y*scale);FrameLayout.LayoutParams old=(FrameLayout.LayoutParams)v.getLayoutParams();if(old==null||old.width!=lp.width||old.height!=lp.height||old.leftMargin!=lp.leftMargin||old.topMargin!=lp.topMargin)v.setLayoutParams(lp);if(v instanceof TextView)((TextView)v).setTextSize(TypedValue.COMPLEX_UNIT_PX,font*scale);}
  protected void onDraw(Canvas c){super.onDraw(c);Paint p=new Paint(3);p.setShader(new LinearGradient(0,0,getWidth(),getHeight(),new int[]{0xff102d4d,0xff030a18},null,Shader.TileMode.CLAMP));c.drawRect(0,0,getWidth(),getHeight(),p);p.setShader(null);p.setColor(0x183cecf1);for(int y=0;y<getHeight();y+=64)c.drawLine(0,y,getWidth(),y,p);p.setColor(0xff3cecf1);c.drawCircle(ox+62*scale,oy+57*scale,5*scale,p);}

  void refresh(){long now=System.currentTimeMillis();clock.setText(DailyContent.clock(now));date.setText(DailyContent.date(now));quote.setText(DailyContent.quote(now));source.setText("—— "+DailyContent.source(now));try{Bundle b=getContentResolver().call(Uri.parse("content://local.thinksmart.chatgpt.screenstate"),"status",null,null);keep=b==null?null:b.getBoolean("keep_screen_on");}catch(RuntimeException e){keep=null;}for(Card v:cards)v.invalidate();cards[2].setContentDescription("不息屏，"+(keep==null?"待同步":keep?"已开启":"已关闭")+"，点击切换");wifi.sample();}
  final class Card extends View {
   final int index;float dw,dh;final Paint p=new Paint(3);boolean down;
   Card(int i,float w,float h){super(MainActivity.this);index=i;dw=w;dh=h;String[] desc={"ChatGPT，学习辅导","MusicFree，音乐","不息屏开关","重启，点击后确认"};setContentDescription(desc[i]);}
   protected void onDraw(Canvas canvas){super.onDraw(canvas);canvas.save();canvas.scale(getWidth()/dw,getHeight()/dh);int[] accents={0xff43eaf4,0xff9b82ff,0xfff8d68b,0xffff8d9e,0xff63e8bc};int a=accents[index];p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,0,dw,dh,new int[]{index==0?0xc21b6281:0xbf292b53,0xed09182d},null,Shader.TileMode.CLAMP));RectF box=new RectF(1.5f,1.5f,dw-1.5f,dh-1.5f);canvas.drawRoundRect(box,20,20,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(down?3:1.5f);p.setColor(a);p.setAlpha(down?255:190);canvas.drawRoundRect(box,20,20,p);p.setAlpha(255);p.setStyle(Paint.Style.FILL);
    if(index==0){label(canvas,"ChatGPT",46,108,49,0xfff7fbff,true);label(canvas,"学习辅导",46,153,29,0xffbfcef2,false);icon(canvas,0,new RectF(dw-385,54,dw-25,dh-42));}
    else if(index==1){label(canvas,"MusicFree",26,65,32,0xfff7fbff,true);label(canvas,"音乐",26,104,24,0xffbfcef2,false);icon(canvas,1,new RectF(dw-135,12,dw-12,dh-45));}
    else if(index==2){icon(canvas,2,new RectF(dw/2-51,dh<200?8:16,dw/2+51,dh<200?96:115));label(canvas,"不息屏",36,dh-52,25,0xfff7fbff,false);label(canvas,keep==null?"待同步":keep?"已开启":"已关闭",36,dh-24,17,Boolean.TRUE.equals(keep)?0xfff9d17b:0xffb5c4dd,false);}
    else if(index==3){icon(canvas,3,new RectF(dw/2-38,dh<190?10:17,dw/2+39,dh<190?90:99));label(canvas,"重启",36,dh-51,25,0xfff7fbff,false);label(canvas,"点击后确认",36,dh-21,17,0xffb5c4dd,false);}

    float cx=dw-44,cy=dh-39;p.setColor(0x183c6d9b);canvas.drawCircle(cx,cy,23,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0x407ab0dc);canvas.drawCircle(cx,cy,23,p);p.setStrokeWidth(2.6f);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(0xffebf4ff);Path arrow=new Path();arrow.moveTo(cx-3,cy-7);arrow.lineTo(cx+4,cy);arrow.lineTo(cx-3,cy+7);canvas.drawPath(arrow,p);p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);canvas.restore();
   }
   void label(Canvas c,String t,float x,float y,float sz,int color,boolean bold){p.setShader(null);p.setColor(color);p.setTextSize(sz);p.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(t,x,y,p);}
   void icon(Canvas c,int i,RectF d){p.setShader(null);p.setColor(0xffb8f4ff);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.create("sans-serif-light",0));p.setTextSize(Math.min(d.width(),d.height())*.55f);String[] glyphs={"AI","♫","☀","↻"};c.drawText(glyphs[i],d.centerX(),d.centerY()+p.getTextSize()*.32f,p);p.setTextAlign(Paint.Align.LEFT);}

   public boolean onTouchEvent(android.view.MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_DOWN)down=true;if(e.getActionMasked()==MotionEvent.ACTION_CANCEL||e.getActionMasked()==MotionEvent.ACTION_UP)down=false;invalidate();return super.onTouchEvent(e);}
  }
 }
 private final class Wifi extends View {
  boolean connected;int level;final Paint p=new Paint(3);
  Wifi(){super(MainActivity.this);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);}
  void sample(){try{android.net.ConnectivityManager cm=(android.net.ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);android.net.NetworkInfo n=cm.getActiveNetworkInfo();WifiManager wm=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);WifiInfo info=wm.getConnectionInfo();connected=n!=null&&n.getType()==ConnectivityManager.TYPE_WIFI&&n.isConnected();level=info==null?0:WifiManager.calculateSignalLevel(info.getRssi(),4);}catch(RuntimeException e){connected=false;level=0;}setContentDescription(connected?"Wi-Fi已连接，信号"+level+"格":"Wi-Fi未连接");invalidate();}
  protected void onDraw(Canvas c){super.onDraw(c);c.save();c.scale(getWidth()/48f,getHeight()/38f);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(3);for(int i=1;i<=3;i++){p.setColor(connected&&level>=i?0xff70f0ff:0xff46637c);float r=i*8;c.drawArc(new RectF(24-r,35-r,24+r,35+r),220,100,false,p);}p.setStyle(Paint.Style.FILL);p.setColor(connected?0xff70f0ff:0xff71869a);c.drawCircle(24,34,2.6f,p);if(!connected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawLine(11,8,38,35,p);}c.restore();}
 }
}
