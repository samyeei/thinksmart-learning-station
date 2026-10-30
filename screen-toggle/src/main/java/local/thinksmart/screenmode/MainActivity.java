package local.thinksmart.screenmode;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.widget.Toast;
public final class MainActivity extends Activity {
 @Override protected void onCreate(Bundle state){super.onCreate(state);
  try{startActivity(new Intent().setClassName("local.thinksmart.chatgpt","local.thinksmart.chatgpt.ScreenModeActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
  catch(RuntimeException error){Toast.makeText(this,"亮屏服务暂不可用，请打开学习辅导后重试",Toast.LENGTH_LONG).show();}
  finish();
 }
}
