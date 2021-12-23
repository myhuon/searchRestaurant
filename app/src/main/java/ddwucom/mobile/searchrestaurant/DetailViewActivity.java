package ddwucom.mobile.searchrestaurant;

import static android.os.Environment.getExternalStoragePublicDirectory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailViewActivity extends AppCompatActivity {

    final static String TAG = "DetailViewActivity";

    TextView tvName;
    TextView tvPhone;
    TextView tvAddress;
    TextView tvlocation;
    TextView tvWebsiteUri;
    EditText etMemo;
    ImageView imageView;

    // .fetchPhoto에 쓰일 PlacesClient 변수
    private PlacesClient placesClient;

//    PendingIntent sender = null;
//    AlarmManager alarmManager = null;

    private final static int REQUEST_TAKE_THUMBNAIL = 100;
    private static final int REQUEST_TAKE_PHOTO = 200;

    private String mCurrentPhotoPath;

    public static final String CHANNEL_ID = "100";

    File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        tvName = findViewById(R.id.tvName_Detail);
        tvPhone = findViewById(R.id.tvPhone_Detail);
        tvAddress = findViewById(R.id.tvAddress_Detail);
        tvlocation = findViewById(R.id.tvLatLng_Detail);
        tvWebsiteUri = findViewById(R.id.tvWebsite_Detail);
        etMemo = findViewById(R.id.etMemo_Detail);
        imageView = findViewById(R.id.ivDetail);

        Intent intent = new Intent();
        String name = intent.getStringExtra("name");
        String adderss = intent.getStringExtra("address");
        String latlng = intent.getStringExtra("lat_lng");
        String websiteUri = intent.getStringExtra("websiteUri");
        String phone = intent.getStringExtra("phone");
        // 사진은 getSerializeableExtra로 꺼내야 함
        PhotoMetadata photoMetadata = (PhotoMetadata) intent.getSerializableExtra("photo");

        tvName.setText(name);
        tvPhone.setText(phone);
        tvAddress.setText(adderss);
        tvlocation.setText(latlng);
        tvWebsiteUri.setText(websiteUri);

        // 사진은 FetchPhotoRequest로 한 번 더 요청 처리 해야지 이미지 Bitmap 얻을 수 있음
        setPhoto(photoMetadata);
    }

    public void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            // 채널 생성
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void onClick(View v){
        switch(v.getId()){
            case R.id.btnSaveMemo:
                // 저장하시겠습니까? 다이얼로그 띄우기
                AlertDialog.Builder dlg = new AlertDialog.Builder(DetailViewActivity.this);
                dlg.setTitle("저장")
                        .setMessage("메모를 저장하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            // 확인 -> 알림 띄우기
                            public void onClick(DialogInterface dialog, int which) {
                                // 메모 디비에 저장하기
                                String imgFilePath = photoFile.getPath();

                                AreaDBHelper helper = new AreaDBHelper(DetailViewActivity.this);
                                SQLiteDatabase db = helper.getWritableDatabase();

                                ContentValues row = new ContentValues();
                                row.put(AreaDBHelper.COL_NAME, tvName.getText().toString());
                                row.put(AreaDBHelper.COL_PHONE, tvPhone.getText().toString());
                                row.put(AreaDBHelper.COL_ADDRESS, tvAddress.getText().toString());
                                row.put(AreaDBHelper.COL_LATLNG, tvlocation.getText().toString());
                                row.put(AreaDBHelper.COL_WEBSITEURI, tvWebsiteUri.getText().toString());
                                row.put(AreaDBHelper.COL_MEMO, etMemo.getText().toString());
                                row.put(AreaDBHelper.COL_IMGPATH, imgFilePath);

                                long result = db.insert(helper.TABLE_NAME, null, row);

                                //		db.execSQL("insert into " + ContactDBHelper.TABLE_NAME + " values ( NULL, '"
                                //	 				+ etName.getText().toString() + "', '" + etPhone.getText().toString() + "', '"
                                //					+ etCategory.getText().toString() + "');");
                                helper.close();

                                if(result > 1){
                                    //Intent intent = new Intent(this, DetailViewActivity.class);
                                    //intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(DetailViewActivity.this, CHANNEL_ID)
                                            .setSmallIcon(R.mipmap.full_heart)
                                            .setContentTitle("메모 저장")
                                            .setContentText("선택한 음식점에 대한 메모가 저장되었습니다.")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            //.setContentIntent(pendingIntent)
                                            .setAutoCancel(true);

                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(DetailViewActivity.this);

                                    int notificationId = 100;
                                    notificationManager.notify(notificationId, builder.build());
                                }
                                else{
                                    Toast.makeText(DetailViewActivity.this, "저장에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            // 취소 -> Toast 띄우기
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(DetailViewActivity.this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();

                break;

            case R.id.btnCapture:
                // 저화질의 썸네일 필요 시 카메라 앱 호출
                dispatchTakePictureIntent();

                break;
        }
    }

    public void setPhoto(PhotoMetadata photoMetadata){
        // Get the attribution text.
        final String attributions = photoMetadata.getAttributions();

        // Create a FetchPhotoRequest. -- FetchPlaceRequest 아님!!
        // photoMetadata를 FetchPhotoRequest.builder()에 설정하여 요청을 획득 - .setMaxWidth() & .setMaxHeight() -> .build()
        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(500) // Optional.
                .setMaxHeight(300) // Optional.
                .build();

        // 생성한 placesClient에 fetchPhoto에 요청을 설정한다. -- fetchPlace() 아님!!
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
            @Override
            // 받은 응답에서 getBitmap()으로 이미지 Bitmap을 꺼낸다.
            public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                // 이미지 view에 셋팅 할 때는 setImageBitmap() 메소드 사용
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePictureIntent.resolveActivity(getPackageManager()) != null){

            try {
                // 파일 정보 가져오기
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(photoFile != null){
                // photoFile 이름으로 내 앱의 전용 폴더에 저장됨
                Uri photoUri = FileProvider.getUriForFile(this, "ddwu.com.mobile.multimedia.photo.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                galleryAddPic();
            }
        }
    }

    // 앱에서 외부 저장소 공용폴더에 사진 저장하면 사진이 추가되었음을 통지하여 MediaDatabase에 기록한다
    private void galleryAddPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    /*현재 시간 정보를 사용하여 파일 정보 생성*/
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // 외부 앱 전용 폴더
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // 외부 공용 폴더
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    /*사진의 크기를 ImageView에서 표시할 수 있는 크기로 변경*/
    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        // Option 변경 안하고 just 정보만 가져오겠다
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        // 정보만 가져오지 않고 Option 변경 하겠다
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;

        // Path에 있는 사진 Option 적용해서(사이즈 변경해서) Bitmap 형태로 가져온다
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }


    @Override
    // 썸네일 카메라 호출 하는 dispatchTakePictureIntent()에서 startActivityForResult() 호출 시 이 메소드로 결과 들어옴
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_THUMBNAIL && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic();
        }
    }
}