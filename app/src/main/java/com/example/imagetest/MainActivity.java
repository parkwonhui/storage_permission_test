package com.example.imagetest;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_READ_STORAGE = 1000;
    private final int REQUEST_WRITE_STORAGE = 1001;
    private final int REQUEST_CAMERA = 1002;
    private final int REQUEST_READ_STORAGE_IMAGE = 2000;
    private Button imageReadBtn;
    private Button imageWriteBtn;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        imageReadBtn = findViewById(R.id.image_read_btn);
        imageWriteBtn = findViewById(R.id.image_write_btn);
        imageReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_READ_STORAGE_IMAGE);
            }
        });

        imageWriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        checkPhotoPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_READ_STORAGE_IMAGE) {
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap image = getImage(data);
                    if (image != null) {
                        imageView.setImageBitmap(image);
                    }
                } catch (Exception e) {
                    // TODO : toast 띄우기
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "cancle selecting photo", Toast.LENGTH_LONG).show();
            }
        }
    }

    public Bitmap getImage(Intent intent) {
        Uri imageUri = intent.getData();
        /*try {
            // 기존 회전이 안되던 코드
            InputStream in = getContentResolver().openInputStream(intent.getData());
            Bitmap img = BitmapFactory.decodeStream(in);
            in.close();
            return img;
        } catch (Exception e) {
            return null;
        }*/

        String imagePath = getPath(this, imageUri);
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION
                , ExifInterface.ORIENTATION_NORMAL);
        return getRotationBitmap(BitmapFactory.decodeFile(imagePath), orientation);
    }

    public void checkPhotoPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this
                    , Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.d("TEST_LOG", "읽기 권한을 이전에 거부한 경우");

            }
            ActivityCompat.requestPermissions(this
                    , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                    , REQUEST_READ_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 이전 요청을 거부한 경우 true, 다시 묻지 않음 옵션을 선택했다면 false
            if (ActivityCompat.shouldShowRequestPermissionRationale(this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // TODO : 쓰기권한은 처음부터 거부되는데 왜지?? 읽기 권한이 없어서?
                Log.d("TEST_LOG", "쓰기 권한을 이전에 거부한 경우");
            }

            ActivityCompat.requestPermissions(this
                    , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , REQUEST_WRITE_STORAGE);
        }
    }

    // 카메라 권한
    public void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this
                    , Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this
                        , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                        , REQUEST_READ_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TEST_LOG", "Success get reading storage permission");
                } else {
                    Log.d("TEST_LOG", "Fail get reading storage permission ");
                }
                break;
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TEST_LOG", "Success get writing storage permission");
                } else {
                    Log.d("TEST_LOG", "Fail get writing storage permission");
                }
                break;
        }
    }

    private static Bitmap getRotationBitmap(Bitmap bitmap, int orientation) {
        Log.d("TEST_LOG", "orientation:" + orientation);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-270);
                break;
            default:
                return bitmap;
        }

        try {
            Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return resultBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    //https://stackoverflow.com/questions/19985286/convert-content-uri-to-actual-path-in-android-4-4/27271131#27271131
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Log.d("TEST_LOG", "context:"+context+" uri:"+uri+" selection:"+selection+" selectionArgs:"+selectionArgs.toString());

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
