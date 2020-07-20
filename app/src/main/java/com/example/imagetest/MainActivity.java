package com.example.imagetest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

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
        imageReadBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_READ_STORAGE_IMAGE);
            }
        });

        imageWriteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

            }
        });

        checkPhotoPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TEST_LOG", "requestCode:"+requestCode
                                    +"resultCode:"+resultCode);
        if (requestCode == REQUEST_READ_STORAGE_IMAGE) {
            if (resultCode == RESULT_OK) {
                try {

                    Bitmap image = getImage(data);
                    Log.d("TEST_LOG", "image is null?"+(image==null));
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
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(imageUri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO : 여기서 왜 리턴을 안하죠???
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION
                                                    , ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap img = null;
        try {
            InputStream in = getContentResolver().openInputStream(imageUri);
            img = BitmapFactory.decodeStream(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return getRotationBitmap(img, orientation);
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
            case REQUEST_READ_STORAGE :
                if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TEST_LOG", "Success get reading storage permission");
                } else {
                    Log.d("TEST_LOG", "Fail get reading storage permission ");
                }
                break;
            case REQUEST_WRITE_STORAGE :
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TEST_LOG", "Success get writing storage permission");
                } else {
                    Log.d("TEST_LOG", "Fail get writing storage permission");
                }
                break;
        }
    }

    // TODO : 각 CASE 마다 검증 필요
    public static Bitmap getRotationBitmap(Bitmap bitmap, int orientation) {
        Log.d("TEST_LOG", "orientation:"+orientation);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            // 왼쪽 오른쪽 변경
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, -1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
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
}
