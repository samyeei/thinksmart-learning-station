package local.thinksmart.chatgpt;
import android.app.*;
import android.content.*;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
public final class ProjectionRequestActivity extends Activity {
 protected void onCreate(Bundle state){super.onCreate(state);if(state==null)startActivityForResult(((MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(),801);}
 protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==801){if(result==RESULT_OK && data!=null){Intent service=new Intent(this,ParentMonitorService.class).setAction("projection").putExtra("result",result).putExtra("data",data);startForegroundService(service);}finish();}}
}
