package local.thinksmart.chatgpt;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

/** Opened only by an authorized ADB installer; never receives a password in an Intent. */
public final class ParentSetupActivity extends Activity {
 @Override protected void onCreate(Bundle state){
  super.onCreate(state);
  MonitorSettings settings=new MonitorSettings(this);
  if(settings.prefs.contains("hash")){
   Toast.makeText(this,"已设置家长密码；请在已登录的家长页修改",Toast.LENGTH_LONG).show();finish();return;
  }
  LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(32,32,32,32);
  TextView title=new TextView(this);title.setText("首次设置家长端\n请先向孩子说明：家长可查看前台时长、备用网页拦截域名及经系统授权的当前屏幕。仅在可信家庭 Wi-Fi 使用。");form.addView(title);
  EditText password=new EditText(this),confirm=new EditText(this);
  for(EditText field:new EditText[]{password,confirm}){field.setSingleLine(true);field.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);field.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(128)});field.setImportantForAutofill(android.view.View.IMPORTANT_FOR_AUTOFILL_NO);form.addView(field);}
  password.setHint("家长密码（12–128字符）");confirm.setHint("再次输入密码");
  TextView result=new TextView(this);Button save=new Button(this);save.setText("创建家长密码");form.addView(save);form.addView(result);setContentView(form);
  save.setOnClickListener(v->{
   String value=password.getText().toString();
   if(!value.equals(confirm.getText().toString())){result.setText("两次密码不一致");return;}
   try{settings.provision(value);password.setText("");confirm.setText("");save.setEnabled(false);ParentMonitorService.ensureStarted(this);result.setText("已创建。手机连接同一 Wi-Fi 后访问 http://"+ParentMonitorService.localIp()+":8766 。请保存密码，不要发给 Agent。");}
   catch(IllegalArgumentException e){result.setText("密码需要12–128个字符");}
   catch(IllegalStateException e){result.setText("设置未完成，或已设置过密码；请由家长核对。");}
  });
 }
}
