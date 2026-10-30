package local.thinksmart.chatgpt;
import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.graphics.*;
import android.media.ExifInterface;
import java.io.*;
/** Private files; temporary URI grants are issued only to the system still camera. */
public final class QuestionPhotoProvider extends ContentProvider {
 private static final java.util.Set<String> ACTIVE=java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
 static final String AUTHORITY="local.thinksmart.chatgpt.photos";
 private static File directory(Context c){File dir=new File(c.getCacheDir(),"question-photos");if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("Cannot create photo cache");return dir;}
 static void prune(Context c){File[] files=directory(c).listFiles();if(files!=null)for(File f:files)if(PhotoPolicy.validName(f.getName()) && !ACTIVE.contains(f.getName()) && f.lastModified()<System.currentTimeMillis()-3600000)f.delete();}
 static void release(Context c,Uri uri,boolean delete){if(uri==null)return;File f=file(c,uri);ACTIVE.remove(f.getName());if(delete)f.delete();}
 static Uri create(Context c)throws IOException {prune(c);File f=File.createTempFile("question-capture-",".jpg",directory(c));ACTIVE.add(f.getName());return uri(f);}
 private static Uri uri(File file){return new Uri.Builder().scheme("content").authority(AUTHORITY).appendPath(file.getName()).build();}
 static File file(Context c,Uri uri){if(uri==null || !"content".equals(uri.getScheme()) || !AUTHORITY.equals(uri.getAuthority()) || !PhotoPolicy.validName(uri.getLastPathSegment()) || !uri.getPath().equals("/"+uri.getLastPathSegment()) || uri.getQuery()!=null || uri.getFragment()!=null)throw new IllegalArgumentException("Invalid photo URI");return new File(directory(c),uri.getLastPathSegment());}
 static Uri prepareUpload(Context c,Uri input)throws IOException {
  File source=file(c,input);BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeFile(source.getAbsolutePath(),bounds);if(bounds.outWidth<=0 || bounds.outHeight<=0)throw new IOException("Invalid photo");
  BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;while(Math.max(bounds.outWidth,bounds.outHeight)/options.inSampleSize>2048)options.inSampleSize*=2;
  Bitmap bitmap=BitmapFactory.decodeFile(source.getAbsolutePath(),options);if(bitmap==null)throw new IOException("Cannot read photo");Bitmap rotated=bitmap;
  File output=null;boolean complete=false;
  try{
   int orientation=new ExifInterface(source.getAbsolutePath()).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);Matrix matrix=new Matrix();
   switch(orientation){case 2:matrix.postScale(-1,1);break;case 3:matrix.postRotate(180);break;case 4:matrix.postScale(1,-1);break;case 5:matrix.postRotate(90);matrix.postScale(-1,1);break;case 6:matrix.postRotate(90);break;case 7:matrix.postRotate(270);matrix.postScale(-1,1);break;case 8:matrix.postRotate(270);break;default:break;}
   if(!matrix.isIdentity())rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
   output=File.createTempFile("question-upload-",".jpg",directory(c));ACTIVE.add(output.getName());try(FileOutputStream stream=new FileOutputStream(output)){if(!rotated.compress(Bitmap.CompressFormat.JPEG,88,stream))throw new IOException("Cannot encode photo");}
   // Re-encoding deliberately strips camera EXIF, including GPS metadata.
   release(c,input,true);complete=true;return uri(output);
  }finally{if(output!=null && !complete)release(c,uri(output),true);if(rotated!=bitmap)rotated.recycle();bitmap.recycle();}
 }
 @Override public boolean onCreate(){return true;}
 @Override public String getType(Uri uri){file(getContext(),uri);return "image/jpeg";}
 @Override public Cursor query(Uri uri,String[] projection,String selection,String[] selectionArgs,String sortOrder){File f=file(getContext(),uri);if(selection!=null || selectionArgs!=null || sortOrder!=null)throw new IllegalArgumentException();String[] cols=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;MatrixCursor result=new MatrixCursor(cols);Object[] row=new Object[cols.length];for(int i=0;i<cols.length;i++){if(cols[i].equals(OpenableColumns.DISPLAY_NAME))row[i]="question.jpg";else if(cols[i].equals(OpenableColumns.SIZE))row[i]=f.length();else throw new IllegalArgumentException("Unsupported column");}result.addRow(row);return result;}
 @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{File f=file(getContext(),uri);int flags;if(mode.equals("r"))flags=ParcelFileDescriptor.MODE_READ_ONLY;else if(mode.equals("w") || mode.equals("wt"))flags=ParcelFileDescriptor.MODE_WRITE_ONLY|ParcelFileDescriptor.MODE_CREATE|ParcelFileDescriptor.MODE_TRUNCATE;else if(mode.equals("rw"))flags=ParcelFileDescriptor.MODE_READ_WRITE|ParcelFileDescriptor.MODE_CREATE;else throw new FileNotFoundException("Read or camera output only");return ParcelFileDescriptor.open(f,flags);}
 @Override public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
 @Override public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
 @Override public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
}
