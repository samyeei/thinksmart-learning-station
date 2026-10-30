package local.thinksmart.chatgpt;
import android.content.Context;import android.util.AtomicFile;import java.io.*;import java.nio.charset.StandardCharsets;
/** Restore original engine defaults after the unsuccessful memory trial. */
final class GeckoCompatibilityDefaults {
 static String restore(Context context){AtomicFile file=new AtomicFile(new File(context.getFilesDir(),"gecko-compat.yaml"));FileOutputStream out=null;try{out=file.startWrite();out.write(("prefs:\n  javascript.options.ion: true\n  javascript.options.wasm_optimizingjit: true\n  javascript.options.mem.nursery.max_kb: 65536\n").getBytes(StandardCharsets.UTF_8));file.finishWrite(out);return file.getBaseFile().getAbsolutePath();}catch(IOException e){if(out!=null)file.failWrite(out);android.util.Log.w("ThinkSmartRecovery","compat_config_unavailable");return null;}}
}
