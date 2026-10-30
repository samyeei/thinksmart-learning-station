package local.thinksmart.chatgpt;
import org.junit.Test;
import static org.junit.Assert.*;
public class PhotoPolicyTest {
 @Test public void acceptsPhotoCompatiblePromptsOnly(){assertTrue(PhotoPolicy.allowsImages(null));assertTrue(PhotoPolicy.allowsImages(new String[]{}));assertTrue(PhotoPolicy.allowsImages(new String[]{"image/jpeg","image/png"}));assertTrue(PhotoPolicy.allowsImages(new String[]{"*/*"}));assertFalse(PhotoPolicy.allowsImages(new String[]{"video/*"}));assertFalse(PhotoPolicy.allowsImages(new String[]{"application/pdf"}));}
 @Test public void confinesCameraFilesToGeneratedJpegs(){assertTrue(PhotoPolicy.validName("question-capture-123.jpg"));assertTrue(PhotoPolicy.validName("question-upload-456.jpg"));for(String s:new String[]{"../question-capture-123.jpg","/question-capture-123.jpg","question-capture-123.jpg/other","account.db","question-upload-1.png",null})assertFalse(String.valueOf(s),PhotoPolicy.validName(s));}
}
