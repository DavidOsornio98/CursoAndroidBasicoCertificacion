package training.edu.droidbountyhunter.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
//import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PictureTools {
    public static final int REQUEST_CODE = 1707;
    private static final String TAG = PictureTools.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static PictureTools instance;
    public static String currentPhotoPath = "";
    private Context context;

    public PictureTools() {
    }

    private synchronized static void createInstance() {
        if (instance == null) {
            instance = new PictureTools();
        }
    }

    public static PictureTools getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    public static PictureTools with(Context context) {
        getInstance().setContext(context);
        return getInstance();
    }

    private void setContext(Context context) {
        this.context = context;
    }

    /**
     * Create a file Uri for saving an image or video
     */
    public static Uri getOutputMediaFileUri() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return FileProvider.getUriForFile(getInstance().context,
                        getInstance().context.getPackageName() + ".provider", getOutputMediaFile());
            }
            return Uri.fromFile(getOutputMediaFile());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile() throws IOException {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getPath(), "DroidBountyHunterPictures");

        String BASE_PATH = mediaStorageDir.getPath() + File.separator;
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("File", "No se pudo crear el folder");
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(new Date());
        String path = "PNG_" + timeStamp + ".png";
        path = path.replace(" ", "_");
        if (path.contains("'")) {
            path = path.replace("'", "");
        }
        String imageFileName = BASE_PATH + path;
        File image = new File(imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private static int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.v(TAG, "Exif orientation: " + orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static Bitmap decodeSampledBitmapFromUri(String dir, int Width, int Height) {
        Bitmap rotatedBitmap;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(dir, options);

            options.inSampleSize = calculateInSampleSize(options, Width, Height);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(dir, options);
            Matrix matrix = new Matrix();
            matrix.postRotate(PictureTools.getCameraPhotoOrientation(dir));
            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            return null;
        }
        return rotatedBitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int Width, int Height) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int size_inicialize = 1;

        if (height > Height || width > Width) {
            if (width > height) {
                size_inicialize = Math.round((float) height / (float) Height);
            } else {
                size_inicialize = Math.round((float) width / (float) Width);
            }
        }
        return size_inicialize;
    }

    public static boolean permissionReadMemmory(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA}, REQUEST_CODE);
                    return false;
                } else {
                    //No explanation needed, we can request the permissions.
                    ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA}, REQUEST_CODE);
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
