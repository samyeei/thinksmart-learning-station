package local.thinksmart.chatgpt;
import android.content.Context;
final class LearningSettings {
 static String home(Context c){String value=c.getSharedPreferences("device-learning",0).getString("home_url","");return LearningPolicy.validHome(value)?value:"https://chatgpt.com/";}
 static boolean enabled(Context c){return LearningPolicy.validHome(home(c));}
 static void configure(Context c,String value){if(!LearningPolicy.validHome(value))throw new IllegalArgumentException("Exact ChatGPT project home required");c.getSharedPreferences("device-learning",0).edit().putString("home_url",value).commit();}
}
